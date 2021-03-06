package com.xcompwiz.lookingglass.api.animator;

import com.xcompwiz.lookingglass.api.view.IViewCamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * This is a badly approximated "portal render" animator, which makes the camera move based on what direction the player is from some defined point. It doesn't
 * do quite what it was meant to, but it's neat enough looking, so I'll leave it be. It does make a view look more "alive" or 3D, so if you just want a slightly
 * animated view that doesn't look like you could walk through it, then this works great. It produces an effect more akin to Harry Potter's portraits than it
 * does to Portal's portals.
 *
 * @author xcompwiz
 */
public class CameraAnimatorPlayer implements ICameraAnimator {
    // This is the camera object we are animating
    private final IViewCamera camera;

    // The entity was are using as our reference point, such as our portrait view.
    private Entity reference;
    // The entity we are facing. Expected to be the client side player, but could be anything, really.
    private Entity player;
    // The point we are looking at in block coordinates.
    private BlockPos target;

    private boolean updateY;
    private float accum;

    /**
     * @param camera
     * @param reference The entity we are using as our reference point, such as our portrait view.
     * @param player    The entity we are facing. Expected to be the client side player, but could be anything, really.
     */
    public CameraAnimatorPlayer(IViewCamera camera, Entity reference, Entity player) {
        this.camera = camera;
        this.reference = reference;
        this.player = player;
    }

    /**
     * Sets the point we are looking at using block coordinates.
     */
    @Override
    public void setTarget(BlockPos target) {
        this.target = new BlockPos(target);
    }

    @Override
    public void refresh() {
        updateY = true;
    }

    @Override
    public void update(long dt) {
        // This animator is incomplete and broken. It's a rough approximation I made at 4AM one night.
        // However, it's also pretty cool looking, so I'm not going to bother fixing it. :P
        // Note: Needs base yaw and pitch of view
        if (reference.world.provider.getDimension() != player.world.provider.getDimension()) return;

        // A standard accumulator trick to force periodic rechecks of the y position. Probably superfluous.
        // Ticks every 10 seconds
        if ((accum += dt) >= 10000) {
            updateY = true;
            accum -= 10000;
        }
        if (updateY) updateTargetPosition();
        double dx = player.posX - reference.posX;
        double dy = player.posY - (reference.posY + player.getYOffset());
        double dz = player.posZ - reference.posZ;
        double length = Math.sqrt(dx * dx + dz * dz + dy * dy); //TODO: Needs Go Faster
        float yaw = -(float) Math.atan2(dx, dz);
        yaw *= 180 / Math.PI;
        float pitch = (float) Math.asin(dy / length);
        pitch *= 180 / Math.PI;
        camera.setLocation(target.getX(), target.getY(), target.getZ());
        camera.setYaw(yaw);
        camera.setPitch(pitch);
    }

    private void updateTargetPosition() {
        updateY = false;
        int x = target.getX();
        int y = target.getY();
        int z = target.getZ();
        if (!camera.chunkExists(x, z)) {
            if (camera.getBlockData().getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()) {
                //noinspection StatementWithEmptyBody
                while (y > 0 && camera.getBlockData().getBlockState(new BlockPos(x, --y, z)).getMaterial().blocksMovement());
                if (y == 0) y = target.getY();
                else y += 2;
            } else {
                //noinspection StatementWithEmptyBody
                while (y < 256 && !camera.getBlockData().getBlockState(new BlockPos(x, ++y, z)).getMaterial().blocksMovement());
                if (y == 256) y = target.getY();
                else ++y;
            }
            target = new BlockPos(target.getX(), y, target.getZ());
        }
    }
}
