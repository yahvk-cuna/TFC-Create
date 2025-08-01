package moe.yahvk.tfc_create.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static ForgeConfigSpec.BooleanValue basinHeatable;
    public static ForgeConfigSpec.BooleanValue heatItem;
    public static ForgeConfigSpec.BooleanValue heatRecipeInBasin;

    CommonConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("basin");

        basinHeatable = builder
                .comment("Whether the basin can be heated. If this is false, all other basin-related config options will be ignored.")
                .define("basinHeatable", true);

        heatItem = builder
                .comment("Whether the basin can heat items. If this is false, items will not be heated in the hot basin.")
                .define("heatItem", true);

        heatRecipeInBasin = builder
                .comment("Whether the basin can process heat recipes like a crucible (eg. melting metal).")
                .define("heatRecipeInBasin", true);

        builder.pop();
    }
}