package com.dungeonderps.resourcefulbees.compat.jei;

import com.dungeonderps.resourcefulbees.ResourcefulBees;
import com.dungeonderps.resourcefulbees.data.BeeData;
import com.dungeonderps.resourcefulbees.entity.passive.CustomBeeEntity;
import com.dungeonderps.resourcefulbees.lib.BeeConstants;
import com.dungeonderps.resourcefulbees.registry.RegistryHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.dungeonderps.resourcefulbees.config.BeeInfo.BEE_INFO;

public class BeeHiveCategory implements IRecipeCategory<BeeHiveCategory.Recipe> {
    public static final ResourceLocation GUI_BACK = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/gui/jei/beehive.png");
    public static final ResourceLocation ID = new ResourceLocation(ResourcefulBees.MOD_ID, "hive");
    private final IDrawable background;
    private final IDrawable icon;
    private final String localizedName;
    private final CustomBeeEntity bee;

    public BeeHiveCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(GUI_BACK, 0, 0, 160, 25).addPadding(0, 0, 0, 0).build();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(RegistryHandler.T1_BEEHIVE_ITEM.get()));
        this.localizedName = I18n.format("gui.resourcefulbees.jei.category.hive");
        World clientWorld = Minecraft.getInstance().world;
        if (clientWorld != null)
            bee = RegistryHandler.CUSTOM_BEE.get().create(clientWorld);
        else
            bee = null;
    }

    public static List<Recipe> getHoneycombRecipes(IIngredientManager ingredientManager) {
        List<Recipe> recipes = new ArrayList<>();
        for (Map.Entry<String, BeeData> bee : BEE_INFO.entrySet()){
            if (!bee.getValue().getMainOutput().isEmpty()) {
                ItemStack honeyCombItemStack = new ItemStack(RegistryHandler.RESOURCEFUL_HONEYCOMB.get());
                final CompoundNBT honeyCombItemStackTag = honeyCombItemStack.getOrCreateChildTag(BeeConstants.NBT_ROOT);
                honeyCombItemStackTag.putString(BeeConstants.NBT_COLOR, bee.getValue().getHoneycombColor());
                honeyCombItemStackTag.putString(BeeConstants.NBT_BEE_TYPE, bee.getKey());
                recipes.add(new Recipe(honeyCombItemStack, bee.getKey()));
            }
        }
        return recipes;
    }

    @Nonnull
    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Nonnull
    @Override
    public Class<? extends Recipe> getRecipeClass() {
        return Recipe.class;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return this.localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Nonnull
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setIngredients(Recipe recipe, IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getComb());
        ingredients.setInput(VanillaTypes.ITEM, new ItemStack(RegistryHandler.T1_BEEHIVE_ITEM.get()));
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, @Nonnull Recipe recipe, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = iRecipeLayout.getItemStacks();
        itemStacks.init(0, false, 138, 4);
        itemStacks.init(1, true, 62, 4);
        itemStacks.set(ingredients);
    }

    @Nonnull
    @Override
    public List<ITextComponent> getTooltipStrings(@Nonnull Recipe recipe, double mouseX, double mouseY) {
        double beeX = 2D;
        double beeY = 2D;
        if (mouseX >= beeX && mouseX <= beeX + 30D && mouseY >= beeY && mouseY <= beeY + 30D){
            return Collections.singletonList(new StringTextComponent(I18n.format("entity." + ResourcefulBees.MOD_ID + "." + recipe.beeType + "_bee")));
        }
        return IRecipeCategory.super.getTooltipStrings(recipe,mouseX, mouseY);
    }

    public void renderEntity(MatrixStack matrix, String beeType, Float rotation, Double xPos, Double yPos){
        Minecraft mc = Minecraft.getInstance();
        matrix.push();
        matrix.translate(0, 0, 0.5D);

        if (mc.player !=null) {
            bee.ticksExisted = mc.player.ticksExisted;
            bee.renderYawOffset = rotation - 90;
            bee.setBeeType(beeType);
            float scaledSize = 30;
            if (!bee.getSizeModifierFromInfo(bee.getBeeType()).equals(1.0F)) {
                scaledSize = 30 / bee.getSizeModifierFromInfo(bee.getBeeType());
            }
            matrix.translate(xPos, yPos, 1);
            matrix.rotate(Vector3f.ZP.rotationDegrees(180.0F));
            matrix.translate(0.0F, -0.2F, 1);
            matrix.scale(scaledSize, scaledSize, 30);
            EntityRendererManager entityrenderermanager = mc.getRenderManager();
            IRenderTypeBuffer.Impl irendertypebuffer$impl = mc.getRenderTypeBuffers().getBufferSource();
            entityrenderermanager.renderEntityStatic(bee, 0, 0, 0.0D, mc.getRenderPartialTicks(), 1, matrix, irendertypebuffer$impl, 15728880);
            irendertypebuffer$impl.finish();
        }
        matrix.pop();
    }

    @Override
    public void draw(Recipe recipe, @Nonnull MatrixStack stack, double mouseX, double mouseY) {
        renderEntity(stack, recipe.getBeeType(), 135.0F, 20D, 20D);
    }

    public static class Recipe {
        private final ItemStack comb;
        private final String beeType;

        public Recipe(ItemStack comb, String beeType) {
            this.comb = comb;
            this.beeType = beeType;
        }

        public ItemStack getComb() {
            return this.comb;
        }
        public String getBeeType() {
            return this.beeType;
        }
    }
}