package com.teamresourceful.resourcefulbees.datagen.providers.recipes;

import com.teamresourceful.resourcefulbees.ResourcefulBees;
import com.teamresourceful.resourcefulbees.common.lib.constants.ModTags;
import com.teamresourceful.resourcefulbees.common.recipe.NestIngredient;
import com.teamresourceful.resourcefulbees.common.registry.minecraft.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildShapelessRecipes(@NotNull Consumer<IFinishedRecipe> recipes) {
        RecipeCriteria hasPlanks = new RecipeCriteria("has_planks", has(ItemTags.PLANKS));
        RecipeCriteria hasGoldStorage = new RecipeCriteria("has_gold_storage", has(ModItems.GOLD_STORAGE_UPGRADE.get()));
        RecipeCriteria hasHoneycombBlock = new RecipeCriteria("has_honeycomb_block", has(ModTags.Items.HONEYCOMB_BLOCK));
        RecipeCriteria hasIron = new RecipeCriteria("has_iron", has(Tags.Items.INGOTS_IRON));

        //region Hive Upgrades
        RecipeHelper.createBoxed(Ingredient.of(ItemTags.PLANKS), Ingredient.of(Items.GRASS), ModItems.T1_HIVE_UPGRADE.get())
                .unlockedBy(hasPlanks).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(ItemTags.PLANKS), Ingredient.of(ModTags.Items.HONEYCOMB), Ingredient.of(ModTags.Items.WAX), ModItems.T2_HIVE_UPGRADE.get())
                .unlockedBy(hasPlanks).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(ItemTags.PLANKS), Ingredient.of(ModTags.Items.HONEYCOMB_BLOCK), Ingredient.of(ModTags.Items.WAX_BLOCK), ModItems.T3_HIVE_UPGRADE.get())
                .unlockedBy(hasPlanks).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(ItemTags.PLANKS), Ingredient.of(ModTags.Items.HONEYCOMB_BLOCK), Ingredient.of(Items.HONEY_BLOCK), ModItems.T4_HIVE_UPGRADE.get())
                .unlockedBy(hasPlanks).save(recipes);
        //endregion
        //region Apiary Upgrades
        RecipeHelper.createCornerWithChestRecipe(Ingredient.of(ModItems.APIARY_STORAGE_ITEM.get()), Tags.Items.INGOTS_IRON, ModItems.IRON_STORAGE_UPGRADE.get())
                .unlockedBy(hasIron).save(recipes);
        RecipeHelper.createCornerWithChestRecipe(Ingredient.of(ModItems.IRON_STORAGE_UPGRADE.get()), Tags.Items.INGOTS_GOLD, ModItems.GOLD_STORAGE_UPGRADE.get())
                .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD)).save(recipes);
        RecipeHelper.createCornerWithChestRecipe(Ingredient.of(ModItems.GOLD_STORAGE_UPGRADE.get()), Tags.Items.GEMS_DIAMOND, ModItems.DIAMOND_STORAGE_UPGRADE.get())
                .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND)).save(recipes);
        RecipeHelper.createCornerWithChestRecipe(Ingredient.of(ModItems.DIAMOND_STORAGE_UPGRADE.get()), Tags.Items.GEMS_EMERALD, ModItems.EMERALD_STORAGE_UPGRADE.get())
                .unlockedBy("has_emerald", has(Tags.Items.GEMS_EMERALD)).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(ModItems.GOLD_STORAGE_UPGRADE.get()), Ingredient.of(ModItems.BEE_JAR.get()), Ingredient.of(ItemTags.FLOWERS), ModItems.APIARY_BREEDER_UPGRADE.get())
                .unlockedBy(hasGoldStorage).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(ModItems.GOLD_STORAGE_UPGRADE.get()), Ingredient.of(Items.CLOCK), Ingredient.of(ItemTags.FLOWERS), ModItems.APIARY_BREED_TIME_UPGRADE.get())
                .unlockedBy(hasGoldStorage).save(recipes);
        //endregion
        //region Apiary Blocks
        RecipeHelper.createCornerWithMid(Ingredient.of(Items.NETHER_STAR), Ingredient.of(ModTags.Items.HONEYCOMB_BLOCK), NestIngredient.ofTier(4), ModItems.T1_APIARY_ITEM.get())
                .unlockedBy(hasHoneycombBlock).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(Items.NETHER_STAR), Ingredient.of(ModTags.Items.HONEYCOMB_BLOCK), Ingredient.of(ModItems.T1_APIARY_ITEM.get()), ModItems.T2_APIARY_ITEM.get())
                .unlockedBy(hasHoneycombBlock).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(Items.NETHER_STAR), Ingredient.of(ModTags.Items.HONEYCOMB_BLOCK), Ingredient.of(ModItems.T2_APIARY_ITEM.get()), ModItems.T3_APIARY_ITEM.get())
                .unlockedBy(hasHoneycombBlock).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(Items.NETHER_STAR), Ingredient.of(ModTags.Items.HONEYCOMB_BLOCK), Ingredient.of(ModItems.T3_APIARY_ITEM.get()), ModItems.T4_APIARY_ITEM.get())
                .unlockedBy(hasHoneycombBlock).save(recipes);
        RecipeHelper.createCornerWithChestRecipe(Ingredient.of(Items.HOPPER), ModTags.Items.HONEYCOMB_BLOCK, ModItems.APIARY_STORAGE_ITEM.get())
                .unlockedBy(hasHoneycombBlock).save(recipes);
        RecipeHelper.createCornerWithMid(Ingredient.of(ModItems.APIARY_STORAGE_ITEM.get()), Ingredient.of(ModItems.BEE_JAR.get()), Ingredient.of(ItemTags.FLOWERS), ModItems.APIARY_BREEDER_ITEM.get())
                .unlockedBy("has_bee_jar", has(ModItems.BEE_JAR.get())).save(recipes);
        //endregion
        //region Honey Conversion
        ShapelessRecipeBuilder.shapeless(ModItems.HONEY_FLUID_BUCKET.get()).requires(Items.HONEY_BOTTLE, 4).requires(Items.BUCKET)
                .unlockedBy("has_honey_bottle", has(Items.HONEY_BOTTLE)).save(recipes, new ResourceLocation(ResourcefulBees.MOD_ID, "honey_bottles_to_bucket"));
        ShapelessRecipeBuilder.shapeless(Items.HONEY_BOTTLE, 4).requires(Items.GLASS_BOTTLE, 4).requires(ModItems.HONEY_FLUID_BUCKET.get())
                .unlockedBy("has_honey_bottle", has(Items.HONEY_BOTTLE)).save(recipes, new ResourceLocation(ResourcefulBees.MOD_ID, "honey_bucket_to_bottles"));
        //endregion
        //region Wax
        RecipeHelper.getStorageRecipe(ModItems.WAX_BLOCK_ITEM.get(), Ingredient.of(ModItems.WAX.get()))
                .unlockedBy("has_wax", has(ModItems.WAX.get())).save(recipes);
        RecipeHelper.getStorageToItemRecipe(ModItems.WAX.get(), Ingredient.of(ModItems.WAX_BLOCK_ITEM.get()))
                .unlockedBy("has_wax_block", has(ModItems.WAX_BLOCK_ITEM.get())).save(recipes);
        //endregion
        //region Tools
        AdvancedShapedRecipeBuilder.shaped(ModItems.SMOKERCAN)
                .pattern("II ", "I I", "ICI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('C', Ingredient.of(Items.CAMPFIRE))
                .unlockedBy(hasIron)
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.SCRAPER)
                .pattern(" II", " SI", "S  ")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('S', Ingredient.of(Items.STICK))
                .unlockedBy(hasIron)
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.BEE_JAR)
                .pattern(" G ", "G G", "GGG")
                .define('G', Ingredient.of(Tags.Items.GLASS_PANES_COLORLESS))
                .unlockedBy("has_glass_panes", has(Tags.Items.GLASS_PANES_COLORLESS))
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.BELLOW)
                .pattern("LL ", "L L", " LL")
                .define('L', Ingredient.of(Items.LEATHER))
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.BEEPEDIA)
                .pattern("IRI", "IGI", "IHI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('G', Ingredient.of(Tags.Items.GLASS_PANES))
                .define('H', Ingredient.of(ModTags.Items.HONEY_BOTTLES))
                .define('R', Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .unlockedBy(hasIron)
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.BEE_BOX)
                .pattern("PPP", "WIW", "PPP")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('P', Ingredient.of(ItemTags.PLANKS))
                .define('W', Ingredient.of(ModTags.Items.WAX))
                .unlockedBy(hasIron)
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.HONEY_DIPPER)
                .pattern(" CW", " SC", "S  ")
                .define('C', Ingredient.of(ModTags.Items.HONEYCOMB))
                .define('S', Ingredient.of(Tags.Items.RODS_WOODEN))
                .define('W', Ingredient.of(ModTags.Items.WAX))
                .unlockedBy("has_honeycomb", has(ModTags.Items.HONEYCOMB))
                .save(recipes);
        ShapelessRecipeBuilder.shapeless(ModItems.SMOKER.get()).requires(ModItems.SMOKERCAN.get()).requires(ModItems.BELLOW.get()).unlockedBy("has_honeycomb", has(ModItems.SMOKERCAN.get())).save(recipes);
        //endregion
        //region Machines
        AdvancedShapedRecipeBuilder.shaped(ModItems.ENDER_BEECON_ITEM)
                .pattern("PPP","GEG","POP")
                .define('E', Ingredient.of(Tags.Items.ENDER_PEARLS))
                .define('P', Ingredient.of(Items.PURPUR_BLOCK))
                .define('G', Ingredient.of(Tags.Items.GLASS))
                .define('O', Ingredient.of(Tags.Items.OBSIDIAN))
                .unlockedBy("has_obsidian", has(Tags.Items.OBSIDIAN))
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.SOLIDIFICATION_CHAMBER_ITEM)
                .pattern(" G ","IGI","SWS")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('G', Ingredient.of(Tags.Items.GLASS))
                .define('W', Ingredient.of(ModTags.Items.WAX))
                .define('S', Ingredient.of(Tags.Items.STONE))
                .unlockedBy(hasIron)
                .save(recipes);
        AdvancedShapedRecipeBuilder.shaped(ModItems.HONEY_GENERATOR_ITEM)
                .pattern("I@I","SGR","IBI")
                .define('I', Ingredient.of(Tags.Items.INGOTS_IRON))
                .define('B', Ingredient.of(Items.BUCKET))
                .define('G', Ingredient.of(Tags.Items.GLASS))
                .define('S', Ingredient.of(Items.IRON_BARS))
                .define('R', Ingredient.of(Tags.Items.DUSTS_REDSTONE))
                .define('@', Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON))
                .unlockedBy(hasIron)
                .save(recipes);
        //endregion
    }





}
