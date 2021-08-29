package com.teamresourceful.resourcefulbees.common.tileentity.multiblocks.centrifuge;

import com.teamresourceful.resourcefulbees.common.block.multiblocks.centrifuge.EliteCentrifugeCasingBlock;
import com.teamresourceful.resourcefulbees.common.capabilities.CustomEnergyStorage;
import com.teamresourceful.resourcefulbees.common.config.Config;
import com.teamresourceful.resourcefulbees.common.container.CentrifugeContainer;
import com.teamresourceful.resourcefulbees.common.container.EliteCentrifugeMultiblockContainer;
import com.teamresourceful.resourcefulbees.common.registry.minecraft.ModContainers;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class EliteCentrifugeControllerTileEntity extends CentrifugeControllerTileEntity {

    private static final int INPUTS = 6;
    private static final int TANK_CAPACITY = 50000;
    private final IIntArray times = new TimesArray(6);

    public EliteCentrifugeControllerTileEntity(TileEntityType<?> tileEntityType) { super(tileEntityType); }

    @Override
    public int getNumberOfInputs() { return INPUTS; }

    @Override
    public int getMaxTankCapacity() { return TANK_CAPACITY; }

    @Override
    public int getRecipeTime(int i) { return getRecipe(i) != null ? Math.max(5, (int)(getRecipe(i).getMultiblockTime() * 0.5)) : Config.GLOBAL_CENTRIFUGE_RECIPE_TIME.get(); }

    @Override
    protected CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(Config.MAX_CENTRIFUGE_RF.get() * 10, 1000, 0) {
            @Override
            protected void onEnergyChanged() { setChanged(); }
        };
    }

    @Override
    protected Predicate<BlockPos> validBlocks() {
        return blockPos -> {
            assert level != null : "Validating Centrifuge - How is world null??";
            Block block = level.getBlockState(blockPos).getBlock();
            TileEntity tileEntity = level.getBlockEntity(blockPos);
            if (block instanceof EliteCentrifugeCasingBlock && tileEntity instanceof EliteCentrifugeCasingTileEntity) {
                EliteCentrifugeCasingTileEntity casing = (EliteCentrifugeCasingTileEntity) tileEntity;
                return !casing.isLinked() || (casing.getController() != null && casing.getController().equals(this));
            }
            return false;
        };
    }

    @Nullable
    @Override
    public CentrifugeContainer createMenu(int id, @NotNull PlayerInventory playerInventory, @NotNull PlayerEntity playerEntity) {
        assert level != null;
        return new EliteCentrifugeMultiblockContainer(ModContainers.ELITE_CENTRIFUGE_MULTIBLOCK_CONTAINER.get(), id, level, worldPosition, playerInventory, times);
    }
}