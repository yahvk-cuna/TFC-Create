package moe.yahvk.tfc_create.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import moe.yahvk.tfc_create.TFCCreate;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.recipe.ITemperatureRecipe;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeatBlock;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasinRecipe.class, remap = false)
public class BasinRecipeMixin {
    @Inject(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void checkTemperature(BasinBlockEntity basin, Recipe<?> recipe, boolean test, CallbackInfoReturnable<Boolean> cir) {
        if (!CommonConfig.alloyingRequireHot.get()) {
            return;
        }
        if (recipe instanceof ITemperatureRecipe tempRecipe) {
            var cold = basin.getCapability(HeatCapability.BLOCK_CAPABILITY)
                    .map(heat -> heat.getTemperature() < tempRecipe.getRequiredTemperature())
                    .orElse(true);
            if (cold) {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/recipe/HeatCondition;testBlazeBurner(Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlock$HeatLevel;)Z"))
    private static boolean temperatureHeat(HeatCondition instance, BlazeBurnerBlock.HeatLevel level, BasinBlockEntity basin, Recipe<?> recipe, boolean test) {
        float temp = basin.getCapability(HeatCapability.BLOCK_CAPABILITY)
                .map(IHeatBlock::getTemperature).orElse(0f);
        if (instance == HeatCondition.NONE) {
            return true;
        } else if (instance == HeatCondition.HEATED && temp >= CommonConfig.temperatureToHeated.get()) {
            return true;
        } else if (instance == HeatCondition.SUPERHEATED && temp >= CommonConfig.temperatureToSuperHeated.get()) {
            return true;
        }
        if (CommonConfig.temperatureOnly.get()) {
            return instance.testBlazeBurner(level);
        } else {
            return false;
        }
    }
}
