package com.teamresourceful.resourcefulbees.common.item;

import com.teamresourceful.resourcefulbees.common.lib.constants.BeeConstants;
import com.teamresourceful.resourcefulbees.common.lib.constants.NBTConstants;
import com.teamresourceful.resourcefulbees.common.lib.constants.TranslationConstants;
import com.teamresourceful.resourcefulbees.common.utils.BeeInfoUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BeeBox extends Item {

    final boolean isTemp;

    public BeeBox(Properties properties, boolean isTemp) {
        super(properties);
        this.isTemp = isTemp;
    }

    @Override
    public @NotNull ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null) {
            World playerWorld = context.getPlayer().getCommandSenderWorld();
            ItemStack stack = context.getItemInHand();
            if (!context.getPlayer().isShiftKeyDown()) return ActionResultType.FAIL;
            if (playerWorld.isClientSide() || !isFilled(stack)) return ActionResultType.FAIL;
            World worldIn = context.getLevel();
            BlockPos pos = context.getClickedPos();
            List<Entity> entities = getEntitiesFromStack(stack, worldIn, true);
            for (Entity entity : entities) {
                if (entity != null) {
                    BlockPos blockPos = pos.relative(context.getClickedFace());
                    entity.absMoveTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, 0, 0);
                    worldIn.addFreshEntity(entity);
                    if (entity instanceof BeeEntity) {
                        BeeEntity beeEntity = (BeeEntity) entity;
                        BeeJar.resetBee(beeEntity);
                        BeeJar.setBeeAngry(beeEntity, player);
                    }
                }
            }
            if (isTemp) stack.shrink(1);
            else stack.setTag(null);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    public @NotNull List<Entity> getEntitiesFromStack(ItemStack stack, World world, boolean withInfo) {
        CompoundNBT tag = stack.getTag();
        List<Entity> entities = new ArrayList<>();
        if (tag != null) {
            ListNBT bees = tag.getList(NBTConstants.NBT_BEES, 10);
            bees.stream()
                    .map(CompoundNBT.class::cast)
                    .forEach(compoundNBT -> {
                        EntityType<?> type = EntityType.byString(compoundNBT.getCompound(NBTConstants.ENTITY_DATA).getString("id")).orElse(null);
                        if (type != null) {
                            Entity entity = type.create(world);
                            if (entity != null && withInfo) entity.load(compoundNBT.getCompound(NBTConstants.ENTITY_DATA));
                            entities.add(entity);
                        }
                    });
        }
        return entities;
    }

    @NotNull
    @Override
    public ActionResultType interactLivingEntity(@NotNull ItemStack stack, @NotNull PlayerEntity player, LivingEntity targetIn, @NotNull Hand hand) {
        if (targetIn.getCommandSenderWorld().isClientSide() || (!(targetIn instanceof BeeEntity) || !targetIn.isAlive())) {
            return ActionResultType.FAIL;
        }
        if (isTemp) return ActionResultType.FAIL;
        BeeEntity target = (BeeEntity) targetIn;

        CompoundNBT tag = stack.getTag() == null ? new CompoundNBT() : stack.getTag();

        ListNBT bees = tag.contains(NBTConstants.NBT_BEES) ? tag.getList(NBTConstants.NBT_BEES, 10) : new ListNBT();

        if (bees.size() == BeeConstants.MAX_BEES_BEE_BOX) return ActionResultType.FAIL;
        CompoundNBT entityData = new CompoundNBT();
        entityData.put(NBTConstants.ENTITY_DATA, BeeInfoUtils.createJarBeeTag(target, NBTConstants.NBT_ID));
        bees.add(entityData);
        tag.put(NBTConstants.NBT_BEES, bees);
        stack.setTag(tag);
        player.setItemInHand(hand, stack);
        player.swing(hand);
        target.remove(true);
        return ActionResultType.PASS;
    }

    public static boolean isFilled(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag() != null && stack.getTag().contains(NBTConstants.NBT_BEES);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable World world, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag tooltipFlag) {
        super.appendHoverText(stack, world, tooltip, tooltipFlag);
        if (isTemp) {
            tooltip.add(TranslationConstants.Items.TEMP_INFO.withStyle(TextFormatting.GOLD));
        } else {
            tooltip.add(TranslationConstants.Items.INFO.withStyle(TextFormatting.GOLD));
        }
        if (Screen.hasShiftDown() && isFilled(stack)) {
            tooltip.add(TranslationConstants.Items.BEES.withStyle(TextFormatting.YELLOW));

            //noinspection ConstantConditions
            stack.getTag().getList(NBTConstants.NBT_BEES, 10).stream()
                    .map(CompoundNBT.class::cast)
                    .forEach(compoundNBT -> {
                        String id = compoundNBT.getCompound(NBTConstants.ENTITY_DATA).getString("id");
                        EntityType<?> entityType = BeeInfoUtils.getEntityType(id);
                        ITextComponent name = entityType == null ? new StringTextComponent("NULL_ENTITY") : entityType.getDescription();
                        tooltip.add(new StringTextComponent("  - ").append(name).withStyle(TextFormatting.WHITE));
                    });
        } else if (isFilled(stack)) {
            tooltip.add(TranslationConstants.Items.MORE_INFO.withStyle(TextFormatting.YELLOW));
        }
    }
}
