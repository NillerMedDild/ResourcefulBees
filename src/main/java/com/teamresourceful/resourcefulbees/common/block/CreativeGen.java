package com.teamresourceful.resourcefulbees.common.block;

import com.teamresourceful.resourcefulbees.common.tileentity.CreativeGenTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.Nullable;


public class CreativeGen extends Block {
    public CreativeGen(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CreativeGenTileEntity();
    }
}
