package de.drvlabs.btscreen.btprocess;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BedrockCleaner extends BTProcessHelper {
    public static final BedrockCleaner INSTANCE = new BedrockCleaner();

    private BedrockCleaner() {
    }

    private static final ISelectionManager SEL_MGR = Utils.BT.getSelectionManager();
    private static final IInputOverrideHandler INPUT_HANDLER = Utils.BT.getInputOverrideHandler();
    private static final IPlayerContext ctx = Utils.BT.getPlayerContext();
    private static SelectionIterator blockIterator = null;
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
            blockIterator.revertUntil(b -> Utils.MC.world.getBlockState(b).isAir(), 4);
            currentBlock = blockIterator.nextUntil(new Function<>() {
                int columnX = Integer.MAX_VALUE;
                int columnZ = Integer.MAX_VALUE;
                boolean columnHasBedrock = false;

                @Override
                public Boolean apply(BetterBlockPos currentBlock) {
                    if (columnX != currentBlock.x || columnZ != currentBlock.z) {
                        columnX = currentBlock.x;
                        columnZ = currentBlock.z;
                        columnHasBedrock = false;
                    }
                    BlockState blockState = Utils.MC.world.getBlockState(currentBlock);
                    if (columnHasBedrock || blockState.getBlock().equals(Blocks.BEDROCK)) {
                        columnHasBedrock = true;
                        return false;
                    } else if (blockState.isAir() || !blockState.isFullCube(Utils.MC.world, currentBlock)) {
                        return false;
                    }
                    return true;
                }
            });
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
            for (BlockPos saveBlockPos : List.of(
                    walkOnPos.west(),
                    walkOnPos.north(),
                    walkOnPos.south(),
                    walkOnPos.east(),
                    walkOnPos.west().north(),
                    walkOnPos.west().south(),
                    walkOnPos.east().north(),
                    walkOnPos.east().south())) {
                // check if block is save
                BlockState saveBlock = Utils.MC.world.getBlockState(saveBlockPos);
                if (saveBlock.isAir() || !saveBlock.isFullCube(Utils.MC.world, saveBlockPos)) {
                    continue;
                }
                // calculate tp pos
                BlockPos relativBlockPos = saveBlockPos.subtract(walkOnPos);
                tpToPos = walkBlockPos.toBottomCenterPos()
                        .add(relativBlockPos.getX() * 0.3, 0, relativBlockPos.getZ() * 0.3);
                // check if pos is save
                if (Utils.MC.world.getStatesInBox(ClientPlayerEntity.STANDING_DIMENSIONS.getBoxAt(tpToPos))
                        .allMatch(BlockState::isAir)) {
                    break;
                }
                tpToPos = null;
            }
            if (tpToPos == null) {
                BTScreen.chatMessage(Text.literal("Warn: No save block found for block: " + currentBlock.x + ", "
                        + currentBlock.y + ", " + currentBlock.z).formatted(Formatting.GOLD));
                currentBlock = null;
                blockIterator.blacklistPrevious();
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
                if (timeoutTicks > 100) {
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

        private int currentIndex;
        private final Set<Integer> blacklist = new HashSet<>();

        private final int sizeY;
        private final int sizeZ;
        private final int totalSize;
        private final int planeSize;

        public SelectionIterator(BetterBlockPos min, BetterBlockPos max) {
            this.min = min;
            this.max = max;

            this.sizeY = max.y - min.y + 1;
            this.sizeZ = max.z - min.z + 1;
            int sizeX = max.x - min.x + 1;
            this.totalSize = sizeX * this.sizeY * this.sizeZ;
            this.planeSize = this.sizeY * this.sizeZ;

            this.currentIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < totalSize;
        }

        @Override
        public BetterBlockPos next() {
            if (!hasNext()) {
                return null;
            }
            BetterBlockPos pos = getPosFromIndex(currentIndex);
            do {
                currentIndex++;
            } while (blacklist.contains(currentIndex));
            return pos;
        }

        public BetterBlockPos nextUntil(Function<BetterBlockPos, Boolean> predicate) {
            while (hasNext()) {
                BetterBlockPos pos = next();
                if (predicate.apply(pos)) {
                    return pos;
                }
            }
            return null;
        }

        public boolean hasPrevious() {
            return currentIndex > 0;
        }

        public BetterBlockPos previous() {
            do {
                if (!hasPrevious()) {
                    return null;
                }
                currentIndex--;
            } while (blacklist.contains(currentIndex));
            return getPosFromIndex(currentIndex);
        }

        public void blacklistPrevious() {
            blacklist.add(currentIndex - 1);
        }

        public void revertUntil(Function<BetterBlockPos, Boolean> predicate, int matches) {
            int count = 0;
            while (hasPrevious() && count < matches) {
                if (predicate.apply(previous())) {
                    count++;
                }
            }
        }

        private BetterBlockPos getPosFromIndex(int index) {
            if (index < 0 || index >= totalSize) {
                throw new IndexOutOfBoundsException("Must be between 0 and " + totalSize);
            }

            int xIndex = index / planeSize;
            int x = max.x - xIndex;

            int remainingInPlane = index % planeSize;
            int zRelIndex = remainingInPlane / sizeY;

            int z;
            if (x % 2 == 0) { // z descending
                z = max.z - zRelIndex;
            } else { // z ascending
                z = min.z + zRelIndex;
            }

            int y = max.y - (remainingInPlane % sizeY);

            return new BetterBlockPos(x, y, z);
        }
    }
}
