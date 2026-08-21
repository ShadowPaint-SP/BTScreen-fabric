package de.drvlabs.btscreen.implementation;

import java.util.ArrayList;
import java.util.List;

import baritone.api.selection.ISelection;
import baritone.api.selection.ISelectionManager;
import baritone.api.utils.BetterBlockPos;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.utils.Utils;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message.MessageType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public final class Caveman {
    private static final String TRANSLATABLE_PREFIX = BTScreen.MOD_ID + ".caveman.";
    private static final ISelectionManager SEL_MGR = Utils.BT.getSelectionManager();

    private Caveman() {
    }

    public static void clipSelections(GuiBase gui) {
        ISelection[] selections = SEL_MGR.getSelections();
        if (selections.length == 0) {
            gui.addMessage(MessageType.ERROR, 1000, TRANSLATABLE_PREFIX + "noSelection");
            return;
        }

        List<ChunkArea> areas = new ArrayList<>();
        for (ISelection selection : selections) {
            if (!addChunkAreas(selection, areas, gui)) {
                return;
            }
        }
        if (areas.isEmpty()) {
            gui.addMessage(MessageType.WARNING, 1000, TRANSLATABLE_PREFIX + "nothingToClip");
            return;
        }

        SEL_MGR.removeAllSelections();
        areas.forEach(area -> SEL_MGR.addSelection(area.min(), area.max()));
        gui.addMessage(MessageType.SUCCESS, 1000, TRANSLATABLE_PREFIX + "clipped", areas.size());
    }

    private static boolean addChunkAreas(ISelection selection, List<ChunkArea> areas, GuiBase gui) {
        BetterBlockPos min = selection.min();
        BetterBlockPos max = selection.max();
        int minChunkX = SectionPos.blockToSectionCoord(min.x);
        int maxChunkX = SectionPos.blockToSectionCoord(max.x);
        int minChunkZ = SectionPos.blockToSectionCoord(min.z);
        int maxChunkZ = SectionPos.blockToSectionCoord(max.z);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!Utils.MC.level.hasChunk(chunkX, chunkZ)) {
                    gui.addMessage(MessageType.ERROR, 1000, TRANSLATABLE_PREFIX + "chunkNotLoaded",
                            chunkX, chunkZ);
                    return false;
                }

                ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                int maxY = maxMineY(Utils.MC.level.getChunk(chunkX, chunkZ));
                if (maxY < min.y) {
                    continue;
                }
                areas.add(new ChunkArea(
                        Math.max(min.x, chunkPos.getMinBlockX()),
                        Math.min(max.x, chunkPos.getMaxBlockX()),
                        min.y,
                        Math.min(max.y, maxY),
                        Math.max(min.z, chunkPos.getMinBlockZ()),
                        Math.min(max.z, chunkPos.getMaxBlockZ())));
            }
        }
        return true;
    }

    private static int maxMineY(LevelChunk chunk) {
        int lowestSurfaceY = Integer.MAX_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                if (surfaceY >= chunk.getMinY()) {
                    lowestSurfaceY = Math.min(lowestSurfaceY, surfaceY);
                }
            }
        }
        if (lowestSurfaceY == Integer.MAX_VALUE) {
            return Integer.MIN_VALUE;
        }
        return SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(lowestSurfaceY)) - 1;
    }

    private record ChunkArea(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        private BetterBlockPos min() {
            return new BetterBlockPos(minX, minY, minZ);
        }

        private BetterBlockPos max() {
            return new BetterBlockPos(maxX, maxY, maxZ);
        }
    }
}
