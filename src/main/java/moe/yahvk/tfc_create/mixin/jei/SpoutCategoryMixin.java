package moe.yahvk.tfc_create.mixin.jei;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.SpoutCategory;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.createmod.catnip.platform.CatnipServices;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.util.Metal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.dries007.tfc.common.items.TFCItems.*;

@Mixin(value = SpoutCategory.class, remap = false)
public class SpoutCategoryMixin {
    @Inject(method = "consumeRecipes(Ljava/util/function/Consumer;Lmezz/jei/api/runtime/IIngredientManager;)V",
            at = @At("HEAD"))
    private static void consumeRecipes(Consumer<FillingRecipe> consumer, IIngredientManager ingredientManager, CallbackInfo ci) {
        Collection<FluidStack> fluidStacks = ingredientManager.getAllIngredients(ForgeTypes.FLUID_STACK);

        Stream.concat(MOLDS.values().stream(), Stream.of(BELL_MOLD, FIRE_INGOT_MOLD)).map(RegistryObject::get).forEach(item -> {
            Optional<IFluidHandlerItem> capability =
                    item.getDefaultInstance().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
            if (capability.isEmpty())
                return;
            var fhi = capability.get();

            for (FluidStack fluidStack : fluidStacks) {
                if (!fhi.isFluidValid(0, fluidStack)) {
                    continue;
                }
                FluidStack fluidCopy = fluidStack.copy();
                fluidCopy.setAmount(fhi.getTankCapacity(0));
                fhi.fill(fluidCopy, IFluidHandler.FluidAction.EXECUTE);

                ItemStack container = fhi.getContainer();
                Ingredient bucket = Ingredient.of(item);

                ResourceLocation itemName = CatnipServices.REGISTRIES.getKeyOrThrow(item);
                ResourceLocation fluidName = CatnipServices.REGISTRIES.getKeyOrThrow(fluidCopy.getFluid());
                consumer.accept(new ProcessingRecipeBuilder<>(FillingRecipe::new,
                        Create.asResource("fill_" + itemName.getNamespace() + "_" + itemName.getPath()
                                + "_with_" + fluidName.getNamespace() + "_" + fluidName.getPath()))
                        .withItemIngredients(bucket)
                        .withFluidIngredients(FluidIngredient.fromFluidStack(fluidCopy))
                        .withSingleItemOutput(container)
                        .build());
            }
        });
    }

    @Inject(method = "draw(Lcom/simibubi/create/content/fluids/transfer/FillingRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At("HEAD"))
    private void draw(FillingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY, CallbackInfo ci) {
        var fluid = recipe.getRequiredFluid().getMatchingFluidStacks().get(0).getFluid();
        var metal = Metal.get(fluid);
        if (metal == null) return;
        for (IRecipeSlotView view : iRecipeSlotsView.getSlotViews(RecipeIngredientRole.OUTPUT)) {
            view.getDisplayedItemStack()
                    .ifPresent(stack -> HeatCapability.setTemperature(stack, metal.getMeltTemperature()));
        }
    }
}
