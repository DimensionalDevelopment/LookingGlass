package com.xcompwiz.lookingglass.client.proxyworld;

import com.xcompwiz.lookingglass.api.view.IViewCamera;
import com.xcompwiz.lookingglass.entity.EntityCamera;
import net.minecraft.world.IBlockAccess;

public class ViewCameraImpl implements IViewCamera {
    private EntityCamera camera;

    public ViewCameraImpl(EntityCamera camera) {
        this.camera = camera;
    }

    @Override
    public void addRotations(float yaw, int pitch) {
        camera.setAngles(yaw, pitch);
    }

    @Override
    public void setYaw(float f) {
        camera.prevRotationYaw = f;
        camera.rotationYaw = f;
    }

    @Override
    public float getYaw() {
        return camera.rotationYaw;
    }

    @Override
    public void setPitch(float f) {
        camera.prevRotationPitch = f;
        camera.rotationPitch = f;
    }

    @Override
    public float getPitch() {
        return camera.rotationPitch;
    }

    @Override
    public void setLocation(double x, double y, double z) {
        camera.setLocationAndAngles(x, y, z, camera.rotationYaw, camera.rotationPitch);
    }

    @Override
    public double getX() {
        return camera.posX;
    }

    @Override
    public double getY() {
        return camera.posY;
    }

    @Override
    public double getZ() {
        return camera.posZ;
    }

    @Override
    public IBlockAccess getBlockData() {
        return camera.worldObj;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return !camera.worldObj.getChunkFromBlockCoords(x, z).isEmpty();
    }

    @Override
    public boolean chunkLevelsExist(int x, int z, int yl1, int yl2) {
        return !camera.worldObj.getChunkFromBlockCoords(x, z).getAreLevelsEmpty(yl1, yl2);
    }
}
