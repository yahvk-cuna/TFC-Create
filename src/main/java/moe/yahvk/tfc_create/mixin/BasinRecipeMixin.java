package moe.yahvk.tfc_create.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.recipe.ITemperatureRecipe;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
