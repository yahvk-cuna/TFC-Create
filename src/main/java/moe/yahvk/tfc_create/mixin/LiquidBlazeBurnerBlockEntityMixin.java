package moe.yahvk.tfc_create.mixin;

import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.create.BlazeBurnerHeatBehaviour;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = LiquidBlazeBurnerBlockEntity.class, remap = false)
public abstract class LiquidBlazeBurnerBlockEntityMixin extends SmartBlockEntity {
    @Unique
    BlazeBurnerHeatBehaviour tfccreate$blazeBurnerHeatBehaviour;

    public LiquidBlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "addBehaviours", at = @At("HEAD"))
    public void addBehaviours(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        if (!CommonConfig.basinHeatable.get()) {
            return;
        }
        this.tfccreate$blazeBurnerHeatBehaviour = new BlazeBurnerHeatBehaviour(this);
        behaviours.add(this.tfccreate$blazeBurnerHeatBehaviour);
    }

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    public <T> void getCapability(Capability<T> cap, Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (cap == HeatCapability.BLOCK_CAPABILITY) {
            cir.setReturnValue(LazyOptional.of(() -> tfccreate$blazeBurnerHeatBehaviour).cast());
        }
    }
}
