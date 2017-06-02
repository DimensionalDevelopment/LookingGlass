package com.xcompwiz.lookingglass.entity;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;
import net.minecraftforge.client.event.EntityViewRenderEvent;

/**
 * Our camera entity. This is made a player so that we can replace the player client-side when doing rendering.
 * At the bottom of the class we create a bunch of method stubs to override higher level logic, so that our "player" doesn't act like one.
 */
@SuppressWarnings({"EntityConstructor", "ConstantConditions"})
public class EntityCamera extends EntityPlayerSP {

		private ICameraAnimator		animator;
		private BlockPos	target;
		private boolean				defaultSpawn	= false;

		private float				fovmultiplier	= 1;

		public EntityCamera(World world, BlockPos spawn) {
			super(Minecraft.getMinecraft(), world, Minecraft.getMinecraft().getConnection(), new StatisticsManager());
			this.target = spawn;

			if (target == null) {
				defaultSpawn = true;
				BlockPos cc = world.provider.getSpawnPoint();
				target = updateTargetPosition(cc);
			}
			this.setPositionAndUpdate(target.getX(), target.getY(), target.getZ());
		}

		public void setAnimator(ICameraAnimator animator) {
			this.animator = animator;
			if (this.animator != null) this.animator.setTarget(target);
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);
			getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
		}

		public void updateWorldSpawn(BlockPos cc) {
			if (defaultSpawn) {
				target = updateTargetPosition(cc);
				this.setPositionAndUpdate(target.getX(), target.getY(), target.getZ());
				if (animator != null) animator.setTarget(cc);
				this.refreshAnimator();
			}
		}

		private BlockPos updateTargetPosition(BlockPos target) {
			int x = target.getX();
			int y = target.getY();
			int z = target.getZ();
			if (!this.world.getChunkFromBlockCoords(new BlockPos(x,y,z)).isEmpty()) {
				if (this.world.getBlockState(new BlockPos(x, y, z)).getBlock().isPassable(this.world, new BlockPos(x, y, z))) {
					while (y > 0 && this.world.getBlockState(new BlockPos(x, --y, z)).getBlock().isPassable(this.world, new BlockPos(x, y, z)))
						;
					if (y == 0) y = target.getY();
					else ++y;
				} else {
					while (y < 256 && !this.world.getBlockState(new BlockPos(x, ++y, z)).getBlock().isPassable(this.world, new BlockPos(x, y, z)))
						;
					if (y == 256) y = target.getY();
				}
				return new BlockPos(x,y,z);
			}
			return target;
		}

		public void refreshAnimator() {
			if (this.animator != null) animator.refresh();
		}

		public void tick(long dt) {
			if (this.animator != null) animator.update(dt);
		}

		@Override
		public float getFovModifier() {
			return fovmultiplier;
		}

		public void setFOVMult(float fovmult) {
			fovmultiplier = fovmult;
		}

		/*
         * POSSIBLY UNNECESSARY CODE TO PREVENT OTHER CODE FROM RUNNING
         */
		@Override
		public void onEntityUpdate() {}

		@Override
		public void onLivingUpdate() {}

		@Override
		public void onUpdate() {}

		@Override
		protected int getExperiencePoints(EntityPlayer par1EntityPlayer) {
			return 0;
		}

		@Override
		public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {}

		@Override
		public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {}

		@Override
		public void setAIMoveSpeed(float par1) {}

		@Override
		protected void dropEquipment(boolean par1, int par2) {}

		@Override
		protected void onDeathUpdate() {
			this.setDead();
		}

		@Override
		public EntityLivingBase getAITarget() {
			return null;
		}

		@Override
		public void setRevengeTarget(EntityLivingBase par1) {}

		@Override
		public EntityLivingBase getLastAttacker() {
			return null;
		}

		@Override
		public void setLastAttacker(Entity par1) {}

		@Override
		protected void updatePotionEffects() {}

		@Override
		public void clearActivePotions() {}

		@Override
		public boolean isPotionActive(Potion par1) {
			return false;
		}

		@Override
		public PotionEffect getActivePotionEffect(Potion par1) {
			return null;
		}

		@Override
		public void addPotionEffect(PotionEffect par1) {}

		@Override
		public boolean isPotionApplicable(PotionEffect par1) {
			return false;
		}

		@Override
		public boolean isEntityUndead() {
			return false;
		}

		@Override
		protected void onNewPotionEffect(PotionEffect par1) {}

		@Override
		protected void onChangedPotionEffect(PotionEffect par1, boolean par2) {}

		@Override
		protected void onFinishedPotionEffect(PotionEffect par1) {}

		@Override
		public void heal(float par1) {}

		@Override
		public boolean attackEntityFrom(DamageSource par1, float par2) {
			return false;
		}

		@Override
		public void renderBrokenItemStack(ItemStack par1) {}

		@Override
		public void onDeath(DamageSource source) {
			this.world.setEntityState(this, (byte) 3);
		}

		@Override
		public void knockBack(Entity par1Entity, float par2, double par3, double par5) {}

		@Override
		public boolean isOnLadder() {
			return false;
		}

		@Override
		public int getTotalArmorValue() {
			return 0;
		}

		@Override
		protected float applyArmorCalculations(DamageSource par1DamageSource, float par2) {
			return par2;
		}

		@Override
		protected float applyPotionDamageCalculations(DamageSource par1DamageSource, float par2) {
			return par2;
		}

		@Override
		protected void damageEntity(DamageSource source, float par2) {}

		@Override
		protected void updateArmSwingProgress() {}

		@Override
		public void setSprinting(boolean par1) {}

		@Override
		protected float getSoundVolume() {
			return 0F;
		}

		@Override
		public void dismountEntity(Entity par1Entity) {}

		@Override
		public void moveEntityWithHeading(float par1, float par2) {}

		@Override
		public void updateRidden() {}

		@Override
		public void setJumping(boolean par1) {}

		@Override
		public void onItemPickup(Entity par1Entity, int par2) {}

		@Override
		public boolean canEntityBeSeen(Entity par1Entity) {
			return false;
		}

		@Override
		public boolean canBeCollidedWith() {
			return false;
		}

		@Override
		public boolean canBePushed() {
			return false;
		}

		@Override
		protected boolean canTriggerWalking() {
			return false;
		}

		@Override
		public boolean handleWaterMovement() {
			return false;
		}

		@Override
		public boolean isInsideOfMaterial(Material par1Material) {
			return false;
		}

		@Override
		public float getBrightness(float par1) {
			return 0;
		}

		@Override
		public void applyEntityCollision(Entity par1Entity) {}

		@Override
		public boolean isBurning() {
			return false;
		}

		@Override
		public boolean isRiding() {
			return false;
		}

		@Override
		public boolean isSneaking() {
			return false;
		}

		@Override
		public boolean isInvisible() {
			return true;
		}

		@Override
		public void onStruckByLightning(EntityLightningBolt par1) {}

		@Override
		public boolean shouldRenderInPass(int pass) {
			return false;
		}

		@Override
		protected void collideWithEntity(Entity par1Entity) {}

		@Override
		protected void collideWithNearbyEntities() {}

		@Override
		public boolean doesEntityNotTriggerPressurePlate() {
			return true;
		}
	}