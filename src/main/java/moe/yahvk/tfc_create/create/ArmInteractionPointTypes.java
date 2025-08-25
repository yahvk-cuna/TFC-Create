package moe.yahvk.tfc_create.create;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import moe.yahvk.tfc_create.TFCCreate;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class ArmInteractionPointTypes {

    static {
        register("crucible", new CrucibleType());
        register("charcoal_forge", new CharcoalForgeType());
        register("barrel", new BarrelType());
    }

    private static <T extends ArmInteractionPointType> void register(String name, T type) {
        Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, TFCCreate.asResource(name), type);
    }

    public static void init() {
    }

    public static class CrucibleType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(TFCBlocks.CRUCIBLE.get());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class CharcoalForgeType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(TFCBlocks.CHARCOAL_FORGE.get());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ArmInteractionPoint(this, level, pos, state);
        }
    }

    public static class BarrelType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return level.getBlockEntity(pos) instanceof BarrelBlockEntity;
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BarrelPoint(this, level, pos, state);
        }
    }

    public static class BarrelPoint extends ArmInteractionPoint {
        protected LazyOptional<BarrelBlockEntity.BarrelInventory> cachedHandler = LazyOptional.empty();

        public BarrelPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Nullable
        @Override
        protected BarrelBlockEntity.BarrelInventory getHandler() {
            if (!cachedHandler.isPresent()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof BarrelBlockEntity)) {
                    return null;
                }
                cachedHandler = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).cast();
            }
            //noinspection DataFlowIssue
            return cachedHandler.orElse(null);
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            BarrelBlockEntity.BarrelInventory handler = getHandler();
            if (handler == null)
                return stack;
            return handler.insertItem(BarrelBlockEntity.SLOT_ITEM, stack, simulate);
        }

        @Override
        public ItemStack extract(int slot, int amount, boolean simulate) {
            BarrelBlockEntity.BarrelInventory handler = getHandler();
            if (handler == null)
                return ItemStack.EMPTY;
            return handler.extractItem(BarrelBlockEntity.SLOT_ITEM, amount, simulate);
        }
    }
}
