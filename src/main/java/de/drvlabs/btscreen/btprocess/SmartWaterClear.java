package de.drvlabs.btscreen.btprocess;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.IBuilderProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.event.BaritoneEvents;
import de.drvlabs.btscreen.implementation.LiquidReplacementHelper;
import de.drvlabs.btscreen.utils.Utils;
import de.drvlabs.btscreen.utils.Waiter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clears liquid one layer at a time while extending an outside retaining wall.
 *
 * <p>The two-block inner ring is used as a moving work platform. On each new
 * layer, the process fills liquid in the new inner H ring and then moves beyond
 * that ring. It only then opens the aligned inner and outer H cells on the
 * previous layer wherever the next wall layer has a gap. During that clear,
 * Baritone may only choose standing positions in the safe interior. The
 * Before extending G, the process clears aligned obstructions from H_outer,
 * then walks H_inner clockwise from the nearest corner. G
 * contains only face-adjacent cells on sides that had liquid during the
 * startup scan. Every wall coordinate already built remains protected from
 * Baritone's pathfinder.</p>
 */
public final class SmartWaterClear extends BTProcessHelper implements BaritoneEvents.Stopped {
    public static final SmartWaterClear INSTANCE = new SmartWaterClear();

    private static final String TRANSLATABLE_PREFIX = BTScreen.MOD_ID + ".smartWaterClear.";
    private static final int MAX_PHASE_ATTEMPTS = 4;
    private static final int STABLE_TICKS = 10;
    private static final int G_CONFIRM_TICKS = 5;

    private static final IBuilderProcess BUILD_PROC = Utils.BT.getBuilderProcess();
    private static final ISelectionManager SEL_MGR = Utils.BT.getSelectionManager();
    private static final Settings SETTINGS = BaritoneAPI.getSettings();

    // Baritone calculates paths off-thread, so guard lookups must be thread-safe.
    private static final Set<Long> PROTECTED_G = ConcurrentHashMap.newKeySet();
    private static volatile ExactStandingPathConstraint OUTER_H_CLEAR_PATH_CONSTRAINT;
    private static volatile GPlacementPathConstraint G_PLACEMENT_PATH_CONSTRAINT;

    private Phase phase = Phase.IDLE;
    private ISelection originalSelection;
    private BetterBlockPos min;
    private BetterBlockPos max;
    private Item fillerItem;
    private int currentY;
    private Goal moveInsideGoal;
    private EnumSet<GuardSide> activeGuardSides = EnumSet.noneOf(GuardSide.class);
    private BlockPos currentGTarget;
    private Goal currentGStagingGoal;
    private List<BlockPos> gPlacementOrder = List.of();
    private int gPlacementCursor;
    private int currentGSealedTicks;

    private List<LayerArea> phaseAreas = List.of();
    private Set<Long> requiredSolidPositions = Set.of();
    private String phaseCommand;
    private boolean phasePrepared;
    private boolean commandIssued;
    private int phaseAttempts;
    private int stableTicks;
    private int builderFailureRetries;
    private boolean retryScheduled;

    private SmartWaterClear() {
        BaritoneEvents.STOPPED.register(this);
    }

    @Override
    public boolean isActive() {
        return phase != Phase.IDLE;
    }

    public static boolean isRunning() {
        return INSTANCE.isActive();
    }

    public static boolean isProtected(int x, int y, int z) {
        return isRunning() && PROTECTED_G.contains(BlockPos.asLong(x, y, z));
    }

    /** Hides routine one-block Builder messages during the staged G loop. */
    public static boolean shouldSuppressBaritoneChat(Component message) {
        if (INSTANCE.phase != Phase.FILL_G) {
            return false;
        }
        String text = message.getString();
        return text.endsWith("Filling now") || text.endsWith("Done building");
    }

    /** Keeps current H_outer obstruction clearing on its safe H_inner cell. */
    public static Boolean isAllowedHClearStandingPosition(int targetX, int targetY, int targetZ,
            int standingX, int standingY, int standingZ) {
        ExactStandingPathConstraint outerConstraint = OUTER_H_CLEAR_PATH_CONSTRAINT;
        if (outerConstraint != null) {
            Boolean allowed = outerConstraint.allows(targetX, targetY, targetZ,
                    standingX, standingY, standingZ);
            if (allowed != null) {
                return allowed;
            }
        }
        // Previous-layer H clearing deliberately uses Baritone's unmodified
        // GoalBreak so it can approach normally and mine path obstructions.
        return null;
    }

