package moe.yahvk.tfc_create.create;

import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import moe.yahvk.tfc_create.config.CommonConfig;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeatBlock;

public class BlazeBurnerHeatBehaviour extends BlockEntityBehaviour implements IHeatBlock {
    public static final BehaviourType<FluidFillingBehaviour> TYPE = new BehaviourType<>();

    public BlazeBurnerHeatBehaviour(BlazeBurnerBlockEntity be) {
        super(be);
    }

    public static float heatToTemperature(BlazeBurnerBlock.HeatLevel heatLevel) {
        return switch (heatLevel) {
            case NONE -> 0f;
            case SMOULDERING -> CommonConfig.smoulderingTemperature.get();
            case FADING -> CommonConfig.fadingTemperature.get();
            case KINDLED -> CommonConfig.kindledTemperature.get();
            case SEETHING -> CommonConfig.seethingTemperature.get();
        };
    }

    @Override
    public void tick() {
        if (blockEntity.getLevel() != null) {
            HeatCapability.provideHeatTo(blockEntity.getLevel(), blockEntity.getBlockPos().above(), getTemperature());
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public float getTemperature() {
        if (blockEntity instanceof BlazeBurnerBlockEntity blaze) {
            return heatToTemperature(blaze.getHeatLevelFromBlock());
        }
        return 0f;
    }

    @Override
    public void setTemperature(float v) {
    }
}
