package de.drvlabs.btscreen.implementation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.drvlabs.btscreen.config.Configs;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class LiquidReplacementHelper {
    private static final Set<Item> POSSIBLE_BLOCKS = Configs.Lists.BLOCKS_TO_REPLACE_LIQUID_WITH.getStrings().stream()
            .map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .map(BuiltInRegistries.ITEM::getValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    private LiquidReplacementHelper() {
    }

    public static Item findBestItem() {
        Item bestItem = null;
        int maxCount = 0;
        for (Item item : POSSIBLE_BLOCKS) {
            int count = Utils.MC.player.getInventory().countItem(item);
            if (count > maxCount) {
                maxCount = count;
                bestItem = item;
            }
        }
        return bestItem;
    }

    public static String createLiquidReplaceCommand(Item replacement) {
        Set<String> selectors = new LinkedHashSet<>();
        BuiltInRegistries.BLOCK.stream()
                .filter(block -> block instanceof BucketPickup || block instanceof LiquidBlockContainer)
                .map(LiquidReplacementHelper::selectorForLiquidBlock)
                .forEach(selectors::add);
        return createReplaceCommand(replacement, selectors);
    }

    public static String createReplaceCommand(Item replacement, Collection<String> selectors) {
        StringBuilder command = new StringBuilder("sel replace");
        selectors.forEach(selector -> command.append(' ').append(selector));
        Identifier replacementId = BuiltInRegistries.ITEM.getKey(replacement);
        command.append(' ').append(replacementId);
        return command.toString();
    }

    public static String selectorFor(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED)) {
            return id + "[waterlogged=true]";
        }
        return id.toString();
    }

    private static String selectorForLiquidBlock(Block block) {
        String selector = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (block instanceof SimpleWaterloggedBlock) {
            selector += "[waterlogged=true]";
        }
        return selector;
    }

}
