package de.drvlabs.btscreen.btprocess;

import java.util.Iterator;
import java.util.List;

import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.IInputOverrideHandler;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class BedrockCleaner extends BTProcessHelper {
    public static final BedrockCleaner INSTANCE = new BedrockCleaner();

    private BedrockCleaner() {
    }

    protected static final PathingCommand CANCEL = new PathingCommand(null, PathingCommandType.CANCEL_AND_SET_GOAL);
    protected static final PathingCommand CONTINUE = new PathingCommand(null, PathingCommandType.SET_GOAL_AND_PATH);

    private static final ISelectionManager SEL_MGR = Utils.BT.getSelectionManager();
    private static final IInputOverrideHandler INPUT_HANDLER = Utils.BT.getInputOverrideHandler();
    private static final IPlayerContext ctx = Utils.BT.getPlayerContext();
    private static Iterator<BetterBlockPos> blockIterator = null;
    private static int walkY;
    private BetterBlockPos currentBlock = null;
    private Vec3d tpToPos = null;
    private int timeoutTicks = 0;

    @Override
    public boolean isActive() {
        return blockIterator != null;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        INPUT_HANDLER.clearAllKeys();
        if (currentBlock == null) {
            int columnX = Integer.MAX_VALUE, columnZ = Integer.MAX_VALUE;
            boolean columnHasBedrock = false;
            while (currentBlock == null && blockIterator.hasNext()) {
                currentBlock = blockIterator.next();
                if (columnX != currentBlock.x || columnZ != currentBlock.z) {
                    columnX = currentBlock.x;
                    columnZ = currentBlock.z;
                    columnHasBedrock = false;
                }
                BlockState blockState = Utils.MC.world.getBlockState(currentBlock);
                if (columnHasBedrock || blockState.getBlock().equals(Blocks.BEDROCK)) {
                    columnHasBedrock = true;
                    currentBlock = null;
                } else if (blockState.isAir() || !blockState.isFullCube(Utils.MC.world, currentBlock)) {
                    currentBlock = null;
                }
            }
            if (currentBlock == null) {
                onLostControl();
                BTScreen.chatMessage(Text.literal("Bedrock Cleaner finished.").formatted(Formatting.GREEN));
                return DEFER;
            }
            tpToPos = null;
        }
        if (!isSafeToCancel) {
            return CONTINUE;
        }
        BlockState block = Utils.MC.world.getBlockState(currentBlock);
        BlockPos walkBlockPos = currentBlock.withY(walkY);
        if (tpToPos == null) {
            BlockPos walkOnPos = walkBlockPos.down();
            // get save nearby block to sand on
            for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                BlockPos saveBlockPosTmp = walkOnPos.offset(direction);
                BlockState saveBlock = Utils.MC.world.getBlockState(saveBlockPosTmp);
                BlockState saveBlockUp1 = Utils.MC.world.getBlockState(saveBlockPosTmp.up());
                BlockState saveBlockUp2 = Utils.MC.world.getBlockState(saveBlockPosTmp.up(2));
                if (saveBlock.isAir()
                        || !saveBlock.isFullCube(Utils.MC.world, saveBlockPosTmp)
                        || !saveBlockUp1.isAir()
                        || !saveBlockUp2.isAir())
                    continue;
                // calculate tp pos
                tpToPos = walkBlockPos.toBottomCenterPos()
                        .add(direction.getOffsetX() * 0.3, 0, direction.getOffsetZ() * 0.3);
                break;
            }
            if (tpToPos == null) {
                BTScreen.chatMessage(Text.literal("Warn: No save block found for block: " + currentBlock.x + ", "
                        + currentBlock.y + ", " + currentBlock.z).formatted(Formatting.GOLD));
                currentBlock = null;
                return CANCEL;
            }
            timeoutTicks = 0;
        }
        // go to tp pos by walking or tp
        Vec3d playerPos = Utils.MC.player.getPos();
        if (playerPos.isInRange(tpToPos, 1)) {
            Utils.MC.player.setPosition(tpToPos);
            Utils.MC.player.setPitch(90);
            INPUT_HANDLER.setInputForceState(Input.SNEAK, true);
            if (ctx.isLookingAt(currentBlock)) {
                PlayerInventory inventory = Utils.MC.player.getInventory();
                int prevSelectedSlot = inventory.selectedSlot;
                MovementHelper.a(ctx, block); // obfuscated switchToBestToolFor | save?
                if (prevSelectedSlot == inventory.selectedSlot) {
                    INPUT_HANDLER.setInputForceState(Input.CLICK_LEFT, true);
                }
            }
            // next block if "finished" with block
            if (block.isAir() || !block.isFullCube(Utils.MC.world, currentBlock)) {
                currentBlock = null;
            } else {
                timeoutTicks++;
                if (timeoutTicks > 200) {
                    // Timeout
                    BTScreen.chatMessage(Text.literal("Warn: Timeout for block: " + currentBlock.x + ", "
                            + currentBlock.y + ", " + currentBlock.z).formatted(Formatting.GOLD));
                    currentBlock = null;
                }
            }
            return CANCEL;
        } else if (playerPos.isInRange(tpToPos, 2)) {
            Utils.MC.player.setPosition(playerPos.subtract(tpToPos).multiply(0.5).add(tpToPos));
            return CANCEL;
        } else {
            return new PathingCommand(new GoalGetToBlock(currentBlock.withY(walkY)),
                    PathingCommandType.FORCE_REVALIDATE_GOAL_AND_PATH);
        }
    }

    @Override
    public void onLostControl() {
        blockIterator = null;
        currentBlock = null;
        tpToPos = null;
    }

    @Override
    public double priority() {
        return DEFAULT_PRIORITY;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    // /execute in minecraft:overworld run tp @s -2367.24 -59.00 -7263.48 89.44
    // 90.00
    public static void activate() {
        if (blockIterator != null && blockIterator.hasNext()) {
            BTScreen.chatMessage(Text.literal("Error: Bedrock Cleaner already started.").formatted(Formatting.RED));
            return;
        }
        ISelection selection = SEL_MGR.getOnlySelection();
        if (selection == null) {
            BTScreen.chatMessage(Text.literal("Error: Needs exactly one selection.").formatted(Formatting.RED));
            return;
        }
        BetterBlockPos min = selection.min();
        BetterBlockPos max = selection.max();
        if ((max.y - min.y) >= 4) {
            BTScreen.chatMessage(
                    Text.literal("Error: Selection too big (max 4 blocks high).").formatted(Formatting.RED));
            return;
        }
        walkY = max.y + 1;
        blockIterator = new SelectionIterator(min, max);
        BTScreen.chatMessage(Text.literal("Bedrock Cleaner started.").formatted(Formatting.GREEN));
    }

    private static class SelectionIterator implements Iterator<BetterBlockPos> {
        private final BetterBlockPos min;
        private final BetterBlockPos max;
        private int x, y, z;

        public SelectionIterator(BetterBlockPos min, BetterBlockPos max) {
            this.min = min;
            this.max = max;
            this.x = max.x;
            if (x % 2 == 0) {
                this.z = max.z;
            } else {
                this.z = min.z;
            }
            this.y = max.y;
        }

        @Override
        public boolean hasNext() {
            return x >= min.x;
        }

        @Override
        public BetterBlockPos next() {
            if (!hasNext()) {
                return null;
            }
            BetterBlockPos current = new BetterBlockPos(x, y, z);
            y--;
            if (y < min.y) {
                y = max.y;
                if (x % 2 == 0) {
                    z--;
                    if (z < min.z) {
                        z = min.z;
                        x--;
                    }
                } else {
                    z++;
                    if (z > max.z) {
                        z = max.z;
                        x--;
                    }
                }
            }
            return current;
        }
    }
}
