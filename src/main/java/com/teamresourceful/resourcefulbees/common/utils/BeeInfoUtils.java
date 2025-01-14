package com.teamresourceful.resourcefulbees.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.teamresourceful.resourcefulbees.ResourcefulBees;
import com.teamresourceful.resourcefulbees.api.ICustomBee;
import com.teamresourceful.resourcefulbees.api.beedata.CoreData;
import com.teamresourceful.resourcefulbees.api.beedata.CustomBeeData;
import com.teamresourceful.resourcefulbees.api.honeydata.HoneyBlockData;
import com.teamresourceful.resourcefulbees.api.honeydata.HoneyFluidData;
import com.teamresourceful.resourcefulbees.common.entity.passive.CustomBeeEntity;
import com.teamresourceful.resourcefulbees.common.fluids.CustomHoneyFluid;
import com.teamresourceful.resourcefulbees.common.item.BeeJar;
import com.teamresourceful.resourcefulbees.common.item.CustomHoneyBottleItem;
import com.teamresourceful.resourcefulbees.common.lib.constants.BeeConstants;
import com.teamresourceful.resourcefulbees.common.lib.constants.ModConstants;
import com.teamresourceful.resourcefulbees.common.lib.constants.NBTConstants;
import com.teamresourceful.resourcefulbees.common.registry.custom.HoneyRegistry;
import com.teamresourceful.resourcefulbees.common.registry.minecraft.ModFluids;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.teamresourceful.resourcefulbees.common.lib.constants.BeeConstants.VANILLA_BEE_COLOR;

public class BeeInfoUtils {

    private BeeInfoUtils() {
        throw new IllegalStateException(ModConstants.UTILITY_CLASS);
    }

    public static Pair<String, String> sortParents(String parent1, String parent2) {
        return parent1.compareTo(parent2) > 0 ? Pair.of(parent1, parent2) : Pair.of(parent2, parent1);
    }

    public static Effect getEffect(String effectName) {
        return ForgeRegistries.POTIONS.getValue(ResourceLocation.tryParse(effectName));
    }

    public static @Nullable EntityType<?> getEntityType(String entityName) {
        return ForgeRegistries.ENTITIES.getValue(ResourceLocation.tryParse(entityName));
    }

    public static @Nullable EntityType<?> getEntityType(ResourceLocation entityId) {
        return ForgeRegistries.ENTITIES.getValue(entityId);
    }

    public static Optional<ResourceLocation> getResourceLocation(String resource) {
        return Optional.ofNullable(ResourceLocation.tryParse(resource));
    }

    @Nullable
    public static ITag<Item> getItemTag(String itemTag) {
        return getResourceLocation(itemTag).map(ItemTags.getAllTags()::getTag).orElse(null);
    }

    @Nullable
    public static ITag<Fluid> getFluidTag(String fluidTag) {
        return getResourceLocation(fluidTag).map(FluidTags.getAllTags()::getTag).orElse(null);
    }

    @Nullable
    public static ITag<Block> getBlockTag(String blockTag) {
        return getResourceLocation(blockTag).map(BlockTags.getAllTags()::getTag).orElse(null);
    }

    public static void flagBeesInRange(BlockPos pos, World world) {
        MutableBoundingBox box = MutableBoundingBox.createProper(pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10, pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10);
        AxisAlignedBB aabb = AxisAlignedBB.of(box);
        if (world != null) {
            List<CustomBeeEntity> list = world.getEntitiesOfClass(CustomBeeEntity.class, aabb);
            list.forEach(customBeeEntity -> customBeeEntity.setHasHiveInRange(true));
        }
    }

    public static List<String> getLoreLines(CompoundNBT outputNBT) {
        if (outputNBT.isEmpty()) return new LinkedList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(outputNBT.toString());
        String nbtString = "NBT: " + gson.toJson(je);
        return Arrays.asList(nbtString.split("\n"));
    }

