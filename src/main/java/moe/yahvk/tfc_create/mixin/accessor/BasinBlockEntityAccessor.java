package moe.yahvk.tfc_create.mixin.accessor;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BasinBlockEntity.class, remap = false)
public interface BasinBlockEntityAccessor {
    @Accessor
    boolean getContentsChanged();
}
