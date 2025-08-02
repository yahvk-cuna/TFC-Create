package moe.yahvk.tfc_create.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static ForgeConfigSpec.BooleanValue basinHeatable;
    public static ForgeConfigSpec.BooleanValue heatItem;
    public static ForgeConfigSpec.BooleanValue heatRecipeInBasin;
    public static ForgeConfigSpec.IntValue basinInputTanks;
    public static ForgeConfigSpec.IntValue basinOutputTanks;

    public static ForgeConfigSpec.BooleanValue alloyingByMixer;
    public static ForgeConfigSpec.BooleanValue alloyingRequireHot;

    CommonConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("basin");

        basinHeatable = builder
                .comment("Whether the basin can be heated. If this is false, all other heat-related recipe will not work.")
                .define("basinHeatable", true);

        heatItem = builder
                .comment("Whether the basin can heat items. If this is false, items will not be heated in the hot basin.")
                .define("heatItem", true);

        heatRecipeInBasin = builder
                .comment("Whether the basin can process heat recipes like a crucible (eg. melting metal).")
                .define("heatRecipeInBasin", true);

        basinInputTanks = builder
                .comment("The number of input tanks in the basin. Vanilla Create is 2.")
                .defineInRange("basinInputTanks", 4, 2, 8);

        basinOutputTanks = builder
                .comment("The number of output tanks in the basin. Vanilla Create is 2.\n" +
                        "Note: melting recipes will put fluid in output tanks, so if you will melting and mixing in the same basin, " +
                        "you need to set this to the max ingredient number plus 1 of alloy recipes.")
                .defineInRange("basinInputTanks", 4, 2, 8);

        builder.pop();

        builder.push("alloying");

        alloyingByMixer = builder
                .comment("Whether to allow alloying by mechanical mixer.")
                .define("alloyingByMixer", true);

        alloyingRequireHot = builder
                .comment("Whether to require the basin to be hot for alloying.")
                .define("alloyingRequireHot", true);

        builder.pop();
    }
}