    /** Constrains the current G placement goal to its matching H_inner cell. */
    public static Boolean isAllowedGPlacementPosition(int targetX, int targetY, int targetZ,
            int standingX, int standingY, int standingZ) {
        GPlacementPathConstraint constraint = G_PLACEMENT_PATH_CONSTRAINT;
        if (constraint == null || constraint.target != BlockPos.asLong(targetX, targetY, targetZ)) {
            return null;
        }
        return constraint.allows(standingX, standingY, standingZ);
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (!isSafeToCancel || BUILD_PROC.isActive()) {
            return REQUEST_PAUSE;
        }

        // Several empty phases can be skipped in one tick, but cap the loop so a
        // zero-volume edge case cannot monopolize the client thread.
        for (int transitions = 0; transitions < 8 && isActive(); transitions++) {
            if (phase == Phase.FILL_G) {
                return tickFillGuard(calcFailed);
            }

            if (phase == Phase.MOVE_INSIDE) {
                BetterBlockPos feet = Utils.BT.getPlayerContext().playerFeet();
                if (moveInsideGoal.isInGoal(feet)) {
                    advancePhase();
                    continue;
                }
                if (calcFailed) {
                    phaseAttempts++;
                    if (phaseAttempts > MAX_PHASE_ATTEMPTS) {
                        fail("stalled", phase.name().toLowerCase(Locale.ROOT), currentY);
                        return DEFER;
                    }
                }
                return new PathingCommand(moveInsideGoal, PathingCommandType.FORCE_REVALIDATE_GOAL_AND_PATH);
            }

            if (!phasePrepared) {
                preparePhase();
            }

            if (!commandIssued && phaseCommand != null) {
                if (!phase.clearsBlocks() && Utils.MC.player.getInventory().countItem(fillerItem) == 0) {
                    fail("outOfBlocks", currentY);
                    return DEFER;
                }
                applyPhaseSelections();
                SETTINGS.okIfWater.value = phase.clearsBlocks();
                BTScreen.debugLog("smart_water_clear phase: {}, y: {}, areas: {}", phase, currentY,
                        phaseAreas.size());
                Utils.execute(phaseCommand);
                commandIssued = true;
                return REQUEST_PAUSE;
            }

            if (!phaseSatisfied()) {
                stableTicks = 0;
                phaseAttempts++;
                if (phaseAttempts > MAX_PHASE_ATTEMPTS) {
                    fail("stalled", phase.name().toLowerCase(Locale.ROOT), currentY);
                    return DEFER;
                }
                phasePrepared = false;
                commandIssued = false;
                continue;
            }

            stableTicks++;
            if (stableTicks < phase.requiredStableTicks()) {
                return REQUEST_PAUSE;
            }
            advancePhase();
        }
        return isActive() ? REQUEST_PAUSE : DEFER;
    }

    private void preparePhase() {
        phaseAreas = switch (phase) {
            case INITIAL_T, FILL_T -> List.of(fullLayer(currentY));
            case INITIAL_G, FILL_G -> guardRing(currentY);
            case CLEAR_PREVIOUS_H -> previousHOpenings();
            case CLEAR_CURRENT_OUTER_H -> currentOuterHObstructions();
            case FILL_INNER_H -> innerRing(currentY, 1);
            case FILL_OUTER_H -> innerRing(currentY, 0);
            case IDLE, MOVE_INSIDE -> List.of();
        };

        if (phase.buildsGuard()) {
            protect(phaseAreas);
        } else if (phase == Phase.FILL_INNER_H || phase == Phase.CLEAR_PREVIOUS_H
                || phase == Phase.CLEAR_CURRENT_OUTER_H) {
            // Freeze the next G layer before any H work. Existing wall blocks on
            // that layer are valid parts of the wall and must not become a path.
            protect(guardRing(currentY));
        }

        Set<Long> actionPositions = new HashSet<>();
        Set<String> selectors = new LinkedHashSet<>();
        if (phase.clearsBlocks()) {
            forEachPosition(phaseAreas, pos -> {
                BlockState state = Utils.MC.level.getBlockState(pos);
                boolean needsClear = phase == Phase.CLEAR_CURRENT_OUTER_H
                        ? !isOpenForGuardPlacement(state)
                        : !isWorkSpace(state);
                if (needsClear) {
                    actionPositions.add(pos.asLong());
                }
            });
            phaseCommand = actionPositions.isEmpty() ? null : "sel cleararea";
        } else {
            forEachPosition(phaseAreas, pos -> {
                BlockState state = Utils.MC.level.getBlockState(pos);
                boolean needsReplacement = phase.buildsGuard() || phase == Phase.FILL_INNER_H
                        ? !isSealed(state, pos)
                        : !state.getFluidState().isEmpty();
                if (needsReplacement) {
                    actionPositions.add(pos.asLong());
                    selectors.add(LiquidReplacementHelper.selectorFor(state));
                }
            });
            phaseCommand = selectors.isEmpty() ? null
                    : LiquidReplacementHelper.createReplaceCommand(fillerItem, selectors);
        }

        requiredSolidPositions = phase.requiresCapturedSolidTargets() ? Set.copyOf(actionPositions) : Set.of();
        OUTER_H_CLEAR_PATH_CONSTRAINT = phase == Phase.CLEAR_CURRENT_OUTER_H
                ? new ExactStandingPathConstraint(currentOuterHStandingPositions(actionPositions))
                : null;
        phasePrepared = true;
        commandIssued = false;
        stableTicks = 0;
    }

