package moe.yahvk.tfc_create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GenericItemFilling.class, remap = false)
public class GenericItemFillingMixin {
    @Inject(method = "fillItem", at = @At("TAIL"), cancellable = true)
    private static void fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 1) FluidStack toFill) {
        var result = cir.getReturnValue();
        var heat = HeatCapability.get(result);
        if (heat == null) {
            return;
        }
        var metal = Metal.get(toFill.getFluid());
        if (metal != null) {
            heat.setTemperature(metal.getMeltTemperature());
            cir.setReturnValue(result);
        }
    }

    @Redirect(method = "canItemBeFilled",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"))
    private static <T> LazyOptional<T> canItemBeFilled(ItemStack instance, Capability<T> capability) {
        return instance.copyWithCount(1).getCapability(capability);
    }

    @Redirect(method = "getRequiredAmountForItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"))
    private static <T> LazyOptional<T> getRequiredAmountForItem(ItemStack instance, Capability<T> capability) {
        return instance.copyWithCount(1).getCapability(capability);
    }
}
