package moe.yahvk.tfc_create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import moe.yahvk.tfc_create.config.ClientConfig;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.config.Config;
import moe.yahvk.tfc_create.create.HeatableContainerBehaviour;
import net.createmod.catnip.lang.LangBuilder;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.TemperatureDisplayStyle;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILHARD;
import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.PRINT;

@Mixin(value = BasinBlockEntity.class, remap = false)
public class BasinBlockEntityMixin {
    @Unique
    HeatableContainerBehaviour tfccreate$heatableContainerBehaviour;

    @Inject(method = "addBehaviours", at = @At("HEAD"))
    public void addBehaviours(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        if (!CommonConfig.basinHeatable.get()) {
            return;
        }
        this.tfccreate$heatableContainerBehaviour = new HeatableContainerBehaviour((BasinBlockEntity) (Object) this);
        behaviours.add(this.tfccreate$heatableContainerBehaviour);
    }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    public <T> void getCapability(Capability<T> cap, Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (!CommonConfig.basinHeatable.get()) {
            return;
        }
        if (cap == HeatCapability.BLOCK_CAPABILITY) {
            cir.setReturnValue(LazyOptional.of(() -> tfccreate$heatableContainerBehaviour).cast());
        }
    }

    @Redirect(method = "addToGoggleTooltip",
            at = @At(
                    value = "INVOKE", target = "Lnet/createmod/catnip/lang/LangBuilder;forGoggles(Ljava/util/List;I)V", ordinal = 0
            ),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    )
    public void addItemHeatToGoggle(LangBuilder instance, List<? super MutableComponent> tooltip, int indents, @Local(ordinal = 0) ItemStack stackInSlot) {
        if (!ClientConfig.showItemHeatInBasin.get()) {
            instance.forGoggles(tooltip, indents);
            return;
        }
        final var heat = HeatCapability.get(stackInSlot);
        if (heat != null) {
            var tips = new ArrayList<Component>();
            heat.addTooltipInfo(stackInSlot, tips);
                for (Component tip : tips) {
                    instance.add(CreateLang.text("  ")).add(tip);
                }
        }
        instance.forGoggles(tooltip, indents);
    }

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"))
    public void addBasinHeat(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (tfccreate$heatableContainerBehaviour == null) {
            return;
        }

        final var temperature = tfccreate$heatableContainerBehaviour.getTemperature();

        var temp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temperature);
        if (temp != null) {
            CreateLang.text("").add(temp).forGoggles(tooltip);
        }
    }
}
