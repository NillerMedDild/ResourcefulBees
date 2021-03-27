package com.resourcefulbees.resourcefulbees.block;

import com.resourcefulbees.resourcefulbees.registry.ModTileEntityTypes;
import com.resourcefulbees.resourcefulbees.tileentity.CentrifugeTileEntity;
import com.resourcefulbees.resourcefulbees.utils.TooltipBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("deprecation")
public class CentrifugeBlock extends Block {
    public static final BooleanProperty PROPERTY_ON = BooleanProperty.create("on");

    public CentrifugeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PROPERTY_ON,false));
    }

    @NotNull
    @Override
    public ActionResultType use(@NotNull BlockState state, World world, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull Hand hand, @NotNull BlockRayTraceResult rayTraceResult) {
        ItemStack heldItem = player.getItemInHand(hand);
        boolean hasCapability = heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
        TileEntity tileEntity = world.getBlockEntity(pos);

        if (tileEntity instanceof CentrifugeTileEntity) {
            if (hasCapability) {
                tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                        .ifPresent(iFluidHandler -> FluidUtil.interactWithFluidHandler(player, hand, world, pos, null));
            } else if (!player.isShiftKeyDown() && !world.isClientSide) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, pos);
            }

            return ActionResultType.SUCCESS;
        }

        return super.use(state, world, pos, player, hand, rayTraceResult);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, World world, @NotNull BlockPos pos, @NotNull Block changedBlock, @NotNull BlockPos changedBlockPos, boolean bool) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof CentrifugeTileEntity) {
            CentrifugeTileEntity centrifugeTileEntity = (CentrifugeTileEntity) tileEntity;
            centrifugeTileEntity.setIsPoweredByRedstone(world.hasNeighborSignal(pos));
        }
    }

    @Nullable
    @Override
    public INamedContainerProvider getMenuProvider(@NotNull BlockState state, World worldIn, @NotNull BlockPos pos) {
        return (INamedContainerProvider)worldIn.getBlockEntity(pos);
    }

    @Override
    public void onRemove(@NotNull BlockState state1, World world, @NotNull BlockPos pos, @NotNull BlockState state, boolean isMoving) {
        TileEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CentrifugeTileEntity && state.getBlock() != state1.getBlock()){
            CentrifugeTileEntity centrifugeTileEntity = (CentrifugeTileEntity)blockEntity;
            ItemStackHandler h = centrifugeTileEntity.getItemStackHandler();
            IntStream.range(0, h.getSlots()).mapToObj(h::getStackInSlot).filter(s -> !s.isEmpty()).forEach(stack -> InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack));
        }
        super.onRemove(state1, world, pos, state, isMoving);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CentrifugeTileEntity(ModTileEntityTypes.CENTRIFUGE_ENTITY.get());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PROPERTY_ON);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable IBlockReader worldIn, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag flagIn) {
            tooltip.addAll(new TooltipBuilder()
                    .addTip(I18n.get("block.resourcefulbees.centrifuge.tooltip.info"), TextFormatting.GOLD)
                    .build());
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}