    private boolean phaseSatisfied() {
        if (phase.clearsBlocks()) {
            StatePredicate predicate = phase == Phase.CLEAR_CURRENT_OUTER_H
                    ? (state, pos) -> isOpenForGuardPlacement(state)
                    : (state, pos) -> isWorkSpace(state);
            return allPositionsMatch(phaseAreas, predicate);
        }
        if (phase.buildsGuard()) {
            return allPositionsMatch(phaseAreas, SmartWaterClear::isSealed);
        }
        if (phase == Phase.FILL_T) {
            return allPositionsMatch(phaseAreas, (state, pos) -> state.getFluidState().isEmpty());
        }
        for (long packedPos : requiredSolidPositions) {
            BlockPos pos = BlockPos.of(packedPos);
            if (!isSealed(Utils.MC.level.getBlockState(pos), pos)) {
                return false;
            }
        }
        return true;
    }

    private void advancePhase() {
        phase = switch (phase) {
            case INITIAL_T -> Phase.INITIAL_G;
            case INITIAL_G -> nextLayerOrFinish();
            case FILL_INNER_H -> Phase.MOVE_INSIDE;
            case MOVE_INSIDE -> Phase.CLEAR_PREVIOUS_H;
            case CLEAR_PREVIOUS_H -> Phase.CLEAR_CURRENT_OUTER_H;
            case CLEAR_CURRENT_OUTER_H -> Phase.FILL_G;
            case FILL_G -> Phase.FILL_OUTER_H;
            case FILL_OUTER_H -> Phase.FILL_T;
            case FILL_T -> nextLayerOrFinish();
            case IDLE -> Phase.IDLE;
        };
        phasePrepared = false;
        commandIssued = false;
        phaseAttempts = 0;
        stableTicks = 0;
        builderFailureRetries = 0;
        retryScheduled = false;
        OUTER_H_CLEAR_PATH_CONSTRAINT = null;
        resetGPlacementLoop();
    }

    private Phase nextLayerOrFinish() {
        message("layerFinished", ChatFormatting.GRAY, currentY);
        if (currentY <= min.y) {
            finish();
            return Phase.IDLE;
        }
        currentY--;
        return Phase.FILL_INNER_H;
    }

