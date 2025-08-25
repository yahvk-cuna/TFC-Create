package moe.yahvk.tfc_create.mixin;

import com.simibubi.create.AllBlocks;
import moe.yahvk.tfc_create.config.CommonConfig;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.dries007.tfc.common.blocks.devices.SealableDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@Mixin(BarrelBlock.class)
abstract public class BarrelBlockMixin extends SealableDeviceBlock {
    public BarrelBlockMixin(ExtendedProperties properties) {
        super(properties);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (AllBlocks.MECHANICAL_ARM.isIn(heldItem)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Unique
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return CommonConfig.barrelComparator.get();
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        if (!CommonConfig.barrelComparator.get()) {
            return 0;
        }
        if (!(world.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return 0;
        }
        if (barrel.getRecipe() == null) {
            return 0;
        }
        return blockState.getValue(SEALED) ? 15 : 1;
    }
}
