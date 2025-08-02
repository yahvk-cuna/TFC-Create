package moe.yahvk.tfc_create.recipe;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import moe.yahvk.tfc_create.TFCCreate;
import net.dries007.tfc.common.recipes.AlloyRecipe;
import net.dries007.tfc.common.recipes.inventory.AlloyInventory;
import net.dries007.tfc.util.Alloy;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public class AlloyMixerRecipe extends BasinRecipe implements ITemperatureRecipe {
    private float temperature;

    public AlloyMixerRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(AllRecipeTypes.MIXING, params);
        temperature = 0;
    }

    public static Optional<AlloyMixerRecipe> findRecipes(List<FluidStack> fluids, RecipeManager recipeManager) {
        var metals = fluids.stream()
                .map(fluidStack -> Pair.of(Metal.get(fluidStack.getFluid()), fluidStack.getAmount()))
                .filter(pair -> pair.getFirst() != null && pair.getSecond() > 0)
                .toList();
        if (metals.size() < 2) {
            return Optional.empty();
        }
        var alloy = new Alloy();
        StringBuilder suffix = new StringBuilder();
        for (var metal : metals) {
            alloy.add(metal.getFirst(), metal.getSecond(), false);
            suffix.append("_").append(metal.getFirst().getId().getPath()).append(metal.getSecond());
        }
        var alloyRecipe = AlloyRecipe.get(recipeManager, new AlloyInventory(alloy));
        return alloyRecipe.map(recipe -> {
            var builder = new ProcessingRecipeBuilder<>(AlloyMixerRecipe::new, recipe.getId().withSuffix(suffix.toString()));

            for (var metal : metals) {
                builder.require(metal.getFirst().getFluid(), metal.getSecond());
            }
            builder.output(recipe.getResult().getFluid(), alloy.getAmount());

            var mix = builder.build();
            mix.temperature = recipe.getResult().getMeltTemperature();
            return mix;
        });
    }

    @Override
    public float getRequiredTemperature() {
        return temperature;
    }
}
