package com.teamresourceful.resourcefulbees.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamresourceful.resourcefulbees.ResourcefulBees;
import com.teamresourceful.resourcefulbees.api.IBeeRegistry;
import com.teamresourceful.resourcefulbees.api.beedata.CustomBeeData;
import com.teamresourceful.resourcefulbees.compat.jei.ingredients.EntityIngredient;
import com.teamresourceful.resourcefulbees.registry.BeeRegistry;
import com.teamresourceful.resourcefulbees.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EntityFlowerCategory extends BaseCategory<EntityFlowerCategory.Recipe> {
    public static final ResourceLocation GUI_BACK = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/gui/jei/beeentityflowers.png");
    public static final ResourceLocation ID = new ResourceLocation(ResourcefulBees.MOD_ID, "bee_pollination_entity_flowers");
    private static final IBeeRegistry BEE_REGISTRY = BeeRegistry.getRegistry();
    private final IDrawable nonRegisteredEgg;

    public EntityFlowerCategory(IGuiHelper guiHelper){
        super(guiHelper, ID,
                I18n.get("gui.resourcefulbees.jei.category.bee_pollination_entity_flowers"),
                guiHelper.drawableBuilder(GUI_BACK, 0, 0, 100, 75).addPadding(0, 0, 0, 0).build(),
                guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.GOLD_FLOWER.get())),
                EntityFlowerCategory.Recipe.class);
        this.nonRegisteredEgg = guiHelper.createDrawable(ICONS, 41, 0, 16, 16);
    }

    public static List<EntityFlowerCategory.Recipe> getFlowersRecipes() {
        List<EntityFlowerCategory.Recipe> recipes = new ArrayList<>();
        BEE_REGISTRY.getBees().forEach(((s, beeData) -> {
            if (beeData.getCoreData().getEntityFlower().isPresent()) {
                EntityType<?> entityType = beeData.getCoreData().getFlowerEntityType();
                if (entityType != null) {
                    Item spawnEggItem = SpawnEggItem.byId(entityType);
                    recipes.add(new Recipe(beeData, entityType, spawnEggItem != null ? new ItemStack(spawnEggItem) : null));
                }
            }
        }));
        return recipes;
    }

    @Override
    public void setIngredients(@NotNull Recipe recipe, @NotNull IIngredients ingredients) {
        if (recipe.spawnEgg != null) ingredients.setInput(VanillaTypes.ITEM, recipe.spawnEgg);
        ingredients.setInput(JEICompat.ENTITY_INGREDIENT, new EntityIngredient(recipe.beeType, -45.0f));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout iRecipeLayout, @NotNull Recipe recipe, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = iRecipeLayout.getItemStacks();
        if (recipe.spawnEgg != null){
            itemStacks.init(0, false, 41, 55);
            itemStacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
            itemStacks.addTooltipCallback((slotIndex, isInputStack, stack, tooltip) -> {
                if (slotIndex == 0) {
                    tooltip.clear();
                    tooltip.add(recipe.entityType.getDescription().plainCopy());
                    if (recipe.entityType.getRegistryName() != null) {
                        tooltip.add(new TextComponent(recipe.entityType.getRegistryName().toString()).withStyle(ChatFormatting.GRAY));
                    }
                }
            });
        }

        IGuiIngredientGroup<EntityIngredient> ingredientStacks = iRecipeLayout.getIngredientsGroup(JEICompat.ENTITY_INGREDIENT);
        ingredientStacks.init(0, true, 41, 10);
        ingredientStacks.set(0, ingredients.getInputs(JEICompat.ENTITY_INGREDIENT).get(0));
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(@NotNull Recipe recipe, double mouseX, double mouseY) {
        if (recipe.spawnEgg == null && mouseX > 41 && mouseX < 57 && mouseY > 55 && mouseY < 71){
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(recipe.entityType.getDescription().plainCopy());
            if (recipe.entityType.getRegistryName() != null) {
                tooltip.add(new TextComponent(recipe.entityType.getRegistryName().toString()).withStyle(ChatFormatting.GRAY));
            }
            return tooltip;
        }
        return super.getTooltipStrings(recipe, mouseX, mouseY);
    }

    @Override
    public void draw(@NotNull Recipe recipe, @NotNull PoseStack stack, double mouseX, double mouseY) {
        if (recipe.spawnEgg == null){
            nonRegisteredEgg.draw(stack, 42, 56);
        }
    }

    public static class Recipe {
        private final CustomBeeData beeType;
        private final EntityType<?> entityType;
        private final ItemStack spawnEgg;

        public Recipe(CustomBeeData beeType, EntityType<?> entityType, ItemStack spawnEgg) {
            this.beeType = beeType;
            this.entityType = entityType;
            this.spawnEgg = spawnEgg;
        }
    }
}