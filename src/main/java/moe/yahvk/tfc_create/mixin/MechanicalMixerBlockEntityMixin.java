package moe.yahvk.tfc_create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import moe.yahvk.tfc_create.TFCCreate;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.recipe.AlloyMixerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MechanicalMixerBlockEntity.class, remap = false)
public abstract class MechanicalMixerBlockEntityMixin extends BasinOperatingBlockEntity {
    public MechanicalMixerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Inject(method = "getMatchingRecipes",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;)Lnet/minecraftforge/common/util/LazyOptional;"))
    private void getMatchingRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir, @Local List<Recipe<?>> matchingRecipes, @Local BasinBlockEntity basinBlockEntity) {
        if (!CommonConfig.alloyingByMixer.get()) {
            return;
        }
        assert level != null;

        var handler = basinBlockEntity
                .getCapability(ForgeCapabilities.FLUID_HANDLER)
                .resolve();
        if (handler.isEmpty())
            return;
        var availableFluid = handler.get();

        List<FluidStack> fluids = new ArrayList<>();
        for (int i = 0; i < availableFluid.getTanks(); i++) {
            FluidStack fluid = availableFluid.getFluidInTank(i);
            if (!fluid.isEmpty()) {
                fluids.add(fluid);
            }
        }

        var alloyRecipe = AlloyMixerRecipe.findRecipes(fluids, level.getRecipeManager());
        alloyRecipe.ifPresent(matchingRecipes::add);
    }
}
