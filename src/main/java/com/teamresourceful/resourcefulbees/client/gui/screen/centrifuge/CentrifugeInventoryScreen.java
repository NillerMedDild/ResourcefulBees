package com.teamresourceful.resourcefulbees.client.gui.screen.centrifuge;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.containers.CentrifugeContainer;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.helpers.CentrifugeUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class CentrifugeInventoryScreen<T extends CentrifugeContainer<?>> extends BaseCentrifugeScreen<T> {

    private final int u; //this could probably get removed since the value is always zero unless components.png gets changed
    private final int v;

    protected CentrifugeInventoryScreen(T pMenu, PlayerInventory pPlayerInventory, ITextComponent pTitle, int u, int v) {
        super(pMenu, pPlayerInventory, pTitle);
        this.u = u;
        this.v = v;
    }

    @Override
    protected void renderBg(@NotNull MatrixStack matrix, float pPartialTicks, int pX, int pY) {
        super.renderBg(matrix, pPartialTicks, pX, pY);
        drawContainerSlots(matrix, leftPos, topPos);
        drawPlayerInventory(matrix, leftPos - 1 + CentrifugeContainer.INV_X_OFFSET, topPos - 1 + CentrifugeContainer.INV_Y_OFFSET);
    }

    protected void drawContainerSlots(@NotNull MatrixStack matrix, int x, int y) {
        drawSlotGrid(matrix, x + 161, y + 45, CentrifugeUtils.getRows(tier), CentrifugeUtils.getColumns(tier), u, v);
    }

    protected void drawPlayerInventory(@NotNull MatrixStack matrix, int x, int y) {
        // player inventory
        drawSlotGrid(matrix, x, y, 3, 9, 0, 72);
        //hotbar slots
        drawSlotGrid(matrix, x, y + 55, 1, 9, 0, 72);
    }

    protected void drawSlotGrid(MatrixStack matrix, int x, int y, int rows, int columns, int u, int v) {
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < columns; ++c) {
                drawSlot(matrix, x + c * 17, y + r * 17, u, v);
            }
        }
    }

    protected void drawSlot(MatrixStack matrix, int x, int y, int u, int v) {
        blit(matrix, x, y, u, v, 18, 18);
    }

    @Override
    @Nullable List<ITextComponent> getInfoTooltip() {
        return Lists.newArrayList(new StringTextComponent("INFO TEXT"));
    }

    @Override
    void closeScreen() {
        if (minecraft != null) minecraft.setScreen(null);
    }
}
