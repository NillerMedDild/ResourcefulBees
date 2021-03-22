package com.resourcefulbees.resourcefulbees.compat.jei;

import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.compat.jei.ingredients.EntityIngredient;
import com.resourcefulbees.resourcefulbees.config.Config;
import com.resourcefulbees.resourcefulbees.lib.ApiaryOutput;
import com.resourcefulbees.resourcefulbees.registry.BeeRegistry;
import com.resourcefulbees.resourcefulbees.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiaryCategory extends BaseCategory<ApiaryCategory.Recipe> {
    public static final ResourceLocation GUI_BACK = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/gui/jei/beehive.png");
    public static final ResourceLocation ID = new ResourceLocation(ResourcefulBees.MOD_ID, "apiary");

    public ApiaryCategory(IGuiHelper guiHelper) {
        super(guiHelper, ID,
            I18n.get("gui.resourcefulbees.jei.category.apiary"),
            guiHelper.drawableBuilder(GUI_BACK, 0, 0, 160, 26).addPadding(0, 0, 0, 0).build(),
            guiHelper.createDrawableIngredient(new ItemStack(ModItems.T1_APIARY_ITEM.get())),
            ApiaryCategory.Recipe.class);
    }

    public static List<Recipe> getHoneycombRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        final List<ApiaryOutput> outputs = new ArrayList<>(Arrays.asList(Config.T1_APIARY_OUTPUT.get(), Config.T2_APIARY_OUTPUT.get(), Config.T3_APIARY_OUTPUT.get(), Config.T4_APIARY_OUTPUT.get()));
        final int[] outputQuantities = {Config.T1_APIARY_QUANTITY.get(), Config.T2_APIARY_QUANTITY.get(), Config.T3_APIARY_QUANTITY.get(), Config.T4_APIARY_QUANTITY.get()};
        final List<Item> apiaryTiers = new ArrayList<>(Arrays.asList(ModItems.T1_APIARY_ITEM.get(), ModItems.T2_APIARY_ITEM.get(), ModItems.T3_APIARY_ITEM.get(), ModItems.T4_APIARY_ITEM.get()));

        BeeRegistry.getRegistry().getBees().forEach(((s, customBeeData) -> {
            int[] customAmounts = customBeeData.getApiaryOutputAmounts();
            if (customBeeData.hasHoneycomb()) {
                for (int i = 0; i < 4; i++){
                    Item outputItem = outputs.get(i).equals(ApiaryOutput.COMB) ? customBeeData.getCombRegistryObject().get() : customBeeData.getCombBlockItemRegistryObject().get();
                    ItemStack outputStack = new ItemStack(outputItem, customAmounts != null && customAmounts[i] > 0 ? customAmounts[i] :  outputQuantities[i]);
                    recipes.add(new Recipe(outputStack, customBeeData.getName(), new ItemStack(apiaryTiers.get(i))));
                }
            }
        }));
        return recipes;
    }

    @Override
    public void setIngredients(@NotNull Recipe recipe, @NotNull IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getComb());
        ingredients.setInput(VanillaTypes.ITEM, recipe.apiary);
        ingredients.setInput(JEICompat.ENTITY_INGREDIENT, new EntityIngredient(recipe.beeType, -45.0f));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout iRecipeLayout, @Nonnull Recipe recipe, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = iRecipeLayout.getItemStacks();
        itemStacks.init(0, false, 138, 4);
        itemStacks.init(1, true, 62, 4);
        itemStacks.set(ingredients);

        IGuiIngredientGroup<EntityIngredient> ingredientStacks = iRecipeLayout.getIngredientsGroup(JEICompat.ENTITY_INGREDIENT);
        ingredientStacks.init(0, true, 10, 2);
        ingredientStacks.set(0, ingredients.getInputs(JEICompat.ENTITY_INGREDIENT).get(0));
    }

    public static class Recipe {
        private final ItemStack comb;
        private final String beeType;
        private final ItemStack apiary;

        public Recipe(ItemStack comb, String beeType, ItemStack apiary) {
            this.comb = comb;
            this.beeType = beeType;
            this.apiary = apiary;
        }

        public ItemStack getComb() {
            return this.comb;
        }
        public String getBeeType() {
            return this.beeType;
        }
        public ItemStack getApiary() { return this.apiary; }
    }
}