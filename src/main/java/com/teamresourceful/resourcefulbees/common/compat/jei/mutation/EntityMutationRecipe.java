package com.teamresourceful.resourcefulbees.common.compat.jei.mutation;

import com.teamresourceful.resourcefulbees.api.beedata.CustomBeeData;
import com.teamresourceful.resourcefulbees.api.beedata.outputs.EntityOutput;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;

import java.util.Optional;

public class EntityMutationRecipe extends BaseMutationRecipe {

    private final EntityType<?> input;
    private final EntityType<?> output;
    private final CompoundNBT nbt;

    public EntityMutationRecipe(CustomBeeData beeData, double chance, double weight, EntityType<?> input, EntityOutput output) {
        super(beeData, chance, weight);
        this.input = input;
        this.output = output.getEntityType();
        this.nbt = output.getCompoundNBT().orElse(null);
    }

    @Override
    public Optional<CompoundNBT> getNBT() {
        return Optional.ofNullable(nbt);
    }

    @Override
    public Optional<EntityType<?>> getInputEntity() {
        return Optional.ofNullable(input);
    }

    @Override
    public Optional<EntityType<?>> getOutputEntity() {
        return Optional.ofNullable(output);
    }
}
