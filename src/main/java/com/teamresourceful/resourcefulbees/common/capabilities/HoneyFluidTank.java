package com.teamresourceful.resourcefulbees.common.capabilities;

import com.teamresourceful.resourcefulbees.common.lib.constants.ModConstants;
import com.teamresourceful.resourcefulbees.common.utils.BeeInfoUtils;
import com.teamresourceful.resourcefulbees.common.lib.constants.ModTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class HoneyFluidTank extends FluidTank {

    private static final Predicate<FluidStack> FLUID_VALIDATOR = fluidStack -> fluidStack.getFluid().is(ModTags.Fluids.HONEY);

    public HoneyFluidTank(int capacity) {
        this(capacity, FLUID_VALIDATOR);
    }

    public HoneyFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    public void fillBottle(PlayerEntity player, Hand hand) {
        FluidStack fluidStack = new FluidStack(this.getFluid(), ModConstants.HONEY_PER_BOTTLE);
        ItemStack itemStack = new ItemStack(BeeInfoUtils.getHoneyBottleFromFluid(this.getFluid().getFluid()), 1);
        if (this.isEmpty()) return;
        if (this.getFluidAmount() >= ModConstants.HONEY_PER_BOTTLE) {
            this.drain(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getCount() > 1) {
                stack.setCount(stack.getCount() - 1);
                player.addItem(itemStack);
            } else {
                player.setItemInHand(hand, itemStack);
            }
            player.level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    public void emptyBottle(PlayerEntity player, Hand hand) {
        FluidStack fluidStack = new FluidStack(BeeInfoUtils.getHoneyFluidFromBottle(player.getItemInHand(hand)), ModConstants.HONEY_PER_BOTTLE);
        if (!this.getFluid().isFluidEqual(fluidStack) && !this.isEmpty()) {
            return;
        }
        if (this.getFluidAmount() + ModConstants.HONEY_PER_BOTTLE <= this.getTankCapacity(0)) {
            this.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getCount() > 1) {
                stack.setCount(stack.getCount() - 1);
                player.addItem(new ItemStack(Items.GLASS_BOTTLE, 1));
            } else {
                player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE, 1));
            }
            player.level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }
}