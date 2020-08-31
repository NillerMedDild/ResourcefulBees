package com.resourcefulbees.resourcefulbees.item;

import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.config.Config;
import com.resourcefulbees.resourcefulbees.lib.NBTConstants;
import com.resourcefulbees.resourcefulbees.registry.ColorHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ResourcefulHoneycomb extends Item {

    public ResourcefulHoneycomb() {
        super(new Properties().group(ItemGroup.MATERIALS));
    }

    public static int getColor(ItemStack stack, int tintIndex){
        return ColorHandler.getItemColor(stack, tintIndex);
    }


    @Override
    public boolean isFood() {
        return true;
    }

    @Nullable
    @Override
    public Food getFood() {
        return new Food.Builder()
                .hunger(Config.HONEYCOMB_HUNGER.get())
                .saturation(Config.HONEYCOMB_SATURATION.get().floatValue())
                .build();
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack stack) {
        CompoundNBT beeType = stack.getChildTag(NBTConstants.NBT_ROOT);
        String name;
        if ((beeType != null && beeType.contains(NBTConstants.NBT_BEE_TYPE))) {
            name = String.format("item.%1$s.%2$s_honeycomb", ResourcefulBees.MOD_ID, beeType.getString(NBTConstants.NBT_BEE_TYPE));
        } else {
            name = String.format("item.%1$s.resourceful_honeycomb", ResourcefulBees.MOD_ID);
        }
        return name;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
       if (!stack.hasTag()) {
           ITextComponent craftingTip = new StringTextComponent("Unless otherwise specified,").mergeStyle(TextFormatting.AQUA);
           tooltip.add(craftingTip);
           craftingTip = new StringTextComponent("*ANY* resourceful honeycomb variant can be used for crafting recipes").mergeStyle(TextFormatting.AQUA);
           tooltip.add(craftingTip);
       }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}