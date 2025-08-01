package moe.yahvk.tfc_create.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec.BooleanValue showItemHeatInBasin;

    ClientConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("goggle");

        showItemHeatInBasin = builder
                .comment("Whether to show the item heat in the basin when looking at it with goggles.")
                .define("showItemHeatInBasin", true);

        builder.pop();
    }
}