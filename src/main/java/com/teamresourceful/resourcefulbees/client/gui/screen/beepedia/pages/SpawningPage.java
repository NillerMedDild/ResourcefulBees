package com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.pages;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.teamresourceful.resourcefulbees.api.beedata.CustomBeeData;
import com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.BeepediaScreen;
import com.teamresourceful.resourcefulbees.common.lib.constants.TranslationConstants;
import com.teamresourceful.resourcefulbees.common.utils.BeepediaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class SpawningPage extends BeeDataPage {

    private int subScrollPos;
    private final int scrollHeight;

    final List<ResourceLocation> biomeList;

    private final Button prevTab;
    private final Button nextTab;

    public SpawningPage(BeepediaScreen beepedia, CustomBeeData beeData, int xPos, int yPos, BeePage parent) {
        super(beepedia, beeData, xPos, yPos, parent);
        this.beeData = beeData;
        prevTab = new ImageButton(xPos + (SUB_PAGE_WIDTH / 2) - 48, yPos + 6, 8, 11, 0, 0, 11, arrowImage, 16, 33, button -> toggleTab());
        nextTab = new ImageButton(xPos + (SUB_PAGE_WIDTH / 2) + 40, yPos + 6, 8, 11, 8, 0, 11, arrowImage, 16, 33, button -> toggleTab());
        beepedia.addButton(nextTab);
        beepedia.addButton(prevTab);
        nextTab.visible = false;
        prevTab.visible = false;
        biomeList = new ArrayList<>(beeData.getSpawnData().getSpawnableBiomes());
        scrollHeight = biomeList.size() * 12;
    }

    private void toggleTab() {
        BeepediaScreen.currScreenState.setBiomesOpen(!BeepediaScreen.currScreenState.isBiomesOpen());
    }

    @Override
    public void renderBackground(MatrixStack matrix, float partialTick, int mouseX, int mouseY) {
        FontRenderer font = Minecraft.getInstance().font;
        TranslationTextComponent title = BeepediaScreen.currScreenState.isBiomesOpen() ? TranslationConstants.Beepedia.Spawning.BIOMES : TranslationConstants.Beepedia.Spawning.TITLE;
        int padding = font.width(title) / 2;
        font.draw(matrix, title.withStyle(TextFormatting.WHITE), xPos + ((float) SUB_PAGE_WIDTH / 2) - padding, (float) yPos + 8, -1);

        if (BeepediaScreen.currScreenState.isBiomesOpen()) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            double scale = beepedia.getMinecraft().getWindow().getGuiScale();
            int scissorY = (int) (beepedia.getMinecraft().getWindow().getHeight() - (yPos + SUB_PAGE_HEIGHT) * scale);
            GL11.glScissor((int) (xPos * scale), scissorY, (int) (SUB_PAGE_WIDTH * scale), (int) ((SUB_PAGE_HEIGHT - 22) * scale));
            for (int i = 0; i < biomeList.size(); i++) {
                TranslationTextComponent text = new TranslationTextComponent(String.format("biome.%s.%s", biomeList.get(i).getNamespace(), biomeList.get(i).getPath()));
                font.draw(matrix, text.withStyle(TextFormatting.GRAY), xPos, yPos + 22f + subScrollPos + i * 12f, -1);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            TranslationTextComponent groupName = new TranslationTextComponent(TranslationConstants.Beepedia.Spawning.GROUP, beeData.getSpawnData().getMinGroupSize(), beeData.getSpawnData().getMaxGroupSize());
            TranslationTextComponent heightName = new TranslationTextComponent(TranslationConstants.Beepedia.Spawning.HEIGHT, beeData.getSpawnData().getMinYLevel(), beeData.getSpawnData().getMaxYLevel());
            TranslationTextComponent weightName = new TranslationTextComponent(TranslationConstants.Beepedia.Spawning.WEIGHT, beeData.getSpawnData().getSpawnWeight());
            TranslationTextComponent lightName = new TranslationTextComponent(TranslationConstants.Beepedia.Spawning.LIGHT, BeepediaUtils.getLightName(beeData.getSpawnData().getLightLevel()));
            font.draw(matrix, groupName.withStyle(TextFormatting.GRAY), xPos, yPos + 22f, -1);
            font.draw(matrix, heightName.withStyle(TextFormatting.GRAY), xPos, yPos + 34f, -1);
            font.draw(matrix, weightName.withStyle(TextFormatting.GRAY), xPos, yPos + 46f, -1);
            font.draw(matrix, lightName.withStyle(TextFormatting.GRAY), xPos, yPos + 58f, -1);
        }
    }

    @Override
    public void addSearch() {
        biomeList.forEach(b -> {
            //todo add biome tags
            parent.addSearchBiome(b.getPath());
            parent.addSearchBiome(new TranslationTextComponent(String.format("biome.%s.%s", b.getNamespace(), b.getPath())).getString());
        });
        parent.addSearchBeeTag(BeepediaUtils.getLightName(beeData.getSpawnData().getLightLevel()).getString());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (!BeepediaScreen.currScreenState.isBiomesOpen()) return false;
        if (mouseX >= xPos && mouseY >= yPos + 22 && mouseX <= xPos + SUB_PAGE_WIDTH && mouseY <= yPos + SUB_PAGE_HEIGHT) {
            int boxHeight = SUB_PAGE_HEIGHT - 22;
            if (boxHeight > scrollHeight) {
                return true;
            }
            subScrollPos += scrollAmount * 8;
            if (subScrollPos > 0) subScrollPos = 0;
            else if (subScrollPos < -(scrollHeight - boxHeight)) {
                subScrollPos = -(scrollHeight - boxHeight);
            }
            BeepediaScreen.currScreenState.setSpawningScroll(subScrollPos);
            return true;
        }
        return false;
    }

    @Override
    public void openPage() {
        super.openPage();
        nextTab.visible = true;
        prevTab.visible = true;
        subScrollPos = BeepediaScreen.currScreenState.getSpawningScroll();
        int boxHeight = SUB_PAGE_HEIGHT - 22;
        if (boxHeight > scrollHeight) {
            BeepediaScreen.currScreenState.setSpawningScroll(0);
            subScrollPos = 0;
        }
    }

    @Override
    public void closePage() {
        nextTab.visible = false;
        prevTab.visible = false;
        super.closePage();
    }
}
