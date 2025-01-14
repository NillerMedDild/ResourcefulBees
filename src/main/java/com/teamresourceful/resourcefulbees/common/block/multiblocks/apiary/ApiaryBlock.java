package com.teamresourceful.resourcefulbees.common.block.multiblocks.apiary;

import com.teamresourceful.resourcefulbees.common.config.CommonConfig;
import com.teamresourceful.resourcefulbees.common.lib.constants.TranslationConstants;
import com.teamresourceful.resourcefulbees.common.lib.enums.ApiaryOutputType;
import com.teamresourceful.resourcefulbees.common.tileentity.multiblocks.apiary.ApiaryTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class ApiaryBlock extends Block {

  public static final DirectionProperty FACING = HorizontalBlock.FACING;
  public static final BooleanProperty VALIDATED = BooleanProperty.create("validated");

  private final int tier;

  public ApiaryBlock(final int tier, float hardness, float resistance) {
    super(AbstractBlock.Properties.of(Material.METAL).strength(hardness, resistance).sound(SoundType.METAL));
    this.tier = tier;
    this.registerDefaultState(this.stateDefinition.any().setValue(VALIDATED, false).setValue(FACING, Direction.NORTH));
  }

  @Override
  public @NotNull ActionResultType use(@NotNull BlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull Hand handIn, @NotNull BlockRayTraceResult hit) {
    if (!player.isShiftKeyDown() && !world.isClientSide) {
      INamedContainerProvider blockEntity = state.getMenuProvider(world,pos);
      NetworkHooks.openGui((ServerPlayerEntity) player, blockEntity, pos);
    }
    return ActionResultType.SUCCESS;
  }

  @Override
  public BlockState getStateForPlacement(BlockItemUseContext context) {
    if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
    return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(VALIDATED, FACING);
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new ApiaryTileEntity();
  }

  @Nullable
  @Override
  public INamedContainerProvider getMenuProvider(@NotNull BlockState state, World worldIn, @NotNull BlockPos pos) {
    return (INamedContainerProvider)worldIn.getBlockEntity(pos);
  }

  @Override
  public void setPlacedBy(World worldIn, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
    TileEntity tile = worldIn.getBlockEntity(pos);
    if(tile instanceof ApiaryTileEntity) {
      ApiaryTileEntity apiaryTileEntity = (ApiaryTileEntity) tile;
      apiaryTileEntity.setTier(tier);
    }
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void appendHoverText(@NotNull ItemStack stack, @Nullable IBlockReader worldIn, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag flagIn) {
    if(Screen.hasShiftDown())
    {
      tooltip.add(new TranslationTextComponent(TranslationConstants.BeeHive.MAX_BEES, CommonConfig.APIARY_MAX_BEES.get())
              .append(TranslationConstants.BeeHive.UNIQUE.withStyle(TextFormatting.BOLD))
              .withStyle(TextFormatting.GOLD)
      );

      if (tier >= 0) {
        int timeReduction = (int)((0.1 + (tier * .1)) * 100);
        tooltip.add(new TranslationTextComponent(TranslationConstants.BeeHive.HIVE_TIME, "-", timeReduction).withStyle(TextFormatting.GOLD));
      }
      ApiaryOutputType outputTypeEnum;
      int outputQuantity;

      switch (tier) {
        case 8:
          outputTypeEnum = CommonConfig.T4_APIARY_OUTPUT.get();
          outputQuantity = CommonConfig.T4_APIARY_QUANTITY.get();
          break;
        case 7:
          outputTypeEnum = CommonConfig.T3_APIARY_OUTPUT.get();
          outputQuantity = CommonConfig.T3_APIARY_QUANTITY.get();
          break;
        case 6:
          outputTypeEnum = CommonConfig.T2_APIARY_OUTPUT.get();
          outputQuantity = CommonConfig.T2_APIARY_QUANTITY.get();
          break;
        default:
          outputTypeEnum = CommonConfig.T1_APIARY_OUTPUT.get();
          outputQuantity = CommonConfig.T1_APIARY_QUANTITY.get();
      }

      TranslationTextComponent outputType = outputTypeEnum.equals(ApiaryOutputType.COMB) ? TranslationConstants.Apiary.HONEYCOMB : TranslationConstants.Apiary.HONEYCOMB_BLOCK;

      tooltip.add(new TranslationTextComponent(TranslationConstants.Apiary.OUTPUT_TYPE, outputType).withStyle(TextFormatting.GOLD));
      tooltip.add(new TranslationTextComponent(TranslationConstants.Apiary.OUTPUT_QUANTITY, outputQuantity).withStyle(TextFormatting.GOLD));
    }
    else if (Screen.hasControlDown()){
      tooltip.add(TranslationConstants.Apiary.STRUCTURE_SIZE.withStyle(TextFormatting.AQUA));
      tooltip.add(TranslationConstants.Apiary.REQUISITES.withStyle(TextFormatting.AQUA));
      tooltip.add(TranslationConstants.Apiary.DROPS.withStyle(TextFormatting.AQUA));
      tooltip.add(TranslationConstants.Apiary.TAGS.withStyle(TextFormatting.AQUA));
      tooltip.add(TranslationConstants.Apiary.OFFSET.withStyle(TextFormatting.AQUA));
      tooltip.add(TranslationConstants.Apiary.LOCK.withStyle(TextFormatting.AQUA));
      tooltip.add(TranslationConstants.Apiary.LOCK2.withStyle(TextFormatting.AQUA));
    } else {
      tooltip.add(TranslationConstants.Items.MORE_INFO.withStyle(TextFormatting.YELLOW));
      tooltip.add(TranslationConstants.Items.MULTIBLOCK_INFO.withStyle(TextFormatting.AQUA));
    }

    super.appendHoverText(stack, worldIn, tooltip, flagIn);
  }
}
