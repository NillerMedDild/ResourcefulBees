package com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.containers;

import com.teamresourceful.resourcefulbees.common.inventory.slots.FilterSlot;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.blocks.CentrifugeInput;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.entities.CentrifugeInputEntity;
import com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.helpers.CentrifugeUtils;
import com.teamresourceful.resourcefulbees.common.registry.minecraft.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CentrifugeInputContainer extends CentrifugeContainer<CentrifugeInputEntity> {

    public CentrifugeInputContainer(int id, PlayerInventory inv, PacketBuffer buffer) {
        this(id, inv, getTileFromBuf(buffer, CentrifugeInputEntity.class));
    }

    public CentrifugeInputContainer(int id, PlayerInventory inv, CentrifugeInputEntity entity) {
        super(ModContainers.CENTRIFUGE_INPUT_CONTAINER.get(), id, inv, entity);
    }

    protected void setupSlots() {
        if (entity != null) {
            this.addSlot(new FilterSlot(entity.getFilterInventory(), CentrifugeInputEntity.RECIPE_SLOT, 126, 64) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return CentrifugeUtils.getRecipe(level, stack).isPresent();
                }
            });

            for (int r = 0; r < CentrifugeUtils.getRows(tier); r++) {
                for (int c = 0; c < CentrifugeUtils.getColumns(tier); c++) {
                    this.addSlot(new SlotItemHandler(entity.getInventoryHandler(), c + r * 4, 162 + c * 17, 46 + r * 17));
                }
            }
        }

        addPlayerInvSlots();
    }

    @Override
    public @NotNull ItemStack clicked(int pSlotId, int pDragType, @NotNull ClickType pClickType, @NotNull PlayerEntity pPlayer) {
        if (pSlotId == CentrifugeInputEntity.RECIPE_SLOT) {
            switch (pClickType) {
                case PICKUP:
                case PICKUP_ALL:
                case SWAP: {
                    FilterSlot slot = (FilterSlot) this.getSlot(0);
                    ItemStack stack = pPlayer.inventory.getCarried();
                    if (stack.getCount() > 0 && slot.mayPlace(stack)) {
                        ItemStack copy = stack.copy();
                        copy.setCount(1);
                        slot.set(copy);
                    } else if (slot.getItem().getCount() > 0) {
                        slot.set(ItemStack.EMPTY);
                    }
                    if (entity != null) entity.updateRecipe();
                    return slot.getItem().copy();
                }
                default:
                    return ItemStack.EMPTY;
            }
        }
        return super.clicked(pSlotId, pDragType, pClickType, pPlayer);
    }

    @Override
    public boolean stillValid(@NotNull PlayerEntity player) {
        assert entity != null;
        return IWorldPosCallable.create(level, entity.getBlockPos()).evaluate((world, pos) ->
                world.getBlockState(pos).getBlock() instanceof CentrifugeInput && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D, true);
    }

    @Override
    public int getContainerInputEnd() {
        return tier.getSlots();
    }

    @Override
    public int getInventoryStart() {
        return getContainerInputEnd();
    }

    @Override
    public int getContainerInputStart() {
        return 1;
    }
}
