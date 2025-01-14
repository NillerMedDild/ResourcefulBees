package com.teamresourceful.resourcefulbees.common.entity.passive;

import com.teamresourceful.resourcefulbees.api.beedata.CustomBeeData;
import com.teamresourceful.resourcefulbees.api.beedata.mutation.MutationData;
import com.teamresourceful.resourcefulbees.api.beedata.outputs.BlockOutput;
import com.teamresourceful.resourcefulbees.api.beedata.outputs.EntityOutput;
import com.teamresourceful.resourcefulbees.api.beedata.outputs.ItemOutput;
import com.teamresourceful.resourcefulbees.api.beedata.traits.TraitData;
import com.teamresourceful.resourcefulbees.common.config.CommonConfig;
import com.teamresourceful.resourcefulbees.common.entity.goals.*;
import com.teamresourceful.resourcefulbees.common.lib.constants.NBTConstants;
import com.teamresourceful.resourcefulbees.common.lib.constants.TraitConstants;
import com.teamresourceful.resourcefulbees.common.mixin.accessors.BeeEntityAccessor;
import com.teamresourceful.resourcefulbees.common.mixin.invokers.BeeEntityInvoker;
import com.teamresourceful.resourcefulbees.common.mixin.invokers.FindBeehiveGoalInvoker;
import com.teamresourceful.resourcefulbees.common.registry.custom.BeeRegistry;
import com.teamresourceful.resourcefulbees.common.registry.custom.TraitAbilityRegistry;
import com.teamresourceful.resourcefulbees.common.tileentity.TieredBeehiveTileEntity;
import com.teamresourceful.resourcefulbees.common.tileentity.multiblocks.apiary.ApiaryTileEntity;
import com.teamresourceful.resourcefulbees.common.utils.RandomCollection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResourcefulBee extends CustomBeeEntity {
    private boolean wasColliding;
    private int numberOfMutations;
    private BeePollinateGoal pollinateGoal;
    private int explosiveCooldown = 0;

    public ResourcefulBee(EntityType<? extends BeeEntity> type, World world, String beeType) {
        super(type, world, beeType);
    }

    @Override
    protected void registerGoals() {
        //this method is called before constructor finishes making passed data impossible to access.
        String namespaceID = this.getEncodeId();
        assert namespaceID != null;
        String beeType = namespaceID.substring(namespaceID.lastIndexOf(":") + 1, namespaceID.length() - 4);
        CustomBeeData customBeeData = BeeRegistry.getRegistry().getBeeData(beeType);
        boolean isPassive = customBeeData.getCombatData().isPassive();
        boolean isBreedable = customBeeData.getBreedData().hasParents();
        boolean hasMutation = customBeeData.getMutationData().hasMutation();

        if (!isPassive) {
            this.goalSelector.addGoal(0, new BeeEntity.StingGoal(this, 1.4, true));
            this.targetSelector.addGoal(1, (new BeeAngerGoal(this)).setAlertOthers());
            this.targetSelector.addGoal(2, new BeeEntity.AttackPlayerGoal(this));
        }
        this.goalSelector.addGoal(1, new EnterBeehiveGoal2());

        if (isBreedable) {
            this.goalSelector.addGoal(2, new BeeBreedGoal(this, 1.0D, beeType));
            this.goalSelector.addGoal(3, new BeeTemptGoal(this, 1.25D));
            this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        }
        this.pollinateGoal = new BeePollinateGoal(this);
        this.goalSelector.addGoal(4, this.pollinateGoal);

        ((BeeEntityAccessor)this).setPollinateGoal(new FakePollinateGoal());

        this.goalSelector.addGoal(5, new ResourcefulBee.UpdateBeehiveGoal2());

        ((BeeEntityAccessor)this).setGoToHiveGoal(new ResourcefulBee.FindBeehiveGoal2());
        this.goalSelector.addGoal(5, ((BeeEntityAccessor)this).getGoToHiveGoal());

        ((BeeEntityAccessor)this).setGoToKnownFlowerGoal(new BeeEntity.FindFlowerGoal());
        this.goalSelector.addGoal(6, ((BeeEntityAccessor)this).getGoToKnownFlowerGoal());

        if (hasMutation) {
            this.goalSelector.addGoal(7, new ResourcefulBee.FindPollinationTargetGoal2());
        }
        if (Boolean.FALSE.equals(CommonConfig.MANUAL_MODE.get())) this.goalSelector.addGoal(8, new BeeWanderGoal(this));
        this.goalSelector.addGoal(9, new SwimGoal(this));
    }

    @Override
    public boolean isHiveValid() {
        if (!this.hasHive()) {
            return false;
        } else {
            BlockPos pos = this.getHivePos();
            if (pos != null) {
                TileEntity blockEntity = this.level.getBlockEntity(pos);
                return blockEntity instanceof TieredBeehiveTileEntity && ((TieredBeehiveTileEntity) blockEntity).isAllowedBee()
                        || blockEntity instanceof ApiaryTileEntity && ((ApiaryTileEntity) blockEntity).isAllowedBee()
                        || blockEntity instanceof BeehiveTileEntity;
            } else
                return false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (explosiveCooldown > 0) explosiveCooldown--;
    }

    public void setExplosiveCooldown(int cooldown) {
        this.explosiveCooldown = cooldown;
    }

    @Override
    public void dropOffNectar() {
        super.dropOffNectar();
        this.resetCropCounter();
    }

    @Override
    public boolean wantsToEnterHive() {
        if (((BeeEntityAccessor)this).getStayOutOfHiveCountdown() <= 0 && !this.pollinateGoal.isPollinating() && !this.hasStung() && this.getTarget() == null) {
            boolean flag = ((BeeEntityInvoker)this).callIsTiredOfLookingForNectar() || this.hasNectar();
            return flag && !((BeeEntityInvoker)this).callIsHiveNearFire();
        } else {
            return false;
        }
    }

    private void resetCropCounter() {
        this.numberOfMutations = 0;
    }

    @Override
    public void incrementNumCropsGrownSincePollination() {
        ++this.numberOfMutations;
    }

    public void applyPollinationEffect() {
        MutationData mutationData = getMutationData();
        if (mutationData.hasMutation()) {
            if (!mutationData.getEntityMutations().isEmpty() && mutateEntity()) return;

            for (int i = 1; i <= 2; ++i) {
                BlockPos beePosDown = this.blockPosition().below(i);
                BlockState state = level.getBlockState(beePosDown);

                if (mutateBlock(mutationData, state, beePosDown)) {
                    break;
                }
            }
        }
    }

    public boolean mutateEntity() {
        AxisAlignedBB box = this.getMutationBoundingBox();
        List<Entity> entityList = this.level.getEntities(this, box, entity ->
                getMutationData().getEntityMutations().get(entity.getType()) != null);
        if (!entityList.isEmpty()) {
            Map<EntityType<?>, RandomCollection<EntityOutput>> entityMutations = getMutationData().getEntityMutations();
            RandomCollection<EntityOutput> output = entityMutations.get(entityList.get(0).getType());

            if (output != null) {
                EntityOutput entityOutput = output.next();
                if (entityOutput.getChance() >= level.random.nextFloat()) {
                    CompoundNBT nbt = new CompoundNBT();
                    entityOutput.getCompoundNBT().ifPresent(tag -> nbt.put("EntityTag", tag));
                    entityOutput.getEntityType().spawn((ServerWorld) level, nbt, null, null, entityList.get(0).blockPosition(), SpawnReason.NATURAL, false, false);
                    entityList.get(0).remove();
                    level.levelEvent(2005, this.blockPosition().below(1), 0);
                }
                incrementNumCropsGrownSincePollination();
                return true;
            }
        }
        return false;
    }

    public boolean mutateBlock(MutationData mutationData, BlockState state, BlockPos blockPos) {
        Block block = state.getBlock();
        if (mutationData.getBlockMutations().containsKey(block)) {
            mutateBlock(mutationData.getBlockMutations().get(block).next(), blockPos);
            incrementNumCropsGrownSincePollination();
            return true;
        }

        if (mutationData.getItemMutations().containsKey(block)) {
            mutateItem(mutationData.getItemMutations().get(block).next(), blockPos);
            incrementNumCropsGrownSincePollination();
            return true;
        }
        return false;
    }

    private void mutateBlock(BlockOutput output, BlockPos blockPos) {
        if (output.getChance() >= level.random.nextFloat()) {

            level.setBlockAndUpdate(blockPos, output.getBlock().defaultBlockState());
            output.getCompoundNBT().ifPresent(nbt -> {
                TileEntity tile = level.getBlockEntity(blockPos);
                if (tile != null) {
                    nbt.putInt("x", blockPos.getX());
                    nbt.putInt("y", blockPos.getY());
                    nbt.putInt("z", blockPos.getZ());
                    tile.load(output.getBlock().defaultBlockState(), nbt);
                }
            });
        }
    }

    private void mutateItem(ItemOutput output, BlockPos blockPos) {
        if (output.getChance() >= level.random.nextFloat()) {
            level.levelEvent(2005, blockPos, 0);
            level.removeBlock(blockPos, false);
            level.addFreshEntity(new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), output.getItemStack()));
        }
    }

    private AxisAlignedBB getMutationBoundingBox() {
        return getBoundingBox().expandTowards(new Vector3d(0, -2, 0));
    }

    @Override
    public boolean isFlowerValid(@NotNull BlockPos pos) {
        if (getCoreData().getEntityFlower().isPresent()) {
            return this.level.getEntity(this.getFlowerEntityID()) != null;
        } else if (!getCoreData().getBlockFlowers().isEmpty()) {
            return this.level.isLoaded(pos) && getCoreData().getBlockFlowers().contains(this.level.getBlockState(pos).getBlock());
        }
        return false;
    }

    @Override
    public void makeStuckInBlock(BlockState state, @NotNull Vector3d vector) {
        TraitData info = getTraitData();
        boolean isSpider = info.hasSpecialAbilities() && info.getSpecialAbilities().contains(TraitConstants.SPIDER);
        if (state.is(Blocks.COBWEB) && isSpider) return;
        super.makeStuckInBlock(state, vector);
    }

    @Override
    protected void customServerAiStep() {
        TraitData info = getTraitData();
        TraitAbilityRegistry registry = TraitAbilityRegistry.getRegistry();

        if (!info.hasTraits() || !info.hasSpecialAbilities()) {
            super.customServerAiStep();
            return;
        }

        info.getSpecialAbilities().stream()
                .map(registry::getAbility)
                .filter(Objects::nonNull)
                .forEach(consumer -> consumer.accept(this));

        super.customServerAiStep();
    }

    public void setColliding() {
        wasColliding = true;
    }

    public boolean wasColliding() {
        return wasColliding;
    }

    public boolean isPollinating() {
        return this.pollinateGoal.isPollinating();
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entityIn) {
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        TraitData info = getTraitData();
        boolean flag = entityIn.hurt(DamageSource.sting(this), damage);
        if (flag) {
            this.doEnchantDamageEffects(this, entityIn);
            if (entityIn instanceof LivingEntity) {
                ((LivingEntity) entityIn).setStingerCount(((LivingEntity) entityIn).getStingerCount() + 1);
                int i = getDifficultyModifier();
                if (info.hasTraits() && info.hasDamageTypes()) {
                    info.getDamageTypes().forEach(damageType -> {
                        if (damageType.getType().equals(TraitConstants.SET_ON_FIRE))
                            entityIn.setSecondsOnFire(i * damageType.getAmplifier());
                        if (damageType.getType().equals(TraitConstants.EXPLOSIVE))
                            this.explode(i / damageType.getAmplifier());
                    });
                }
                if (i > 0 && info.hasTraits() && info.hasPotionDamageEffects()) {
                    info.getPotionDamageEffects().forEach(damageEffect -> ((LivingEntity) entityIn).addEffect(new EffectInstance(damageEffect.getEffect(), i * 20, damageEffect.getStrength())));
                }
                if (canPoison(info))
                    ((LivingEntity) entityIn).addEffect(new EffectInstance(Effects.POISON, i * 20, 0));
            }

            this.stopBeingAngry();
            ((BeeEntityInvoker) this).callSetFlag(4, CommonConfig.BEE_DIES_FROM_STING.get() && this.getCombatData().removeStingerOnAttack());
            this.playSound(SoundEvents.BEE_STING, 1.0F, 1.0F);
        }

        return flag;
    }

    private int getDifficultyModifier() {
        switch (this.level.getDifficulty()) {
            case EASY: return 5;
            case NORMAL: return 10;
            case HARD: return 18;
            default: return 0;
        }
    }

    private boolean canPoison(TraitData info) {
        return (CommonConfig.BEES_INFLICT_POISON.get() && this.getCombatData().inflictsPoison()) && info.hasTraits() && !info.hasPotionDamageEffects() && !info.hasDamageTypes();
    }

    private void explode(int radius) {
        if (!this.level.isClientSide) {
            Explosion.Mode mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this) ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
            this.dead = true;
            this.level.explode(this, this.getX(), this.getY(), this.getZ(), random.nextFloat() * radius, explosiveCooldown > 0 ? Explosion.Mode.NONE : mode);
            this.remove();
            this.spawnLingeringCloud();
        }
    }

    private void spawnLingeringCloud() {
        Collection<EffectInstance> collection = this.getActiveEffects();
        if (!collection.isEmpty()) {
            AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(this.level, this.getX(), this.getY(), this.getZ());
            areaeffectcloudentity.setRadius(2.5F);
            areaeffectcloudentity.setRadiusOnUse(-0.5F);
            areaeffectcloudentity.setWaitTime(10);
            areaeffectcloudentity.setDuration(areaeffectcloudentity.getDuration() / 2);
            areaeffectcloudentity.setRadiusPerTick(-areaeffectcloudentity.getRadius() / areaeffectcloudentity.getDuration());

            for (EffectInstance effectinstance : collection) {
                areaeffectcloudentity.addEffect(new EffectInstance(effectinstance));
            }

            this.level.addFreshEntity(areaeffectcloudentity);
        }

    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.numberOfMutations = compound.getInt(NBTConstants.NBT_MUTATION_COUNT);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(NBTConstants.NBT_MUTATION_COUNT, this.numberOfMutations);
    }

    public class EnterBeehiveGoal2 extends BeeEntity.EnterBeehiveGoal {
        public EnterBeehiveGoal2() {
            //constructor
        }

        @Override
        public boolean canBeeUse() {
            if (ResourcefulBee.this.hasHive() && ResourcefulBee.this.wantsToEnterHive() && ResourcefulBee.this.getHivePos() != null && ResourcefulBee.this.getHivePos().closerThan(ResourcefulBee.this.position(), 2.0D)) {
                TileEntity blockEntity = ResourcefulBee.this.level.getBlockEntity(ResourcefulBee.this.getHivePos());
                if (blockEntity instanceof BeehiveTileEntity) {
                    BeehiveTileEntity beehiveBlockEntity = (BeehiveTileEntity) blockEntity;
                    if (!beehiveBlockEntity.isFull()) {
                        return true;
                    }

                    ((BeeEntityAccessor)ResourcefulBee.this).setHivePos(null);
                } else if (blockEntity instanceof ApiaryTileEntity) {
                    ApiaryTileEntity apiaryTileEntity = (ApiaryTileEntity) blockEntity;
                    if (apiaryTileEntity.hasSpace()) {
                        return true;
                    }

                    ((BeeEntityAccessor)ResourcefulBee.this).setHivePos(null);
                }
            }

            return false;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            if (ResourcefulBee.this.getHivePos() != null) {
                TileEntity blockEntity = ResourcefulBee.this.level.getBlockEntity(ResourcefulBee.this.getHivePos());
                if (blockEntity != null) {
                    if (blockEntity instanceof BeehiveTileEntity) {
                        BeehiveTileEntity beehiveBlockEntity = (BeehiveTileEntity) blockEntity;
                        beehiveBlockEntity.addOccupant(ResourcefulBee.this, ResourcefulBee.this.hasNectar());
                    } else if (blockEntity instanceof ApiaryTileEntity) {
                        ApiaryTileEntity apiaryTileEntity = (ApiaryTileEntity) blockEntity;
                        apiaryTileEntity.tryEnterHive(ResourcefulBee.this, ResourcefulBee.this.hasNectar(), false);
                    }
                }
            }
        }
    }

    protected class FindPollinationTargetGoal2 extends BeeEntity.FindPollinationTargetGoal {

        public FindPollinationTargetGoal2() {
            super();
        }

        @Override
        public boolean canBeeUse() {
            if (ResourcefulBee.this.numberOfMutations >= getMutationData().getMutationCount()) {
                return false;
            } else if (ResourcefulBee.this.random.nextFloat() < 0.3F) {
                return false;
            } else {
                return ResourcefulBee.this.hasNectar();
            }
        }

        @Override
        public void tick() {
            if (ResourcefulBee.this.tickCount % 5 == 0) {
                MutationData mutationData = getMutationData();
                if (mutationData.hasMutation() && (!mutationData.getBlockMutations().isEmpty() || !mutationData.getItemMutations().isEmpty() || !mutationData.getEntityMutations().isEmpty())) {
                    applyPollinationEffect();
                }
            }
        }
    }

    public class FindBeehiveGoal2 extends BeeEntity.FindBeehiveGoal {

        public FindBeehiveGoal2() {
            super();
        }

        @Override
        public boolean canBeeUse() {
            return getHivePos() != null && !hasRestriction() && wantsToEnterHive() && !((FindBeehiveGoalInvoker)this).callHasReachedTarget(getHivePos()) && isHiveValid();
        }

        @Override
        public void dropHive() {
            if (Boolean.FALSE.equals(CommonConfig.MANUAL_MODE.get())) {
                super.dropHive();
            }  // double check blacklist as it may need to be cleared - epic
        }
    }

    public class FakePollinateGoal extends BeeEntity.PollinateGoal {
        public FakePollinateGoal() {
            super();
        }

        @Override
        public boolean isPollinating() {
            return pollinateGoal.isPollinating();
        }

        @Override
        public void stopPollinating() {
            pollinateGoal.stopPollinating();
        }
    }

    public class UpdateBeehiveGoal2 extends BeeEntity.UpdateBeehiveGoal {
        public UpdateBeehiveGoal2() {
            super();
        }

        @Override
        public boolean canBeeUse() {
            if (Boolean.FALSE.equals(CommonConfig.MANUAL_MODE.get())) {
                return super.canBeeUse();
            }
            return false;
        }
    }
}
