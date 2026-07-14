package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_TORCH;
import static de.drvlabs.btscreen.config.Configs.Generic.MIN_LIGHT_LEVEL;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AutoTorch extends BTProcessHelper {
    public static final AutoTorch INSTANCE = new AutoTorch();

    private AutoTorch() {
    }

    private static boolean hasLightData = false;
    private int tick = 0;
    private int torchSlot;

    @Override
    public boolean isActive() {
        return isActive(AUTO_TORCH) && hasLightData && (tick > 0 || isValid());
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (isSafeToCancel) {
            switch (tick) {
                case 0 -> {
                    torchSlot = getTorchSlotInHotbar();
                    if (torchSlot == -1) {
                        AUTO_TORCH.setBooleanValue(false);
                        BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".autoTorch.noTorch")
                                .withStyle(ChatFormatting.RED));
                        return DEFER;
                    }
                    Utils.BT.getInputOverrideHandler().clearAllKeys();
                }
                case 1 -> Utils.MC.player.getInventory().setSelectedSlot(torchSlot);
                case 2 -> Utils.MC.player.setXRot(90);
                case 3 -> Utils.MC.player.setShiftKeyDown(true);
                case 4 -> {
                    BlockPos blockPos = Utils.MC.player.blockPosition();
                    InteractionResult result = Utils.MC.gameMode.useItemOn(Utils.MC.player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false));
                    if (result instanceof InteractionResult.Success success) {
                        if (success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                            Utils.MC.player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }
                case 5 -> Utils.MC.player.setShiftKeyDown(false);
                case 6 -> Utils.MC.player.setXRot(0);
                default -> tick = -1;
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

    public static void onLightData(int x, int z) {
        ChunkPos chunkPos = Utils.MC.player.chunkPosition();
        if (chunkPos.x() == x && chunkPos.z() == z) {
            hasLightData = true;
        }
    }

    public static void onTeleport() {
        hasLightData = false;
    }

    private static boolean isValid() {
        BlockState block = Utils.MC.player.getInBlockState();
        BlockPos pos = Utils.MC.player.blockPosition();
        int lightLevel = Utils.MC.level.getBrightness(LightLayer.BLOCK, pos);
        if (!block.canBeReplaced() || lightLevel >= MIN_LIGHT_LEVEL.getIntegerValue()) {
            return false;
        }
        return Blocks.TORCH.defaultBlockState().canSurvive(Utils.MC.level, pos);
    }

    private static int getTorchSlotInHotbar() {
        Inventory inventory = Utils.MC.player.getInventory();
        for (int i = 0; i < Inventory.SELECTION_SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.TORCH)) {
                return i;
            }
        }
        return -1;
    }
}
