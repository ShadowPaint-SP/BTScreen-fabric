package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_RESUPPLY;
import static de.drvlabs.btscreen.config.Configs.Lists.RESUPPLY_SOURCES;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.event.BaritoneEvents;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.config.options.table.TableRow;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

/**
 * Refills the startup hotbar and offhand layout from configured chests.
 * Empty startup slots are never used, and only one slot is tracked per item.
 */
public final class AutoResupply extends BTProcessWithInitializer implements BaritoneEvents.Started {
    public static final AutoResupply INSTANCE = new AutoResupply();

    private static final int TRIGGER_COUNT = 1;
    private static final int MENU_TIMEOUT_TICKS = 40;

    private static final Map<Identifier, SupplySlot> TRACKED_SLOTS = new LinkedHashMap<>();

    private final List<SupplyTask> tasks = new ArrayList<>();
    private Map<Identifier, List<BlockPos>> sources = Map.of();
    private Phase phase = Phase.IDLE;
    private boolean active;
    private int taskIndex;
    private int sourceIndex;
    private int timeoutTicks;
    private boolean advanceTaskAfterClose;

    private AutoResupply() {
        BaritoneEvents.STARTED.register(this);
    }

    @Override
    public boolean isActive() {
        return isActive(AUTO_RESUPPLY) && !TRACKED_SLOTS.isEmpty() && (active || needsResupply());
    }

    @Override
    protected void onInitialize() {
        if (!validateTrackedSlots()) {
            return;
        }

        sources = readSources();
        if (!AUTO_RESUPPLY.getBooleanValue()) {
            return;
        }
        if (sources.isEmpty()) {
            fail(Component.translatable(LangKeys.INFO + ".autoResupply.noSources"));
            return;
        }

        active = true;
        phase = Phase.WAITING_FOR_DROP;
        Teleport.requestTeleport(Teleport.Home.DROP);
    }

    @Override
    protected PathingCommand onTick() {
        switch (phase) {
            case WAITING_FOR_DROP -> waitForDrop();
            case OPENING_SOURCE -> openSource();
            case WAITING_FOR_MENU -> waitForMenu();
            case WAITING_FOR_CLOSE -> waitForClose();
            case IDLE -> active = false;
        }
        return REQUEST_PAUSE;
    }

    @Override
    protected void onReset() {
        closeOpenContainer();
        active = false;
        phase = Phase.IDLE;
        tasks.clear();
        taskIndex = 0;
        sourceIndex = 0;
        timeoutTicks = 0;
        advanceTaskAfterClose = false;
    }

    @Override
    public double priority() {
        if (active && phase != Phase.WAITING_FOR_DROP && phase != Phase.IDLE) {
            return super.priority() + 0.02;
        }
        return super.priority();
    }

    @Override
    public void baritoneStarted() {
        snapshotSupplySlots();
    }

    private void waitForDrop() {
        if (!Teleport.Home.DROP.isSame(Teleport.getLastHome())) {
            return;
        }

        tasks.clear();
        Inventory inventory = Utils.MC.player.getInventory();
        for (SupplySlot slot : TRACKED_SLOTS.values()) {
            ItemStack current = inventory.getItem(slot.inventorySlot());
            if (current.isEmpty() || current.getCount() < slot.template().getMaxStackSize()) {
                tasks.add(new SupplyTask(slot));
            }
        }

        if (tasks.isEmpty()) {
            finish();
            return;
        }

        taskIndex = 0;
        sourceIndex = 0;
        phase = Phase.OPENING_SOURCE;
    }