    public static void ageBee(int ticksInHive, BeeEntity beeEntity) {
        int i = beeEntity.getAge();
        if (i < 0) {
            beeEntity.setAge(Math.min(0, i + ticksInHive));
        } else if (i > 0) {
            beeEntity.setAge(Math.max(0, i - ticksInHive));
        }

        if (beeEntity instanceof CustomBeeEntity) {
            ((CustomBeeEntity) beeEntity).setLoveTime(Math.max(0, beeEntity.getInLoveTime() - ticksInHive));
        } else {
            beeEntity.setInLoveTime(Math.max(0, beeEntity.getInLoveTime() - ticksInHive));
        }
        beeEntity.resetTicksWithoutNectarSinceExitingHive();
    }

    public static void setEntityLocationAndAngle(BlockPos blockpos, Direction direction, Entity entity) {
        EntitySize size = entity.getDimensions(Pose.STANDING);
        double d0 = 0.65D + size.width / 2.0F;
        double d1 = blockpos.getX() + 0.5D + d0 * direction.getStepX();
        double d2 = blockpos.getY() + Math.max(0.5D - (size.height / 2.0F), 0);
        double d3 = blockpos.getZ() + 0.5D + d0 * direction.getStepZ();
        entity.moveTo(d1, d2, d3, entity.yRot, entity.xRot);
    }

    public static @NotNull CompoundNBT createJarBeeTag(BeeEntity beeEntity, String nbtTagID) {
        String type = EntityType.getKey(beeEntity.getType()).toString();
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(nbtTagID, type);

        beeEntity.saveWithoutId(nbt);

        String beeColor = VANILLA_BEE_COLOR;

        if (beeEntity instanceof ICustomBee) {
            ICustomBee iCustomBee = (ICustomBee) beeEntity;
            nbt.putString(NBTConstants.NBT_BEE_TYPE, iCustomBee.getBeeType());
            beeColor = iCustomBee.getRenderData().getColorData().getJarColor().toString();
        }

        nbt.putString(NBTConstants.NBT_COLOR, beeColor);

        return nbt;
    }

    public static boolean isBeeInJarOurs(@NotNull ItemStack stack) {
        //noinspection ConstantConditions
        return BeeJar.isFilled(stack) && stack.getTag().getString(NBTConstants.NBT_ENTITY).startsWith(ResourcefulBees.MOD_ID);
    }

    public static Fluid getHoneyFluidFromBottle(ItemStack bottleOutput) {
        Item item = bottleOutput.getItem();
        if (item == Items.HONEY_BOTTLE) {
            return ModFluids.HONEY_STILL.get().getSource();
        } else if (item instanceof CustomHoneyBottleItem) {
            CustomHoneyBottleItem honey = (CustomHoneyBottleItem) item;
            HoneyFluidData fluidData = HoneyRegistry.getRegistry().getHoneyData(honey.getHoneyData().getName()).getFluidData();
            return fluidData.getStillFluid().get().getSource();
        }
        return Fluids.EMPTY;
    }

    public static Item getHoneyBottleFromFluid(Fluid fluid) {
        if (fluid instanceof CustomHoneyFluid) {
            CustomHoneyFluid customfluid = (CustomHoneyFluid) fluid;
            return HoneyRegistry.getRegistry().getHoneyData(customfluid.getHoneyData().getName()).getBottleData().getHoneyBottle().get();
        } else {
            return Items.HONEY_BOTTLE;
        }
    }

    public static Item getHoneyBlockFromFluid(Fluid fluid) {
        if (fluid instanceof CustomHoneyFluid) {
            CustomHoneyFluid customfluid = (CustomHoneyFluid) fluid;
            HoneyBlockData blockData = HoneyRegistry.getRegistry().getHoneyData(customfluid.getHoneyData().getName()).getBlockData();
            if (blockData.getBlockItem() == null) return Items.AIR;
            return blockData.getBlockItem().get();
        } else {
            return Items.HONEY_BLOCK;
        }
    }

}
