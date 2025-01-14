package com.teamresourceful.resourcefulbees.common.mixin.accessors;

import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimalEntity.class)
public interface AnimalEntityAccessor {

    @Accessor("inLove")
    void setLove(int time);

}
