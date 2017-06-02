package com.xcompwiz.lookingglass.client.proxyworld;

import net.minecraft.world.IBlockAccess;

import com.xcompwiz.lookingglass.api.view.IViewCamera;
import com.xcompwiz.lookingglass.entity.EntityCamera;

public class ViewCameraImpl implements IViewCamera {
	private EntityCamera	camera;

	public ViewCameraImpl(EntityCamera camera) {
		this.camera = camera;
	}

	@Override
	public void addRotations(float yaw, int pitch) {
		this.camera.setLocationAndAngles(this.getX(), this.getY(), this.getZ(), yaw, pitch);
	}

	@Override
	public void setYaw(float f) {
		this.camera.prevRotationYaw = f;
		this.camera.rotationYaw = f;
	}

	@Override
	public float getYaw() {
		return this.camera.rotationYaw;
	}

	@Override
	public void setPitch(float f) {
		this.camera.prevRotationPitch = f;
		this.camera.rotationPitch = f;
	}

	@Override
	public float getPitch() {
		return this.camera.rotationPitch;
	}

	@Override
	public void setLocation(double x, double y, double z) {
		this.camera.setLocationAndAngles(x, y, z, this.camera.rotationYaw, this.camera.rotationPitch);
	}

	@Override
	public double getX() {
		return this.camera.posX;
	}

	@Override
	public double getY() {
		return this.camera.posY;
	}

	@Override
	public double getZ() {
		return this.camera.posZ;
	}

	@Override
	public IBlockAccess getBlockData() {
		return this.camera.world;
	}

	@Override
	public boolean chunkExists(int x, int z) {
		return !camera.world.getChunkFromChunkCoords(x, z).isEmpty();
	}

	@Override
	public boolean chunkLevelsExist(int x, int z, int yl1, int yl2) {
		return !camera.world.getChunkFromChunkCoords(x, z).getAreLevelsEmpty(yl1, yl2);
	}
}
