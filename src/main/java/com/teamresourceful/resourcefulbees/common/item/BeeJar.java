package com.teamresourceful.resourcefulbees.common.item;

import com.teamresourceful.resourcefulbees.ResourcefulBees;
import com.teamresourceful.resourcefulbees.api.beedata.CustomBeeData;
import com.teamresourceful.resourcefulbees.api.beedata.render.RenderData;
import com.teamresourceful.resourcefulbees.api.beedata.traits.TraitData;
import com.teamresourceful.resourcefulbees.common.entity.passive.CustomBeeEntity;
import com.teamresourceful.resourcefulbees.common.entity.passive.ResourcefulBee;
import com.teamresourceful.resourcefulbees.common.lib.constants.BeeConstants;
import com.teamresourceful.resourcefulbees.common.lib.constants.NBTConstants;
import com.teamresourceful.resourcefulbees.common.lib.constants.TraitConstants;
import com.teamresourceful.resourcefulbees.common.mixin.accessors.BeeEntityAccessor;
import com.teamresourceful.resourcefulbees.common.registry.minecraft.ModItems;
import com.teamresourceful.resourcefulbees.common.utils.BeeInfoUtils;
import com.teamresourceful.resourcefulbees.common.utils.TextComponentCodec;
import com.teamresourceful.resourcefulbees.common.utils.color.Color;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BeeJar extends Item {
    public BeeJar(Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    public static int getColor(ItemStack stack, int tintIndex) {
        CompoundNBT tag = stack.getTag();
        if (tintIndex == 1 && tag != null) {
            if (tag.contains(NBTConstants.NBT_COLOR) && !tag.getString(NBTConstants.NBT_COLOR).equals(BeeConstants.STRING_DEFAULT_ITEM_COLOR)) {
                return Color.parse(tag.getString(NBTConstants.NBT_COLOR)).getValue();
            } else if (!tag.contains(NBTConstants.NBT_COLOR) && tag.contains(NBTConstants.NBT_ENTITY) && Minecraft.getInstance().level != null) {
                // one time check for a bee's color, if customBeeEntity, set the jar's color to the bee's color, else set it to default.
                // this code should only ever run once per beejar if the beejar does not have a color.
                String id = tag.getString(NBTConstants.NBT_ENTITY);
                EntityType<?> entityType = BeeInfoUtils.getEntityType(id);
                if (entityType != null) {
                    Entity entity = entityType.create(Minecraft.getInstance().level);
                    if (entity instanceof CustomBeeEntity) {
                        RenderData renderData = ((CustomBeeEntity) entity).getRenderData();
                        tag.putString(NBTConstants.NBT_COLOR, renderData.getColorData().getJarColor().toString());
                        return renderData.getColorData().getJarColor().getValue();
                    }
                }
                tag.putString(NBTConstants.NBT_COLOR, BeeConstants.STRING_DEFAULT_ITEM_COLOR);
                stack.setTag(tag);
            }
            return BeeConstants.VANILLA_BEE_INT_COLOR;
        }
        return BeeConstants.DEFAULT_ITEM_COLOR;
    }

    public static boolean isFilled(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag() != null && stack.getTag().contains(NBTConstants.NBT_ENTITY);
    }

    @Override
    public @NotNull ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null) {
            World playerWorld = context.getPlayer().getCommandSenderWorld();
            ItemStack stack = context.getItemInHand();
            if (playerWorld.isClientSide() || !isFilled(stack)) return ActionResultType.FAIL;
            World worldIn = context.getLevel();
            BlockPos pos = context.getClickedPos();
            Entity entity = getEntityFromStack(stack, worldIn, true);

            if (entity != null) {
                assert stack.getTag() != null;
                CompoundNBT display = stack.getTag().getCompound(NBTConstants.NBT_DISPLAY);
                if (!display.getString(NBTConstants.NBT_NAME).contains("item.resourcefulbees.bee_jar_filled")) {
                    entity.setCustomName(ITextComponent.Serializer.fromJson(display.getString(NBTConstants.NBT_NAME)));
                }
                if (entity instanceof BeeEntity) {
                    BeeEntity beeEntity = (BeeEntity) entity;
                    resetBee(beeEntity);
                    setBeeAngry(beeEntity, player);
                }
                BlockPos blockPos = pos.relative(context.getClickedFace());
                entity.absMoveTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, 0, 0);
                worldIn.addFreshEntity(entity);
            }
            if (player.isCreative()) return ActionResultType.SUCCESS;
            if (stack.getCount() > 1) {
                if (!player.addItem(new ItemStack(ModItems.BEE_JAR.get()))) {
                    player.drop(new ItemStack(ModItems.BEE_JAR.get()), false);
                }
                stack.shrink(1);
            } else {
                stack.setTag(null);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }


    public static void setBeeAngry(BeeEntity beeEntity, PlayerEntity player) {
        if (beeEntity.isAngry()) {
            beeEntity.setTarget(player);
            if (beeEntity instanceof ResourcefulBee) {
                ResourcefulBee customBee = (ResourcefulBee) beeEntity;
                TraitData traitData = customBee.getTraitData();
                if (traitData.getDamageTypes().stream().anyMatch(damageType -> damageType.getType().equals(TraitConstants.EXPLOSIVE))) {
                    customBee.setExplosiveCooldown(60);
                }
            }
        }
    }

    public static void resetBee(BeeEntity beeEntity) {
        beeEntity.setSavedFlowerPos(null);
        ((BeeEntityAccessor) beeEntity).setHivePos(null);
    }

    @Nullable
    public static Entity getEntityFromStack(ItemStack stack, World world, boolean withInfo) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            EntityType<?> type = EntityType.byString(tag.getString(NBTConstants.NBT_ENTITY)).orElse(null);
            if (type != null) {
                Entity entity = type.create(world);
                if (entity != null && withInfo) entity.load(tag);
                return entity;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public ActionResultType interactLivingEntity(@NotNull ItemStack stack, @NotNull PlayerEntity player, LivingEntity targetIn, @NotNull Hand hand) {
        if (targetIn.getCommandSenderWorld().isClientSide() || (!(targetIn instanceof BeeEntity) || !targetIn.isAlive()) || (isFilled(stack))) {
            return ActionResultType.FAIL;
        }

        BeeEntity target = (BeeEntity) targetIn;

        if (stack.getCount() > 1) {
            ItemStack newJar = new ItemStack(ModItems.BEE_JAR.get());
            newJar.setTag(BeeInfoUtils.createJarBeeTag(target, NBTConstants.NBT_ENTITY));
            stack.shrink(1);
            renameJar(newJar, target);
            if (!player.addItem(newJar)) {
                player.drop(newJar, false);
            }
        } else {
            stack.setTag(BeeInfoUtils.createJarBeeTag(target, NBTConstants.NBT_ENTITY));
            renameJar(stack, target);
        }
        player.setItemInHand(hand, stack);
        player.swing(hand);
        target.remove(true);
        return ActionResultType.PASS;
    }

    public static void renameJar(ItemStack stack, Entity target, String name) {
        if (stack.getTag() == null || stack.getTag().contains(NBTConstants.NBT_DISPLAY)) return;
        CompoundNBT nbt = stack.getOrCreateTag();
        ITextComponent beeName = target.getCustomName() != null ? target.getCustomName() : target.getDisplayName();
        TranslationTextComponent bottleName = new TranslationTextComponent(name);
        bottleName.append(" - ").append(beeName);
        bottleName.setStyle(Style.EMPTY.withItalic(false));
        CompoundNBT displayNBT = new CompoundNBT();
        displayNBT.putString("Name", ITextComponent.Serializer.toJson(bottleName));
        nbt.put(NBTConstants.NBT_DISPLAY, displayNBT);
    }

    public static void renameJar(ItemStack stack, BeeEntity target) {
        renameJar(stack, target, stack.getItem().getDescriptionId(stack));
    }

    @OnlyIn(Dist.CLIENT)
    public static void fillJar(ItemStack stack, CustomBeeData beeData) {
        EntityType<?> entityType = beeData.getEntityType();
        World world = Minecraft.getInstance().level;
        if (world == null) return;
        Entity entity = entityType.create(world);
        if (entity != null) {
            stack.setTag(BeeInfoUtils.createJarBeeTag((BeeEntity) entity, NBTConstants.NBT_ENTITY));
            renameJar(stack, (BeeEntity) entity);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renameJar(ItemStack stack, CompoundNBT tag, String name) {
        if (stack.getTag() == null || stack.getTag().contains(NBTConstants.NBT_DISPLAY)) return;
        if (Minecraft.getInstance().level == null) return;
        String id = tag.getString(NBTConstants.NBT_ENTITY);
        EntityType<?> entityType = BeeInfoUtils.getEntityType(id);
        if (entityType != null) {
            Entity entity = entityType.create(Minecraft.getInstance().level);
            if (entity != null) {
                renameJar(stack, entity, name);
            }
        }
    }

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack stack) {
        String name;
        if (isFilled(stack)) {
            name = "item." + ResourcefulBees.MOD_ID + ".bee_jar_filled";
            renameJar(stack, stack.getTag(), name);
        } else
            name = "item." + ResourcefulBees.MOD_ID + ".bee_jar_empty";
        return name;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable World world, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        CompoundNBT tag = stack.getTag();
        if (tag != null && isFilled(stack)) {
            String id = tag.getString(NBTConstants.NBT_ENTITY);
            EntityType<?> entityType = BeeInfoUtils.getEntityType(id);
            ITextComponent name = entityType == null ? new StringTextComponent("NULL_ENTITY") : entityType.getDescription();
            tooltip.add(new StringTextComponent("  - ").append(name).withStyle(TextFormatting.WHITE));
        }
    }
}