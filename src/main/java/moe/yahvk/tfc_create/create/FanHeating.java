package moe.yahvk.tfc_create.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.item.ItemHelper;
import moe.yahvk.tfc_create.config.CommonConfig;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Optional;

public class FanHeating {
    private static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));

    public static void setProcessDone(ItemEntity entity, FanProcessingType type) {
        CompoundTag nbt = entity.getPersistentData();

        if (!nbt.contains("CreateData"))
            nbt.put("CreateData", new CompoundTag());
        CompoundTag createData = nbt.getCompound("CreateData");

        if (!createData.contains("Processing"))
            createData.put("Processing", new CompoundTag());
        CompoundTag processing = createData.getCompound("Processing");

        ResourceLocation key = CreateBuiltInRegistries.FAN_PROCESSING_TYPE.getKey(type);
        if (key == null)
            throw new IllegalArgumentException("Could not get id for FanProcessingType " + type + "!");

        processing.putString("Type", key.toString());

        processing.putInt("Time", -1);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidRecipe(HeatingRecipe recipe, FanProcessingType type) {
        if (recipe == null || type == null) {
            return false;
        }

        if (type == AllFanProcessingTypes.BLASTING) {
            return recipe.isValidTemperature(CommonConfig.blastingTemperature.get());
        } else if (type == AllFanProcessingTypes.SMOKING) {
            return recipe.isValidTemperature(CommonConfig.smokingTemperature.get());
        }

        return false;
    }

    public static boolean hasBlastingRecipe(ItemStack stack, Level level) {
        RECIPE_WRAPPER.setItem(0, stack);
        Optional<SmeltingRecipe> smeltingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, RECIPE_WRAPPER, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        if (smeltingRecipe.isPresent())
            return true;

        RECIPE_WRAPPER.setItem(0, stack);
        Optional<BlastingRecipe> blastingRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.BLASTING, RECIPE_WRAPPER, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        return blastingRecipe.isPresent();
    }

    public static boolean hasSmokingRecipe(ItemStack stack, Level level) {
        RECIPE_WRAPPER.setItem(0, stack);
        Optional<SmokingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

        return recipe.isPresent();
    }

    public static List<ItemStack> process(final ItemStack input, final HeatingRecipe recipe, final FanProcessingType type, final Level level) {
        final ItemStackInventory inventory = new ItemStackInventory(input);
        ItemStack out = recipe.assemble(inventory, level.registryAccess());
        if (type == AllFanProcessingTypes.BLASTING) {
            FoodCapability.applyTrait(out, FoodTraits.BURNT_TO_A_CRISP);
        }
        return ItemHelper.multipliedOutput(input, out);
    }

    public static void heat(ItemStack stack, FanProcessingType type, Level world, BlockPos pos) {
        if (!CommonConfig.fanHeatItem.get()) {
            return;
        }

        var heat = HeatCapability.get(stack);
        if (heat == null) {
            return;
        }

        if (type == AllFanProcessingTypes.BLASTING) {
            HeatCapability.addTemp(heat, CommonConfig.blastingTemperature.get(), CommonConfig.blastingMultiplier.get().floatValue());
        } else if (type == AllFanProcessingTypes.SMOKING) {
            HeatCapability.addTemp(heat, CommonConfig.smokingTemperature.get(), CommonConfig.smokingMultiplier.get().floatValue());
        } else if (type == AllFanProcessingTypes.SPLASHING) {
            if (heat.getTemperature() > 0 && CommonConfig.splashingCoolAmount.get() > 0) {
                if (!world.isClientSide()) {
                    Helpers.playSound(world, pos, TFCSounds.ITEM_COOL.get());
                }
                heat.setTemperature(Math.max(0f, heat.getTemperature() - CommonConfig.splashingCoolAmount.get().floatValue()));
            }
        }
    }
}
