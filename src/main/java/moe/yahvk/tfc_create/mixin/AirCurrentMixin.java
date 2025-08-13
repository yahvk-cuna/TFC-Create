package moe.yahvk.tfc_create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import moe.yahvk.tfc_create.create.FanHeating;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = com.simibubi.create.content.kinetics.fan.AirCurrent.class, remap = false)
public class AirCurrentMixin {
    @Inject(method = "tickAffectedEntities",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessing;canProcess(Lnet/minecraft/world/entity/item/ItemEntity;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Z"))
    private void airTemperature(Level world, CallbackInfo ci, @Local FanProcessingType type, @Local ItemEntity entity) {
        FanHeating.heat(entity.getItem(), type, world, entity.blockPosition());
    }

    @Inject(method = "lambda$tickAffectedHandlers$2",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessing;applyProcessing(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;"))
    private void airTemperature(Level world, FanProcessingType processingType, TransportedItemStackHandlerBehaviour handler, TransportedItemStack transported, CallbackInfoReturnable<TransportedItemStackHandlerBehaviour.TransportedResult> cir) {
        FanHeating.heat(transported.stack, processingType, world, handler.getPos());
    }
}
