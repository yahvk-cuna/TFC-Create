package moe.yahvk.tfc_create.mixin;

import moe.yahvk.tfc_create.config.CommonConfig;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes.BlastingType.class, remap = false)
public abstract class BlastingTypeMixin {
    @Inject(method = "canProcess", at = @At("HEAD"), cancellable = true)
    private void canProcess(ItemStack stack, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!CommonConfig.fanHeatItem.get()) {
            return;
        }

        if (stack.getCapability(HeatCapability.CAPABILITY).isPresent()) {
            cir.setReturnValue(true);
        }
    }
}