    private void openSource() {
        SupplyTask task = currentTask();
        if (task == null) {
            finish();
            return;
        }
        if (!isTargetCompatible(task.slot())) {
            fail(Component.translatable(LangKeys.INFO + ".autoResupply.slotChanged", task.slot().itemId()));
            return;
        }
        if (isTargetFull(task.slot())) {
            advanceTask();
            return;
        }

        List<BlockPos> itemSources = sources.getOrDefault(task.slot().itemId(), List.of());
        if (sourceIndex >= itemSources.size()) {
            fail(Component.translatable(LangKeys.INFO + ".autoResupply.unableToFill", task.slot().itemId()));
            return;
        }

        BlockPos pos = itemSources.get(sourceIndex);

        double reach = Utils.MC.player.blockInteractionRange();
        Vec3 chestCenter = Vec3.atCenterOf(pos);
        if (Utils.MC.player.getEyePosition().distanceToSqr(chestCenter) > reach * reach) {
            skipSource(pos, Component.translatable(LangKeys.INFO + ".autoResupply.outOfReach"));
            return;
        }
        if (!(Utils.MC.level.getBlockState(pos).getBlock() instanceof ChestBlock)) {
            skipSource(pos, Component.translatable(LangKeys.INFO + ".autoResupply.notChest"));
            return;
        }

        BlockHitResult hit = new BlockHitResult(chestCenter, Direction.UP, pos, false);
        if (!Utils.MC.gameMode.useItemOn(Utils.MC.player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
            skipSource(pos, Component.translatable(LangKeys.INFO + ".autoResupply.couldNotOpen"));
            return;
        }

        timeoutTicks = 0;
        phase = Phase.WAITING_FOR_MENU;
    }

    private void waitForMenu() {
        if (Utils.MC.player.containerMenu == Utils.MC.player.inventoryMenu) {
            if (++timeoutTicks > MENU_TIMEOUT_TICKS) {
                skipCurrentSource(Component.translatable(LangKeys.INFO + ".autoResupply.openTimeout"));
            }
            return;
        }

        if (!(Utils.MC.player.containerMenu instanceof ChestMenu menu)) {
            closeAndSkipSource(Component.translatable(LangKeys.INFO + ".autoResupply.unexpectedContainer"));
            return;
        }

        SupplyTask task = currentTask();
        if (task == null || !isTargetCompatible(task.slot())) {
            fail(Component.translatable(LangKeys.INFO + ".autoResupply.slotChanged",
                    task == null ? "?" : task.slot().itemId()));
            return;
        }

        boolean foundItem;
        if (task.slot().inventorySlot() == Inventory.SLOT_OFFHAND) {
            foundItem = refillOffhand(menu, task.slot());
        } else {
            foundItem = refillHotbar(menu, task.slot());
        }

        if (!foundItem) {
            closeAndSkipSource(Component.translatable(LangKeys.INFO + ".autoResupply.itemMissing",
                    task.slot().itemId()));
            return;
        }

        closeForNextStep(isTargetFull(task.slot()));
    }

    private void waitForClose() {
        if (Utils.MC.player.containerMenu != Utils.MC.player.inventoryMenu) {
            if (++timeoutTicks > MENU_TIMEOUT_TICKS) {
                Utils.MC.player.closeContainer();
                timeoutTicks = 0;
            }
            return;
        }

        if (advanceTaskAfterClose) {
            advanceTask();
        } else {
            sourceIndex++;
            phase = Phase.OPENING_SOURCE;
        }
    }

    private boolean refillHotbar(ChestMenu menu, SupplySlot target) {
        Inventory inventory = Utils.MC.player.getInventory();
        OptionalInt targetMenuSlot = menu.findSlot(inventory, target.inventorySlot());
        if (targetMenuSlot.isEmpty()) {
            return false;
        }

        int chestSize = menu.getContainer().getContainerSize();
        boolean found = false;
        for (int sourceSlot = 0; sourceSlot < chestSize && !isTargetFull(target); sourceSlot++) {
            ItemStack source = menu.getSlot(sourceSlot).getItem();
            if (!matchesTemplate(source, target.template())) {
                continue;
            }

            found = true;
            click(menu, sourceSlot, 0, ContainerInput.PICKUP);
            click(menu, targetMenuSlot.getAsInt(), 0, ContainerInput.PICKUP);
            if (!menu.getCarried().isEmpty()) {
                click(menu, sourceSlot, 0, ContainerInput.PICKUP);
            }
        }
        return found;
    }

    private boolean refillOffhand(ChestMenu menu, SupplySlot target) {
        int chestSize = menu.getContainer().getContainerSize();
        int destination = -1;
        int available = 0;

        for (int slot = 0; slot < chestSize; slot++) {
            ItemStack stack = menu.getSlot(slot).getItem();
            if (matchesTemplate(stack, target.template())) {
                if (destination < 0) {
                    destination = slot;
                }
                available += stack.getCount();
            }
        }

        int maxStackSize = target.template().getMaxStackSize();
        if (destination < 0 || available < maxStackSize) {
            return false;
        }

        for (int sourceSlot = 0; sourceSlot < chestSize
                && menu.getSlot(destination).getItem().getCount() < maxStackSize; sourceSlot++) {
            if (sourceSlot == destination
                    || !matchesTemplate(menu.getSlot(sourceSlot).getItem(), target.template())) {
                continue;
            }

            click(menu, sourceSlot, 0, ContainerInput.PICKUP);
            click(menu, destination, 0, ContainerInput.PICKUP);
            if (!menu.getCarried().isEmpty()) {
                click(menu, sourceSlot, 0, ContainerInput.PICKUP);
            }
        }

        if (menu.getSlot(destination).getItem().getCount() < maxStackSize) {
            return false;
        }

        click(menu, destination, Inventory.SLOT_OFFHAND, ContainerInput.SWAP);
        return isTargetFull(target);
    }

    private static void click(ChestMenu menu, int slot, int button, ContainerInput input) {
        Utils.MC.gameMode.handleContainerInput(menu.containerId, slot, button, input, Utils.MC.player);
    }

    private void closeAndSkipSource(Component reason) {
        reportSourceProblem(currentSource(), reason);
        closeForNextStep(false);
    }

    private void closeForNextStep(boolean advanceTask) {
        advanceTaskAfterClose = advanceTask;
        timeoutTicks = 0;
        Utils.MC.player.closeContainer();
        phase = Phase.WAITING_FOR_CLOSE;
    }

    private void skipSource(BlockPos pos, Component reason) {
        reportSourceProblem(pos, reason);
        sourceIndex++;
    }

    private void skipCurrentSource(Component reason) {
        skipSource(currentSource(), reason);
        phase = Phase.OPENING_SOURCE;
    }

    private void reportSourceProblem(BlockPos pos, Component reason) {
        if (pos == null) {
            return;
        }
        BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".autoResupply.sourceProblem",
                pos.getX(), pos.getY(), pos.getZ(), reason).withStyle(ChatFormatting.YELLOW));
    }

    private void advanceTask() {
        taskIndex++;
        sourceIndex = 0;
        if (taskIndex >= tasks.size()) {
            finish();
        } else {
            phase = Phase.OPENING_SOURCE;
        }
    }

    private void finish() {
        closeOpenContainer();
        active = false;
        phase = Phase.IDLE;
        BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".autoResupply.finished")
                .withStyle(ChatFormatting.GREEN));
    }

    private void fail(Component reason) {
        closeOpenContainer();
        AUTO_RESUPPLY.setBooleanValue(false);
        active = false;
        phase = Phase.IDLE;
        BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".autoResupply.failed", reason)
                .withStyle(ChatFormatting.RED));
    }

    private void closeOpenContainer() {
        if (Utils.isInGame() && Utils.MC.player.containerMenu != Utils.MC.player.inventoryMenu) {
            Utils.MC.player.closeContainer();
        }
    }

    private SupplyTask currentTask() {
        return taskIndex >= 0 && taskIndex < tasks.size() ? tasks.get(taskIndex) : null;
    }

    private BlockPos currentSource() {
        SupplyTask task = currentTask();
        if (task == null) {
            return null;
        }
        List<BlockPos> itemSources = sources.getOrDefault(task.slot().itemId(), List.of());
        return sourceIndex >= 0 && sourceIndex < itemSources.size() ? itemSources.get(sourceIndex) : null;
    }

    private static boolean needsResupply() {
        Inventory inventory = Utils.MC.player.getInventory();
        for (SupplySlot slot : TRACKED_SLOTS.values()) {
            ItemStack current = inventory.getItem(slot.inventorySlot());
            if (current.isEmpty() || !matchesTemplate(current, slot.template())
                    || current.getCount() <= TRIGGER_COUNT) {
                return true;
            }
        }
        return false;
    }

    private static boolean validateTrackedSlots() {
        for (SupplySlot slot : TRACKED_SLOTS.values()) {
            if (!isTargetCompatible(slot)) {
                INSTANCE.fail(Component.translatable(LangKeys.INFO + ".autoResupply.slotChanged", slot.itemId()));
                return false;
            }
        }
        return true;
    }

    private static boolean isTargetCompatible(SupplySlot slot) {
        ItemStack current = Utils.MC.player.getInventory().getItem(slot.inventorySlot());
        return current.isEmpty() || matchesTemplate(current, slot.template());
    }

    private static boolean isTargetFull(SupplySlot slot) {
        ItemStack current = Utils.MC.player.getInventory().getItem(slot.inventorySlot());
        return matchesTemplate(current, slot.template())
                && current.getCount() >= slot.template().getMaxStackSize();
    }

    private static boolean matchesTemplate(ItemStack stack, ItemStack template) {
        return !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template);
    }

    private static void snapshotSupplySlots() {
        TRACKED_SLOTS.clear();
        Map<Identifier, List<BlockPos>> configuredSources = readSources();
        if (configuredSources.isEmpty()) {
            return;
        }

        Inventory inventory = Utils.MC.player.getInventory();
        ItemStack offhand = inventory.getItem(Inventory.SLOT_OFFHAND);

        if (!offhand.isEmpty() && offhand.get(DataComponents.FOOD) != null) {
            trackSlot(inventory, Inventory.SLOT_OFFHAND, configuredSources);
        }
        for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
            trackSlot(inventory, slot, configuredSources);
        }
        trackSlot(inventory, Inventory.SLOT_OFFHAND, configuredSources);

        BTScreen.debugLog("Resupply slots: " + TRACKED_SLOTS);
        if (AUTO_RESUPPLY.getBooleanValue() && TRACKED_SLOTS.isEmpty()) {
            INSTANCE.fail(Component.translatable(LangKeys.INFO + ".autoResupply.noTrackedSlots"));
        }
    }

    private static void trackSlot(Inventory inventory, int slot, Map<Identifier, List<BlockPos>> configuredSources) {
        ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty() || stack.getMaxStackSize() <= 1) {
            return;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null || !configuredSources.containsKey(itemId) || TRACKED_SLOTS.containsKey(itemId)) {
            return;
        }
        TRACKED_SLOTS.put(itemId, new SupplySlot(itemId, stack.copyWithCount(1), slot));
    }

    private static Map<Identifier, List<BlockPos>> readSources() {
        Map<Identifier, List<BlockPos>> result = new LinkedHashMap<>();
        for (TableRow row : RESUPPLY_SOURCES.getTable()) {
            if (row.list().size() != 4) {
                continue;
            }

            Identifier itemId = Identifier.tryParse(row.getString(0));
            if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                if (AUTO_RESUPPLY.getBooleanValue()) {
                    INSTANCE.fail(Component.translatable(LangKeys.INFO + ".autoResupply.invalidItem",
                            row.getString(0)));
                }
                return Map.of();
            }

            BlockPos pos = new BlockPos(row.getInt(1), row.getInt(2), row.getInt(3));
            result.computeIfAbsent(itemId, ignored -> new ArrayList<>()).add(pos);
        }
        return result;
    }

    private enum Phase {
        IDLE,
        WAITING_FOR_DROP,
        OPENING_SOURCE,
        WAITING_FOR_MENU,
        WAITING_FOR_CLOSE
    }

    private record SupplySlot(Identifier itemId, ItemStack template, int inventorySlot) {
    }

    private record SupplyTask(SupplySlot slot) {
    }
}
