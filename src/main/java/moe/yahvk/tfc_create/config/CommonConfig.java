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

    public static ForgeConfigSpec.IntValue temperatureToHeated;
    public static ForgeConfigSpec.IntValue temperatureToSuperHeated;
    public static ForgeConfigSpec.IntValue smoulderingTemperature;
    public static ForgeConfigSpec.IntValue fadingTemperature;
    public static ForgeConfigSpec.IntValue kindledTemperature;
    public static ForgeConfigSpec.IntValue seethingTemperature;
    public static ForgeConfigSpec.BooleanValue temperatureOnly;

    CommonConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("basin");

        basinHeatable = builder
                .comment("Whether the basin can be heated. If this is false, all other heat-related features will not work.")
                .define("basinHeatable", true);

        heatItem = builder
                .comment("Whether the basin can heat items. If this is false, items will not be heated in the hot basin.")
                .define("heatItem", true);

        heatRecipeInBasin = builder
                .comment("Whether the basin can process heat recipes like a charcoal forge or crucible (eg. melting metal).")
                .define("heatRecipeInBasin", true);

        basinInputTanks = builder
                .comment("The number of input tanks in the basin. Vanilla Create is 2.")
                .defineInRange("basinInputTanks", 4, 2, 20);

        basinOutputTanks = builder
                .comment("The number of output tanks in the basin. Vanilla Create is 2.\n" +
                        "Note: melting recipes will put fluid in output tanks, so if you will melting and mixing in the same basin, " +
                        "you need to set this to the max ingredient number plus 1 of alloy recipes.")
                .defineInRange("basinOutputTanks", 4, 2, 20);

        builder.pop().push("mixer");

        alloyingByMixer = builder
                .comment("Whether to allow alloying by mechanical mixer.")
                .define("alloyingByMixer", true);

        alloyingRequireHot = builder
                .comment("Whether to require the basin to be hot for alloying.")
                .define("alloyingRequireHot", true);

        builder.pop().push("blazeBurner");

        temperatureToHeated = builder
                .comment("At what temperature will the basin be considered in a Heated state for processing recipe?")
                .defineInRange("temperatureToHeated", 1000, 0, Integer.MAX_VALUE);

        temperatureToSuperHeated = builder
                .comment("At what temperature will the basin be considered in a Super-Heated state for processing recipe?")
                .defineInRange("temperatureToSuperHeated", 3000, 0, Integer.MAX_VALUE);

        smoulderingTemperature = builder
                .comment("The temperature of the smouldering blaze burner.")
                .defineInRange("smoulderingTemperature", 0, 0, Integer.MAX_VALUE);

        fadingTemperature = builder
                .comment("The temperature of the fading blaze burner.")
                .defineInRange("fadingTemperature", 1300, 0, Integer.MAX_VALUE);

        kindledTemperature = builder
                .comment("The temperature of the kindled blaze burner.")
                .defineInRange("kindledTemperature", 1600, 0, Integer.MAX_VALUE);

        seethingTemperature = builder
                .comment("The temperature of the seething blaze burner.")
                .defineInRange("seethingTemperature", 3000, 0, Integer.MAX_VALUE);

        temperatureOnly = builder
                .comment("Whether to only use temperature for the basin recipe. If false, it will also check the blaze burner heat level.")
                .define("temperatureOnly", false);
    }
}