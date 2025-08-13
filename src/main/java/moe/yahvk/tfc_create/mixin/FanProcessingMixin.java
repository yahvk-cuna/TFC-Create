package moe.yahvk.tfc_create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.controller.TransportedItemStackController;
import moe.yahvk.tfc_create.create.FanHeating;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = FanProcessing.class, remap = false)
public class FanProcessingMixin {
    @Inject(method = "applyProcessing(Lnet/minecraft/world/entity/item/ItemEntity;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"),
            remap = true)
    private static void fixProcessingTime(ItemEntity entity, FanProcessingType type, CallbackInfoReturnable<Boolean> cir, @Local(name = "entityIn") ItemEntity entityIn) {
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

    // This only handle heating recipe, temperature handled by {@link AirCurrentMixin}
    @Inject(method = "applyProcessing(Lnet/minecraft/world/entity/item/ItemEntity;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void onApplyProcessing(ItemEntity entity, FanProcessingType type, CallbackInfoReturnable<Boolean> cir) {
        if (!CommonConfig.fanHeatItem.get()) {
            return;
        }

        if (type != AllFanProcessingTypes.BLASTING && type != AllFanProcessingTypes.SMOKING) {
            return;
        }

        var heat = HeatCapability.get(entity.getItem());
        if (heat == null) {
            return; // ignore if the item does not have heat capability
        }

        CompoundTag nbt = entity.getPersistentData();

        if (!nbt.contains("TFCCreateData"))
            nbt.put("TFCCreateData", new CompoundTag());
        CompoundTag createData = nbt.getCompound("TFCCreateData");

        if (!createData.contains("RecipeTemperature") ||
                heat.getTemperature() < createData.getFloat("RecipeTemperature") // check cache
        ) {
            if (CommonConfig.fanProcessHighPriority.get() && (
                    (type == AllFanProcessingTypes.BLASTING && FanHeating.hasBlastingRecipe(entity.getItem(), entity.level())) ||
                            (type == AllFanProcessingTypes.SMOKING && FanHeating.hasSmokingRecipe(entity.getItem(), entity.level()))
            )) {
                createData.putFloat("RecipeTemperature", -1);
                return;
            }

            var recipe = HeatingRecipe.getRecipe(entity.getItem());
            if (!FanHeating.isValidRecipe(recipe, type)) {
                createData.putFloat("RecipeTemperature", -1);
                return;
            }
            createData.putFloat("RecipeTemperature", recipe.getTemperature());
        }
        float recipeTemperature = createData.getFloat("RecipeTemperature");
        if (recipeTemperature == -1) {
            return;
        }

        if (heat.getTemperature() < recipeTemperature) {
            cir.setReturnValue(false);
            return;
        }

        var recipe = HeatingRecipe.getRecipe(entity.getItem());
        assert recipe != null;

        // assemble result
        var stacks = FanHeating.process(entity.getItem(), recipe, type, entity.level());

        if (stacks.isEmpty()) {
            entity.discard();
            cir.setReturnValue(false);
            return;
        }

        FanHeating.setProcessDone(entity, type);

        entity.setItem(stacks.remove(0));
        for (ItemStack additional : stacks) {
            ItemEntity entityIn = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), additional);
            entityIn.setDeltaMovement(entity.getDeltaMovement());
            FanHeating.setProcessDone(entity, type);
            entity.level().addFreshEntity(entityIn);
        }
        cir.setReturnValue(true);
    }

    // This only handle heating recipe, temperature handled by {@link AirCurrentMixin}
    @Inject(method = "applyProcessing(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/kinetics/fan/processing/FanProcessingType;)Lcom/simibubi/create/content/kinetics/belt/behaviour/TransportedItemStackHandlerBehaviour$TransportedResult;",
            at = @At("HEAD"), cancellable = true)
    private static void onApplyProcessingTransported(TransportedItemStack transported, Level world, FanProcessingType type, CallbackInfoReturnable<TransportedItemStackHandlerBehaviour.TransportedResult> cir) {
        if (!CommonConfig.fanHeatItem.get()) {
            return;
        }

        if (type != AllFanProcessingTypes.BLASTING && type != AllFanProcessingTypes.SMOKING) {
            return;
        }

        var heat = HeatCapability.get(transported.stack);
        if (heat == null) {
            return; // ignore if the item does not have heat capability
        }

        var controller = (TransportedItemStackController) transported;

        if (!controller.tfc_create$initialized()) {
            if (CommonConfig.fanProcessHighPriority.get() && (
                    (type == AllFanProcessingTypes.BLASTING && FanHeating.hasBlastingRecipe(transported.stack, world)) ||
                            (type == AllFanProcessingTypes.SMOKING && FanHeating.hasSmokingRecipe(transported.stack, world))
            )) {
                controller.tfc_create$setCacheRecipe(null);
                return;
            }

            var recipe = HeatingRecipe.getRecipe(transported.stack);
            if (!FanHeating.isValidRecipe(recipe, type)) {
                controller.tfc_create$setCacheRecipe(null);
                return;
            }
            controller.tfc_create$setCacheRecipe(recipe);
        }
        var recipe = controller.tfc_create$getCacheRecipe();
        if (recipe == null) {
            return;
        }

        if (!recipe.isValidTemperature(heat.getTemperature())) {
            cir.setReturnValue(TransportedItemStackHandlerBehaviour.TransportedResult.doNothing());
            return;
        }

        // assemble result
        var stacks = FanHeating.process(transported.stack, recipe, type, world);

        transported.processingTime = -1;

        List<TransportedItemStack> transportedStacks = new ArrayList<>();
        for (ItemStack additional : stacks) {
            TransportedItemStack newTransported = transported.getSimilar();
            newTransported.stack = additional.copy();
            transportedStacks.add(newTransported);
        }
        cir.setReturnValue(TransportedItemStackHandlerBehaviour.TransportedResult.convertTo(transportedStacks));
    }
}
