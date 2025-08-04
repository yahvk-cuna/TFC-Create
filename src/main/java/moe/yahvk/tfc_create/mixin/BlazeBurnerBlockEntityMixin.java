package moe.yahvk.tfc_create.mixin;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity {
    @Unique
    BlazeBurnerHeatBehaviour tfccreate$blazeBurnerHeatBehaviour;

    public BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "addBehaviours", at = @At("HEAD"))
    public void addBehaviours(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        if (!CommonConfig.basinHeatable.get()) {
            return;
        }
        this.tfccreate$blazeBurnerHeatBehaviour = new BlazeBurnerHeatBehaviour((BlazeBurnerBlockEntity) (Object) this);
        behaviours.add(this.tfccreate$blazeBurnerHeatBehaviour);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == HeatCapability.BLOCK_CAPABILITY) {
            return LazyOptional.of(() -> tfccreate$blazeBurnerHeatBehaviour).cast();
        }
        return super.getCapability(cap, side);
    }
}
