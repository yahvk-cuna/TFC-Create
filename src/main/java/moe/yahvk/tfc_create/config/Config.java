package moe.yahvk.tfc_create.config;

import moe.yahvk.tfc_create.TFCCreate;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

// @Mod.EventBusSubscriber(modid = TFCCreate.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final CommonConfig common;
    private static final ForgeConfigSpec configCommonSpec;
    public static final ClientConfig client;
    private static final ForgeConfigSpec configClientSpec;

    static {
        Pair<CommonConfig, ForgeConfigSpec> spec = new ForgeConfigSpec.Builder()
                .configure(CommonConfig::new);
        common = spec.getLeft();
        configCommonSpec = spec.getRight();

        Pair<ClientConfig, ForgeConfigSpec> spec2 = new ForgeConfigSpec.Builder()
                .configure(ClientConfig::new);

        client = spec2.getLeft();
        configClientSpec = spec2.getRight();
    }

    public static void registerConfigs(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, configCommonSpec);
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, configClientSpec);
    }

//    @SubscribeEvent
//    static void onLoad(final ModConfigEvent event) {
//        logDirtBlock = LOG_DIRT_BLOCK.get();
//        magicNumber = MAGIC_NUMBER.get();
//        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
//
//        // convert the list of strings into a set of items
//        items = ITEM_STRINGS.get().stream().map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName))).collect(Collectors.toSet());
//    }

}
