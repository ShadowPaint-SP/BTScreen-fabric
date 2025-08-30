package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_TORCH;
import static de.drvlabs.btscreen.config.Configs.Generic.MIN_LIGHT_LEVEL;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

public class AutoTorch extends BTProcessHelper {
    private int tick = 0;
    private int torchSlot;

    @Override
    public boolean isActive() {
        return isActive(AUTO_TORCH) && (tick > 0 || isValid());
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (isSafeToCancel) {
            switch (tick) {
                case 0 -> {
                    torchSlot = getTorchSlotInHotbar();
                    if (torchSlot == -1) {
                        AUTO_TORCH.setBooleanValue(false);
                        BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoTorch.noTorch")
                                .formatted(Formatting.RED));
                        return DEFER;
                    }
                    BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoTorch.started"));
                }
                case 1 -> Utils.MC.player.getInventory().setSelectedSlot(torchSlot);
                case 2 -> Utils.MC.player.setPitch(90);
                case 3 -> Utils.MC.player.setSneaking(true);
                case 4 -> {
                    BlockPos blockPos = Utils.MC.player.getBlockPos();
                    ActionResult result = Utils.MC.interactionManager.interactBlock(Utils.MC.player, Hand.MAIN_HAND,
                            new BlockHitResult(blockPos.toCenterPos(), Direction.UP, blockPos, false));
                    if (result instanceof ActionResult.Success success) {
                        if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
                            Utils.MC.player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                }
                case 5 -> Utils.MC.player.setSneaking(false);
                case 6 -> Utils.MC.player.setPitch(0);
                default -> {
                    tick = -1;
                    BTScreen.chatMessage(Text.translatable(LangKeys.INFO + ".autoTorch.finished"));
                }
            }
            tick++;
        }
        return REQUEST_PAUSE;
    }

    @Override
    public void onLostControl() {
        tick = 0;
    }

    @Override
    public double priority() {
        if (tick > 0) {
            return super.priority() + 0.06;
        }
        return super.priority() - 0.02;
    }

    private static boolean isValid() {
        BlockState block = Utils.MC.player.getBlockStateAtPos();
        BlockPos pos = Utils.MC.player.getBlockPos();
        int lightLevel = Utils.MC.world.getLightLevel(LightType.BLOCK, pos);
        if (!block.isReplaceable() || lightLevel >= MIN_LIGHT_LEVEL.getIntegerValue()) {
            return false;
        }
        return Blocks.TORCH.getDefaultState().canPlaceAt(Utils.MC.world, pos);
    }

    private static int getTorchSlotInHotbar() {
        PlayerInventory inventory = Utils.MC.player.getInventory();
        for (int i = 0; i < PlayerInventory.HOTBAR_SIZE; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(Items.TORCH)) {
                return i;
            }
        }
        return -1;
    }
}
