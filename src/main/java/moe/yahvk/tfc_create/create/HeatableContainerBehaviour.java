package moe.yahvk.tfc_create.create;

import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;
import moe.yahvk.tfc_create.config.CommonConfig;
import moe.yahvk.tfc_create.mixin.accessor.BasinBlockEntityAccessor;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.capabilities.heat.IHeatBlock;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeatableContainerBehaviour extends BlockEntityBehaviour implements IHeatBlock {
    public static final BehaviourType<FluidFillingBehaviour> TYPE = new BehaviourType<>();
    private static final int TEMPERATURE_STABILITY_TICKS = 5;
    private int temperatureStabilityTicks;

    private float temperature;
    private HeatingRecipe[] cachedRecipes;
    private long lastCalendarUpdateTick;

    public HeatableContainerBehaviour(BasinBlockEntity be) {
        super(be);

        this.cachedRecipes = new HeatingRecipe[0];
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!(blockEntity instanceof BasinBlockEntity basin)) {
            return;
        }
        SmartInventory inputs = basin.getInputInventory();
        cachedRecipes = new HeatingRecipe[inputs.getSlots()];
        for (int slot = 0; slot < inputs.getSlots(); slot++) {
            ItemStack inputStack = inputs.getItem(slot);
            if (!inputStack.isEmpty()) {
                cachedRecipes[slot] = HeatingRecipe.getRecipe(inputStack);
            }
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void tick() {
        super.tick();

        checkForCalendarUpdate();

        if (temperatureStabilityTicks > 0) {
            temperatureStabilityTicks--;
        }
        if (temperature > 0 && temperatureStabilityTicks == 0) {
            temperature = HeatCapability.adjustTempTowards(temperature, 0);
        }

        if (!(blockEntity instanceof BasinBlockEntity basin)) {
            return;
        }
        SmartInventory inputs = basin.getInputInventory();

        for (int slot = 0; slot < inputs.getSlots(); slot++) {
            ItemStack inputStack = inputs.getItem(slot);
            if (inputStack.isEmpty())
                continue;

            final @Nullable IHeat inputHeat = HeatCapability.get(inputStack);
            if (inputHeat != null) {
                if (CommonConfig.heatItem.get()) {
                    HeatCapability.addTemp(inputHeat, temperature, 2 + temperature * 0.0025f);
                }

                if (!CommonConfig.heatRecipeInBasin.get()) {
                    continue;
                }

                if (((BasinBlockEntityAccessor) basin).getContentsChanged()) {
                    cachedRecipes[slot] = HeatingRecipe.getRecipe(inputStack);
                }
                final HeatingRecipe recipe = cachedRecipes[slot];
                if (recipe != null && recipe.isValidTemperature(inputHeat.getTemperature())) {
                    // Convert input
                    final ItemStackInventory inventory = new ItemStackInventory(inputStack);
                    ItemStack outputItem = recipe.assemble(inventory, getWorld().registryAccess());
                    if (!outputItem.isEmpty())
                        outputItem.setCount(outputItem.getCount() * inputStack.getCount());
                    FluidStack outputFluid = recipe.assembleFluid(inventory);
                    if (!outputFluid.isEmpty())
                        outputFluid.setAmount(outputFluid.getAmount() * inputStack.getCount());

                    // Output transformations
                    // Heat will already be applied from the recipe, so we don't apply it here
                    FoodCapability.applyTrait(outputItem, FoodTraits.BURNT_TO_A_CRISP);

                    if (basin.acceptOutputs(List.of(outputItem), List.of(outputFluid), true)) {
                        basin.acceptOutputs(List.of(outputItem), List.of(outputFluid), false);
                        inputs.extractItem(slot, inputStack.getCount(), false);
                    }
                }
            }
        }

    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public void setTemperature(float t) {
        temperature = t;
        temperatureStabilityTicks = TEMPERATURE_STABILITY_TICKS;
        blockEntity.notifyUpdate();
    }

    @Override
    public void setTemperatureIfWarmer(float t) {
        if (t >= temperature) {
            temperature = t;
            temperatureStabilityTicks = TEMPERATURE_STABILITY_TICKS;
            blockEntity.notifyUpdate();
        }
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        temperature = nbt.getFloat("temperature");
        temperatureStabilityTicks = nbt.getInt("temperatureStabilityTicks");
        if (temperatureStabilityTicks < 0) {
            temperatureStabilityTicks = 0;
        }
        lastCalendarUpdateTick = nbt.getLong("lastCalendarUpdateTick");
        if (lastCalendarUpdateTick <= 0) {
            lastCalendarUpdateTick = Calendars.get().getTicks();
        }
        super.read(nbt, clientPacket);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putFloat("temperature", temperature);
        nbt.putInt("temperatureStabilityTicks", temperatureStabilityTicks);
        nbt.putLong("lastCalendarUpdateTick", lastCalendarUpdateTick);
        super.write(nbt, clientPacket);
    }

    public void onCalendarUpdate(long ticks) {
        temperature = HeatCapability.adjustTempTowards(temperature, 0, ticks);
    }

    void checkForCalendarUpdate() {
        BlockEntity entity = blockEntity;
        if (entity.getLevel() != null && !entity.getLevel().isClientSide()) {
            long thisTick = Calendars.SERVER.getTicks();
            long lastTick = this.getLastCalendarUpdateTick();
            long tickDelta = thisTick - lastTick;
            if (lastTick != -2147483648L && tickDelta != 1L) {
                this.onCalendarUpdate(tickDelta - 1L);
            }

            this.setLastCalendarUpdateTick(thisTick);
            entity.setChanged();
        }

    }

    public long getLastCalendarUpdateTick() {
        return lastCalendarUpdateTick;
    }

    public void setLastCalendarUpdateTick(long tick) {
        lastCalendarUpdateTick = tick;
    }
}
