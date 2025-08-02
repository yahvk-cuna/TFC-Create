package moe.yahvk.tfc_create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import moe.yahvk.tfc_create.config.ClientConfig;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.create.HeatableContainerBehaviour;
import net.createmod.catnip.lang.LangBuilder;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

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

    @ModifyArg(method = "addBehaviours",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/fluid/SmartFluidTankBehaviour;<init>(Lcom/simibubi/create/foundation/blockEntity/behaviour/BehaviourType;Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;IIZ)V"),
            index = 2)
    public int changeTankNumber(BehaviourType<SmartFluidTankBehaviour> type, SmartBlockEntity be, int tanks,
                                int tankCapacity, boolean enforceVariety) {
        if (type == SmartFluidTankBehaviour.INPUT) {
            return CommonConfig.basinInputTanks.get();
        } else if (type == SmartFluidTankBehaviour.OUTPUT) {
            return CommonConfig.basinOutputTanks.get();
        } else {
            return tanks;
        }
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
