package com.teamresourceful.resourcefulbees.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class Label extends TooltipWidget {

    public ITextComponent text;
    private final int color;

    public Label(ITextComponent text, int x, int y, int width, int height, int color, ITextComponent message) {
        super(x, y, width, height, message);
        this.text = text;
        this.color = color;
    }

    public Label(ITextComponent text, int x, int y) {
        this(text, x, y, -1);
    }

    public Label(ITextComponent text, int x, int y, ITextComponent message) {
        this(text, x, y, -1, message);
    }

    public Label(ITextComponent text, int x, int y, int color) {
        this(text, x, y, color, new StringTextComponent(""));
    }

    public Label(ITextComponent text, int x, int y, int color, ITextComponent message) {
        this(text, x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, color, message);
    }

    public Label(ITextComponent text, int x, int y, int width, int height, int color) {
        this(text, x, y, width, height, color, text);
    }

    public Label(ITextComponent text, int x, int y, int width, int height) {
        this(text, x, y, width, height, -1, text);
    }

    public Label(ITextComponent text, int x, int y, int width, int height, ITextComponent message) {
        this(text, x, y, width, height, -1, message);
    }

    @Override
    public void render(@NotNull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        Minecraft.getInstance().font.draw(matrixStack, text, this.x, this.y, color);
    }
}