    private PathingCommand tickFillGuard(boolean calcFailed) {
        protect(guardRing(currentY));

        if (currentGTarget != null) {
            boolean sealed = isSealed(Utils.MC.level.getBlockState(currentGTarget), currentGTarget);
            currentGSealedTicks = sealed ? currentGSealedTicks + 1 : 0;
            if (sealed && currentGSealedTicks >= G_CONFIRM_TICKS) {
                gPlacementCursor++;
                clearCurrentGPlacementTarget();
                phaseAttempts = 0;
                stableTicks = 0;
            } else if (sealed) {
                return REQUEST_PAUSE;
            }
        }

        if (currentGTarget == null) {
            currentGTarget = findNextGuardTarget();
            if (currentGTarget == null) {
                stableTicks++;
                if (stableTicks >= STABLE_TICKS) {
                    advancePhase();
                }
                return REQUEST_PAUSE;
            }

            BlockPos stagingSupport = guardStagingPosition(currentGTarget, currentY);
            if (!isSealed(Utils.MC.level.getBlockState(stagingSupport), stagingSupport)) {
                fail("unwalkableInnerH", stagingSupport.getX(), stagingSupport.getY(), stagingSupport.getZ());
                return DEFER;
            }
            BlockPos stagingFeet = stagingSupport.above();
            currentGStagingGoal = new GoalBlock(stagingFeet);
            G_PLACEMENT_PATH_CONSTRAINT = new GPlacementPathConstraint(currentGTarget.asLong(),
                    stagingFeet.getX(), stagingFeet.getY(), stagingFeet.getZ());
            stableTicks = 0;
        }

        BetterBlockPos feet = Utils.BT.getPlayerContext().playerFeet();
        if (!currentGStagingGoal.isInGoal(feet)) {
            if (calcFailed) {
                phaseAttempts++;
                if (phaseAttempts > MAX_PHASE_ATTEMPTS) {
                    fail("stalled", "move_to_g", currentY);
                    return DEFER;
                }
            }
            return new PathingCommand(currentGStagingGoal,
                    PathingCommandType.FORCE_REVALIDATE_GOAL_AND_PATH);
        }

        if (!commandIssued) {
            if (Utils.MC.player.getInventory().countItem(fillerItem) == 0) {
                fail("outOfBlocks", currentY);
                return DEFER;
            }
            BlockState state = Utils.MC.level.getBlockState(currentGTarget);
            if (isSealed(state, currentGTarget)) {
                return REQUEST_PAUSE;
            }
            phaseAreas = List.of(new LayerArea(currentGTarget.getX(), currentGTarget.getX(), currentY,
                    currentGTarget.getZ(), currentGTarget.getZ()));
            phaseCommand = LiquidReplacementHelper.createReplaceCommand(fillerItem,
                    Set.of(LiquidReplacementHelper.selectorFor(state)));
            applyPhaseSelections();
            SETTINGS.okIfWater.value = false;
            BTScreen.debugLog("smart_water_clear G target: {}, staging: {}", currentGTarget,
                    currentGStagingGoal);
            Utils.execute(phaseCommand);
            commandIssued = true;
            return REQUEST_PAUSE;
        }

        phaseAttempts++;
        if (phaseAttempts > MAX_PHASE_ATTEMPTS) {
            fail("stalled", "place_g", currentY);
            return DEFER;
        }
        commandIssued = false;
        return REQUEST_PAUSE;
    }

    private BlockPos findNextGuardTarget() {
        while (gPlacementCursor < gPlacementOrder.size()) {
            BlockPos pos = gPlacementOrder.get(gPlacementCursor);
            if (!isSealed(Utils.MC.level.getBlockState(pos), pos)) {
                return pos;
            }
            gPlacementCursor++;
        }

        List<BlockPos> unfinished = rotateToNearestCorner(clockwiseGuardPositions(currentY)).stream()
                .filter(pos -> !isSealed(Utils.MC.level.getBlockState(pos), pos))
                .toList();
        if (unfinished.isEmpty()) {
            return null;
        }
        gPlacementOrder = List.copyOf(unfinished);
        gPlacementCursor = 0;
        return gPlacementOrder.getFirst();
    }

    private List<BlockPos> rotateToNearestCorner(List<BlockPos> clockwise) {
        if (clockwise.isEmpty()) {
            return clockwise;
        }

        BetterBlockPos feet = Utils.BT.getPlayerContext().playerFeet();
        GuardSide[] cornerStarts = {
                GuardSide.NORTH, // north-west
                GuardSide.EAST,  // north-east
                GuardSide.SOUTH, // south-east
                GuardSide.WEST   // south-west
        };
        int[][] innerCorners = {
                { min.x + 1, min.z + 1 },
                { max.x - 1, min.z + 1 },
                { max.x - 1, max.z - 1 },
                { min.x + 1, max.z - 1 }
        };
        GuardSide nearestCornerStart = cornerStarts[0];
        long nearestDistance = Long.MAX_VALUE;
        for (int i = 0; i < innerCorners.length; i++) {
            long dx = innerCorners[i][0] - feet.x;
            long dz = innerCorners[i][1] - feet.z;
            long distance = dx * dx + dz * dz;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestCornerStart = cornerStarts[i];
            }
        }

        GuardSide startSide = nearestCornerStart;
        for (int offset = 0; offset < GuardSide.CLOCKWISE.size(); offset++) {
            GuardSide candidate = GuardSide.CLOCKWISE.get((nearestCornerStart.ordinal() + offset)
                    % GuardSide.CLOCKWISE.size());
            if (activeGuardSides.contains(candidate)) {
                startSide = candidate;
                break;
            }
        }

