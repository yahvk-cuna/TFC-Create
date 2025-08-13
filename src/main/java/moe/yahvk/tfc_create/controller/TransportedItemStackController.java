package moe.yahvk.tfc_create.controller;

import net.dries007.tfc.common.recipes.HeatingRecipe;

public interface TransportedItemStackController {
    HeatingRecipe tfc_create$getCacheRecipe();
    void tfc_create$setCacheRecipe(HeatingRecipe recipe);
    boolean tfc_create$initialized();
}
