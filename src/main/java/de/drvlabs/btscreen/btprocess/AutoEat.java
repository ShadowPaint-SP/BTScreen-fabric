package de.drvlabs.btscreen.btprocess;

import static de.drvlabs.btscreen.config.Configs.Generic.AUTO_EAT;
import static de.drvlabs.btscreen.config.Configs.Generic.FOOD_LEVEL;

import baritone.api.process.PathingCommand;
import de.drvlabs.btscreen.BTScreen;
import de.drvlabs.btscreen.config.LangKeys;
import de.drvlabs.btscreen.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.food.FoodProperties;

public final class AutoEat extends BTProcessWithInitializer {
    public static final AutoEat INSTANCE = new AutoEat();

    private AutoEat() {
    }

    public static final int MIN_FOOD_LEVEL = 1;
    private static boolean shouldEat = false;

    @Override
    public boolean isActive() {
        return isActive(AUTO_EAT) && shouldEat;
    }

    @Override
    protected void onInitialize() {
    }

    @Override
    protected PathingCommand onTick() {
        FoodProperties food = Utils.MC.player.getOffhandItem().get(DataComponents.FOOD);
        if (food == null) {
            return DEFER;
        }
        doItemUse();
        return REQUEST_PAUSE;
    }

    @Override
    protected void onReset() {
        shouldEat = false;
    }

    @Override
    public double priority() {
        return super.priority() + 0.02;
    }

    public static void onSetFoodLevel(int foodLevel) {
        boolean oldShouldEat = shouldEat;
        shouldEat = foodLevel < FOOD_LEVEL.getIntegerValue();
        if (!shouldEat) {
            FoodProperties food = Utils.MC.player.getOffhandItem().get(DataComponents.FOOD);
            if (food != null) {
                shouldEat = (foodLevel + food.nutrition()) <= 20;
            } else if (isActive(AUTO_EAT)) {
                BTScreen.chatMessage(Component.translatable(LangKeys.INFO + ".autoEat.noFood").withStyle(ChatFormatting.RED));
            }
        }
        if (isActive(AUTO_EAT) && !shouldEat && oldShouldEat) {
            Utils.MC.options.keyUse.setDown(false);
        }
    }

    private static void doItemUse() {
        if (!Utils.MC.player.isUsingItem()) {
            // Utils.MC.doItemUse(); // accesswidener:
            // accessible method net/minecraft/client/Minecraft startUseItem ()V
            Utils.MC.options.keyUse.setDown(true);
            if (Utils.MC.gameMode.useItem(Utils.MC.player, InteractionHand.OFF_HAND) instanceof Success success) {
                if (success.swingSource() == SwingSource.CLIENT) {
                    Utils.MC.player.swing(InteractionHand.OFF_HAND);
                }
                return;
            }
        }
    }
}