        int nearestIndex = clockwise.indexOf(firstGuardPosition(startSide, currentY));
        if (nearestIndex < 0) {
            return List.copyOf(clockwise);
        }
        List<BlockPos> rotated = new ArrayList<>(clockwise.size());
        rotated.addAll(clockwise.subList(nearestIndex, clockwise.size()));
        rotated.addAll(clockwise.subList(0, nearestIndex));
        return List.copyOf(rotated);
    }

    private BlockPos firstGuardPosition(GuardSide side, int y) {
        return switch (side) {
            case NORTH -> new BlockPos(min.x, y, min.z - 1);
            case EAST -> new BlockPos(max.x + 1, y, min.z);
            case SOUTH -> new BlockPos(max.x, y, max.z + 1);
            case WEST -> new BlockPos(min.x - 1, y, max.z);
        };
    }

    private void clearCurrentGPlacementTarget() {
        currentGTarget = null;
        currentGStagingGoal = null;
        currentGSealedTicks = 0;
        G_PLACEMENT_PATH_CONSTRAINT = null;
        phaseCommand = null;
        commandIssued = false;
    }

    private void resetGPlacementLoop() {
        clearCurrentGPlacementTarget();
        gPlacementOrder = List.of();
        gPlacementCursor = 0;
    }

    private void applyPhaseSelections() {
        SEL_MGR.removeAllSelections();
        phaseAreas.forEach(area -> SEL_MGR.addSelection(area.min(), area.max()));
    }

    private LayerArea fullLayer(int y) {
        return new LayerArea(min.x, max.x, y, min.z, max.z);
    }

    private List<LayerArea> guardRing(int y) {
        // G contains only face-neighbors of T. The four diagonal corners are
        // deliberately omitted because they do not stop fluid flow and make
        // Baritone's wall access less reliable.
        List<LayerArea> result = new ArrayList<>(4);
        for (GuardSide side : GuardSide.CLOCKWISE) {
            if (activeGuardSides.contains(side)) {
                result.add(guardSideArea(side, y, min, max));
            }
        }
        return result;
    }

    private List<BlockPos> clockwiseGuardPositions(int y) {
        List<BlockPos> result = new ArrayList<>();
        if (activeGuardSides.contains(GuardSide.NORTH)) {
            for (int x = min.x; x <= max.x; x++) {
                result.add(new BlockPos(x, y, min.z - 1));
            }
        }
        if (activeGuardSides.contains(GuardSide.EAST)) {
            for (int z = min.z; z <= max.z; z++) {
                result.add(new BlockPos(max.x + 1, y, z));
            }
        }
        if (activeGuardSides.contains(GuardSide.SOUTH)) {
            for (int x = max.x; x >= min.x; x--) {
                result.add(new BlockPos(x, y, max.z + 1));
            }
        }
        if (activeGuardSides.contains(GuardSide.WEST)) {
            for (int z = max.z; z >= min.z; z--) {
                result.add(new BlockPos(min.x - 1, y, z));
            }
        }
        return result;
    }

    private static LayerArea guardSideArea(GuardSide side, int y, BetterBlockPos min, BetterBlockPos max) {
        return switch (side) {
            case NORTH -> new LayerArea(min.x, max.x, y, min.z - 1, min.z - 1);
            case EAST -> new LayerArea(max.x + 1, max.x + 1, y, min.z, max.z);
            case SOUTH -> new LayerArea(min.x, max.x, y, max.z + 1, max.z + 1);
            case WEST -> new LayerArea(min.x - 1, min.x - 1, y, min.z, max.z);
        };
    }

    private List<LayerArea> innerRing(int y, int inset) {
        int minX = min.x + inset;
        int maxX = max.x - inset;
        int minZ = min.z + inset;
        int maxZ = max.z - inset;
        if (minX > maxX || minZ > maxZ) {
            return List.of();
        }
        List<LayerArea> result = new ArrayList<>(4);
        result.add(new LayerArea(minX, maxX, y, minZ, minZ));
        if (maxZ != minZ) {
            result.add(new LayerArea(minX, maxX, y, maxZ, maxZ));
        }
        if (minZ + 1 <= maxZ - 1) {
            result.add(new LayerArea(minX, minX, y, minZ + 1, maxZ - 1));
            if (maxX != minX) {
                result.add(new LayerArea(maxX, maxX, y, minZ + 1, maxZ - 1));
            }
        }
        return result;
    }

    private List<LayerArea> previousHOpenings() {
        Set<Long> openings = new HashSet<>();
        forEachPosition(guardRing(currentY), guardPos -> {
            BlockState state = Utils.MC.level.getBlockState(guardPos);
            if (!isSealed(state, guardPos)) {
                BlockPos outerH = outerHPosition(guardPos, currentY + 1);
                if (!state.getFluidState().isEmpty() && isSelectionCorner(outerH)) {
                    addCornerOpening(openings, outerH);
                } else {
                    openings.add(outerH.asLong());
                    openings.add(guardStagingPosition(guardPos, currentY + 1).asLong());
                }
            }
        });
        return mergeHorizontalRuns(openings, currentY + 1);
    }

    private boolean isSelectionCorner(BlockPos pos) {
        return (pos.getX() == min.x || pos.getX() == max.x)
                && (pos.getZ() == min.z || pos.getZ() == max.z);
    }

    private void addCornerOpening(Set<Long> openings, BlockPos outerCorner) {
        int innerX = outerCorner.getX() == min.x ? outerCorner.getX() + 1 : outerCorner.getX() - 1;
        int innerZ = outerCorner.getZ() == min.z ? outerCorner.getZ() + 1 : outerCorner.getZ() - 1;
        int y = outerCorner.getY();
        openings.add(outerCorner.asLong());
        openings.add(BlockPos.asLong(innerX, y, outerCorner.getZ()));
        openings.add(BlockPos.asLong(outerCorner.getX(), y, innerZ));
        openings.add(BlockPos.asLong(innerX, y, innerZ));
    }

    private List<LayerArea> currentOuterHObstructions() {
        Set<Long> obstructions = new HashSet<>();
        forEachPosition(guardRing(currentY), guardPos -> {
            if (!isSealed(Utils.MC.level.getBlockState(guardPos), guardPos)) {
                BlockPos outerH = outerHPosition(guardPos, currentY);
                if (!isOpenForGuardPlacement(Utils.MC.level.getBlockState(outerH))) {
                    obstructions.add(outerH.asLong());
                }
            }
        });
        return mergeHorizontalRuns(obstructions, currentY);
    }

    private Map<Long, Long> currentOuterHStandingPositions(Set<Long> actionPositions) {
        Map<Long, Long> result = new HashMap<>();
        for (long packedPos : actionPositions) {
            BlockPos outerH = BlockPos.of(packedPos);
            result.put(packedPos, guardStagingPosition(outerH, currentY).above().asLong());
        }
        return Map.copyOf(result);
    }

    private BlockPos outerHPosition(BlockPos guardPos, int y) {
        int x = Math.max(min.x, Math.min(max.x, guardPos.getX()));
        int z = Math.max(min.z, Math.min(max.z, guardPos.getZ()));
        return new BlockPos(x, y, z);
    }

    private BlockPos guardStagingPosition(BlockPos guardPos, int y) {
        int x = Math.max(min.x + 1, Math.min(max.x - 1, guardPos.getX()));
        int z = Math.max(min.z + 1, Math.min(max.z - 1, guardPos.getZ()));
        return new BlockPos(x, y, z);
    }

    private static List<LayerArea> mergeHorizontalRuns(Set<Long> positions, int y) {
        List<LayerArea> result = new ArrayList<>();
        positions.stream().map(BlockPos::of).collect(java.util.stream.Collectors.groupingBy(BlockPos::getZ))
                .entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
                    List<Integer> xs = entry.getValue().stream().map(BlockPos::getX).distinct().sorted().toList();
                    if (xs.isEmpty()) {
                        return;
                    }
                    int start = xs.getFirst();
                    int previous = start;
                    for (int i = 1; i < xs.size(); i++) {
                        int x = xs.get(i);
                        if (x != previous + 1) {
                            result.add(new LayerArea(start, previous, y, entry.getKey(), entry.getKey()));
                            start = x;
                        }
                        previous = x;
                    }
                    result.add(new LayerArea(start, previous, y, entry.getKey(), entry.getKey()));
                });
        return result;
    }

    private static void protect(List<LayerArea> areas) {
        forEachPosition(areas, pos -> PROTECTED_G.add(pos.asLong()));
    }

    private static boolean isWorkSpace(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    private static boolean isOpenForGuardPlacement(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty() && state.canBeReplaced();
    }

    private static boolean isSealed(BlockState state, BlockPos pos) {
        return state.getFluidState().isEmpty()
                && !state.isAir()
                && state.isCollisionShapeFullBlock(Utils.MC.level, pos);
    }

    private static void forEachPosition(List<LayerArea> areas, Consumer<BlockPos> consumer) {
        areas.forEach(area -> area.forEach(consumer));
    }

    private static boolean allPositionsMatch(List<LayerArea> areas, StatePredicate predicate) {
        for (LayerArea area : areas) {
            for (int x = area.minX; x <= area.maxX; x++) {
                for (int z = area.minZ; z <= area.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, area.y, z);
                    if (!predicate.test(Utils.MC.level.getBlockState(pos), pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onLostControl() {
        if (!BUILD_PROC.isActive()) {
            reset(false);
        }
    }

    @Override
    public void baritoneStopped(boolean canceled) {
        if (isActive()) {
            Waiter.wait(1, waiter -> reset(false));
        }
    }

    @Override
    public double priority() {
        return DEFAULT_PRIORITY - 1;
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    public static boolean activate() {
        return INSTANCE.start();
    }

    private boolean start() {
        if (isActive()) {
            message("alreadyStarted", ChatFormatting.RED);
            return false;
        }
        ISelection selection = SEL_MGR.getOnlySelection();
        if (selection == null) {
            message("noSelection", ChatFormatting.RED);
            return false;
        }
        BetterBlockPos selectionMin = selection.min();
        BetterBlockPos selectionMax = selection.max();
        if (selectionMax.x - selectionMin.x + 1 < 5 || selectionMax.z - selectionMin.z + 1 < 5) {
            message("selectionTooNarrow", ChatFormatting.RED);
            return false;
        }
        EnumSet<GuardSide> liquidGuardSides = findLiquidGuardSides(selectionMin, selectionMax);
        int firstLiquidY = findHighestLiquidLayer(selectionMin, selectionMax, liquidGuardSides);
        if (firstLiquidY == Integer.MIN_VALUE) {
            message("noLiquid", ChatFormatting.GOLD);
            return false;
        }
        Item replacement = LiquidReplacementHelper.findBestItem();
        if (replacement == null) {
            message("noUsableItem", ChatFormatting.RED);
            return false;
        }

        originalSelection = selection;
        min = selectionMin;
        max = selectionMax;
        fillerItem = replacement;
        currentY = firstLiquidY;
        activeGuardSides = liquidGuardSides;
        moveInsideGoal = new GoalInsideSelection(min.x + 2, max.x - 2, min.z + 2, max.z - 2);
        phase = Phase.INITIAL_T;
        phasePrepared = false;
        commandIssued = false;
        phaseAttempts = 0;
        stableTicks = 0;
        builderFailureRetries = 0;
        retryScheduled = false;
        PROTECTED_G.clear();
        OUTER_H_CLEAR_PATH_CONSTRAINT = null;
        resetGPlacementLoop();
        message("started", ChatFormatting.WHITE);
        return true;
    }

    private static EnumSet<GuardSide> findLiquidGuardSides(BetterBlockPos min, BetterBlockPos max) {
        EnumSet<GuardSide> result = EnumSet.noneOf(GuardSide.class);
        for (GuardSide side : GuardSide.values()) {
            LayerArea column = guardSideArea(side, min.y, min, max);
            sideScan: for (int y = min.y; y <= max.y; y++) {
                for (int x = column.minX; x <= column.maxX; x++) {
                    for (int z = column.minZ; z <= column.maxZ; z++) {
                        if (hasLiquid(x, y, z)) {
                            result.add(side);
                            break sideScan;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static int findHighestLiquidLayer(BetterBlockPos min, BetterBlockPos max,
            Set<GuardSide> activeGuardSides) {
        for (int y = max.y; y >= min.y; y--) {
            // Scan T first.
            for (int x = min.x; x <= max.x; x++) {
                for (int z = min.z; z <= max.z; z++) {
                    if (hasLiquid(x, y, z)) {
                        return y;
                    }
                }
            }

            // Scan only G sides that contained liquid somewhere in the full
            // selection height. Air never activates a side or a starting layer.
            for (GuardSide side : GuardSide.CLOCKWISE) {
                if (activeGuardSides.contains(side)) {
                    LayerArea area = guardSideArea(side, y, min, max);
                    for (int x = area.minX; x <= area.maxX; x++) {
                        for (int z = area.minZ; z <= area.maxZ; z++) {
                            if (hasLiquid(x, y, z)) {
                                return y;
                            }
                        }
                    }
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean hasLiquid(int x, int y, int z) {
        return !Utils.MC.level.getBlockState(new BlockPos(x, y, z)).getFluidState().isEmpty();
    }

    public static boolean handleBaritoneLog(String message) {
        return INSTANCE.handleBuilderFailure(message);
    }

    private boolean handleBuilderFailure(String message) {
        if (!isActive()) {
            return false;
        }
        boolean unreplaceableLiquid = message.equals("Unreplaceable liquids at at least:");
        boolean unableToBuild = message.equals("Unable to do it. Pausing. resume to resume, cancel to cancel");
        boolean missingMaterials = message.equals("Missing materials for at least:");
        if (!unreplaceableLiquid && !unableToBuild && !missingMaterials) {
            return false;
        }
        if (missingMaterials && Utils.MC.player.getInventory().countItem(fillerItem) == 0) {
            fail("outOfBlocks", currentY);
            return true;
        }
        if (retryScheduled) {
            return true;
        }
        builderFailureRetries++;
        if (builderFailureRetries > MAX_PHASE_ATTEMPTS) {
            fail("stalled", phase.name().toLowerCase(Locale.ROOT), currentY);
            return true;
        }
        retryScheduled = true;
        Waiter.wait(20, waiter -> {
            retryScheduled = false;
            if (isActive() && BUILD_PROC.isPaused()) {
                BUILD_PROC.resume();
            }
        });
        return true;
    }

    public static void resetForWorldChange() {
        INSTANCE.reset(false);
    }

    private void finish() {
        message("finished", ChatFormatting.GREEN);
        reset(true);
    }

    private void fail(String key, Object... args) {
        BTScreen.chatMessage(Component.translatable(TRANSLATABLE_PREFIX + key, args).withStyle(ChatFormatting.RED));
        reset(false);
        Utils.cancel();
    }

    private void reset(boolean completed) {
        boolean hadState = phase != Phase.IDLE || originalSelection != null;
        if (hadState) {
            SETTINGS.okIfWater.value = false;
        }
        restoreOriginalSelection();
        phase = Phase.IDLE;
        phaseAreas = List.of();
        requiredSolidPositions = Set.of();
        phaseCommand = null;
        phasePrepared = false;
        commandIssued = false;
        originalSelection = null;
        min = null;
        max = null;
        fillerItem = null;
        moveInsideGoal = null;
        activeGuardSides = EnumSet.noneOf(GuardSide.class);
        PROTECTED_G.clear();
        OUTER_H_CLEAR_PATH_CONSTRAINT = null;
        resetGPlacementLoop();
        if (hadState && !completed) {
            BTScreen.debugLog("smart_water_clear reset before completion");
        }
    }

    private void restoreOriginalSelection() {
        if (originalSelection == null) {
            return;
        }
        SEL_MGR.removeAllSelections();
        SEL_MGR.addSelection(originalSelection);
    }

    private static void message(String key, ChatFormatting formatting, Object... args) {
        BTScreen.chatMessage(Component.translatable(TRANSLATABLE_PREFIX + key, args).withStyle(formatting));
    }

    private enum Phase {
        IDLE,
        INITIAL_T,
        INITIAL_G,
        MOVE_INSIDE,
        CLEAR_PREVIOUS_H,
        CLEAR_CURRENT_OUTER_H,
        FILL_INNER_H,
        FILL_G,
        FILL_OUTER_H,
        FILL_T;

        boolean buildsGuard() {
            return this == INITIAL_G || this == FILL_G;
        }

        boolean clearsBlocks() {
            return this == CLEAR_PREVIOUS_H || this == CLEAR_CURRENT_OUTER_H;
        }

        boolean requiresCapturedSolidTargets() {
            return this == INITIAL_T || this == FILL_INNER_H || this == FILL_OUTER_H;
        }

        int requiredStableTicks() {
            return buildsGuard() || this == FILL_T ? STABLE_TICKS : 1;
        }
    }

    /** A horizontal goal covering every cell beyond the two-block H ring. */
    private record GoalInsideSelection(int minX, int maxX, int minZ, int maxZ) implements Goal {
        @Override
        public boolean isInGoal(int x, int y, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }

        @Override
        public double heuristic(int x, int y, int z) {
            int nearestX = Math.max(minX, Math.min(maxX, x));
            int nearestZ = Math.max(minZ, Math.min(maxZ, z));
            return GoalXZ.calculate(x - nearestX, z - nearestZ);
        }
    }

    private record ExactStandingPathConstraint(Map<Long, Long> standingByTarget) {
        Boolean allows(int targetX, int targetY, int targetZ, int standingX, int standingY, int standingZ) {
            Long requiredStanding = standingByTarget.get(BlockPos.asLong(targetX, targetY, targetZ));
            if (requiredStanding == null) {
                return null;
            }
            return requiredStanding == BlockPos.asLong(standingX, standingY, standingZ);
        }
    }

    private record GPlacementPathConstraint(long target, int standingX, int standingY, int standingZ) {
        boolean allows(int x, int y, int z) {
            return x == standingX && y == standingY && z == standingZ;
        }
    }

    private enum GuardSide {
        NORTH,
        EAST,
        SOUTH,
        WEST;

        private static final List<GuardSide> CLOCKWISE = List.of(NORTH, EAST, SOUTH, WEST);
    }

    private record LayerArea(int minX, int maxX, int y, int minZ, int maxZ) {
        BetterBlockPos min() {
            return new BetterBlockPos(minX, y, minZ);
        }

        BetterBlockPos max() {
            return new BetterBlockPos(maxX, y, maxZ);
        }

        void forEach(Consumer<BlockPos> consumer) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    consumer.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    @FunctionalInterface
    private interface StatePredicate {
        boolean test(BlockState state, BlockPos pos);
    }
}
