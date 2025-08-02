package moe.yahvk.tfc_create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FanProcessing.class)
public class FanProcessingMixin {
    @Inject(method = "applyProcessing(Lnet/minecraft/world/entity/item/ItemEntity;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private static void applyProcessing(ItemEntity entity, FanProcessingType type, CallbackInfoReturnable<Boolean> cir, @Local(name = "entityIn") ItemEntity entityIn) {
        CompoundTag nbt = entityIn.getPersistentData();

        if (!nbt.contains("CreateData")) {
            nbt.put("CreateData", new CompoundTag());
        } else {
            return;
        }

        CompoundTag nbtOrigin = entity.getPersistentData();
        if (!nbtOrigin.contains("CreateData")) {
            return;
        }
        CompoundTag createDataOrigin = nbtOrigin.getCompound("CreateData");
        if (!createDataOrigin.contains("Processing")) {
            return;
        }
        nbt.getCompound("CreateData").put("Processing", createDataOrigin.getCompound("Processing"));
    }
}
