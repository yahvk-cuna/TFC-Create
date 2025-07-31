package moe.yahvk.tfc_create.create;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import moe.yahvk.tfc_create.TFCCreate;
import net.dries007.tfc.common.capabilities.egg.EggCapability;
import net.dries007.tfc.common.capabilities.egg.IEgg;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.util.Sluiceable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class ItemAttribute {
    public static final ItemAttributeType
            EATABLE = singleton("eatable", FoodCapability::has),
            ROTTEN = singleton("rotten", FoodCapability::isRotten),
            SLUICEABLE = singleton("sluiceable", s -> Sluiceable.get(s) != null),
            HEATABLE = singleton("heatable", HeatCapability::has),
            HOT = singleton("hot", HeatCapability::isHot),
            CAN_WORK = singleton("can_work", s -> s.getCapability(HeatCapability.CAPABILITY).map( h -> h.canWork() && h.getWorkingTemperature() != 0).orElse(false)),
            CAN_WELD = singleton("can_weld", s -> s.getCapability(HeatCapability.CAPABILITY).map(h -> h.canWeld() && h.getWeldingTemperature() != 0).orElse(false)),
            FERTILIZED = singleton("fertilized", s -> s.getCapability(EggCapability.CAPABILITY).map(IEgg::isFertilized).orElse(false));

    private static ItemAttributeType singleton(String id, Predicate<ItemStack> predicate) {
        return register(id, new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, (stack, level) -> predicate.test(stack), "tfc_create." + id)));
    }

    private static ItemAttributeType register(String id, ItemAttributeType type) {
        return Registry.register(CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE, ResourceLocation.fromNamespaceAndPath(TFCCreate.MODID, id), type);
    }

    public static void init() {
    }
}
