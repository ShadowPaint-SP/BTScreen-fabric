package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_EAT;
import static de.drvlabs.btscreen.config.Configs.Generic.FOOD_LEVEL;

import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class AutoEat extends BTProcessWithInitializer {
    // TODO: check if setting is correct
    public static final int MIN_FOOD_LEVEL = 21 - Registries.ITEM.stream().parallel()
            .map(i -> i.getComponents().get(DataComponentTypes.FOOD)).filter(i -> i != null)
            .mapToInt(FoodComponent::nutrition).max().orElse(20);
    private static boolean shouldEat = false;

    @Override
    public boolean isActive() {
        return isActive(AUTO_EAT) && shouldEat;
    }

    @Override
    protected void onInitialize() {
        BTScreen.chatMessage(Text.literal("Started Eating"));
    }

    @Override
    protected PathingCommand onTick() {
        FoodComponent food = Utils.MC.player.getOffHandStack().get(DataComponentTypes.FOOD);
        if (food == null) {
            return new PathingCommand(null, PathingCommandType.DEFER);
        }
        doItemUse();
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    @Override
    protected void onReset() {
        shouldEat = false;
    }

    @Override
    public double priority() {
        return super.priority() + 0.01;
    }

    public static void onSetFoodLevel(int foodLevel) {
        shouldEat = foodLevel < FOOD_LEVEL.getIntegerValue();
        if (!shouldEat) {
            FoodComponent food = Utils.MC.player.getOffHandStack().get(DataComponentTypes.FOOD);
            if (food != null) {
                shouldEat = (foodLevel + food.nutrition()) <= 20;
            } else if (isActive(AUTO_EAT)) {
                BTScreen.chatMessage(Text.literal("Error: No food in offhand!").formatted(Formatting.RED));
            }
        }
        if (isActive(AUTO_EAT) && !shouldEat) {
            BTScreen.chatMessage(Text.literal("Finished Eating"));
        }
    }

    private static void doItemUse() {
        if (!Utils.MC.player.isUsingItem()) {
            // Utils.MC.doItemUse(); // accesswidener:
            // accessible method net/minecraft/client/MinecraftClient doItemUse ()V
            if (Utils.MC.interactionManager.interactItem(Utils.MC.player, Hand.OFF_HAND) instanceof Success success) {
                if (success.swingSource() == SwingSource.CLIENT) {
                    Utils.MC.player.swingHand(Hand.OFF_HAND);
                }
                return;
            }
        }
    }
}
