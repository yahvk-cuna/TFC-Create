package moe.yahvk.tfc_create.mixin;

import moe.yahvk.tfc_create.controller.TransportedItemStackController;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack.class)
public class TransportedItemStackMixin implements TransportedItemStackController {
    @Unique
    HeatingRecipe tfc_create$cacheRecipe;
    @Unique
    boolean tfc_create$initialized = false;

    @Override
    @Nullable
    public HeatingRecipe tfc_create$getCacheRecipe() {
        return tfc_create$cacheRecipe;
    }

    @Override
    public void tfc_create$setCacheRecipe(HeatingRecipe recipe) {
        tfc_create$cacheRecipe = recipe;
        tfc_create$initialized = true;
    }

    @Override
    public boolean tfc_create$initialized() {
        return tfc_create$initialized;
    }
}
