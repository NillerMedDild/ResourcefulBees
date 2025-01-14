package com.teamresourceful.resourcefulbees.common.registry.minecraft;

import com.teamresourceful.resourcefulbees.ResourcefulBees;
import com.teamresourceful.resourcefulbees.common.lib.constants.ModConstants;
import com.teamresourceful.resourcefulbees.common.recipe.ApiaryUpgradeRecipe;
import com.teamresourceful.resourcefulbees.common.recipe.CentrifugeRecipe;
import com.teamresourceful.resourcefulbees.common.recipe.SolidificationRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipeSerializers {

    private ModRecipeSerializers() {
        throw new IllegalStateException(ModConstants.UTILITY_CLASS);
    }

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ResourcefulBees.MOD_ID);

    public static final RegistryObject<IRecipeSerializer<?>> CENTRIFUGE_RECIPE = RECIPE_SERIALIZERS.register("centrifuge", CentrifugeRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> APIARY_UPGRADE_RECIPE = RECIPE_SERIALIZERS.register("hive_upgrade_recipe", ApiaryUpgradeRecipe.Serializer::new);
    public static final RegistryObject<IRecipeSerializer<?>> SOLIDIFICATION_RECIPE = RECIPE_SERIALIZERS.register("solidification", SolidificationRecipe.Serializer::new);
}
