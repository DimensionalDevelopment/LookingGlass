package com.xcompwiz.lookingglass.entity;

import com.xcompwiz.lookingglass.api.animator.CameraAnimatorPlayer;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Despite it's name, this isn't so much a doorway or window as it is a moving picture. More Harry Potter's portraits than Portal's portals. (Man I wish the
 * best example of portal rendering in games wasn't called Portal.... So hard to reference sanely.)
 */
public class EntityPortal extends Entity {
    // We store the dimension ID we point at in the dataWatcher at this index.
    private static final DataParameter<Integer> TARGET_DIM = EntityDataManager.createKey(EntityPortal.class, DataSerializers.VARINT);
    private static final DataParameter<BlockPos> TARGET_POS = EntityDataManager.createKey(EntityPortal.class, DataSerializers.BLOCK_POS);

    // How long the window has to live. Functions as a countdown timer.
    private long lifetime = 1000L;

    @SideOnly(Side.CLIENT)
    private IWorldView activeView;

    public EntityPortal(World world) {
        super(world);
    }

    public EntityPortal(World world, BlockPos pos, int targetDim, BlockPos targetPos) {
        this(world);
        setPosition(pos.getX(), pos.getY(), pos.getZ());
        setTarget(targetDim, targetPos);
    }

    /**
     * Puts the dim id target in the datawatcher.
     */
    private void setTarget(int dim, BlockPos pos) {
        dataManager.set(TARGET_DIM, dim);
        dataManager.set(TARGET_POS, pos);
        //TODO: Technically speaking, it might be wise to design this so that it can change targets, but that's not needed for this class.
        // If it was, we'd have this function kill any active views when the target changed, causing it to open a new view for the new target.
    }

    /**
     * Gets the target dimension id
     */
    private int getTarget() {
        return dataManager.get(TARGET_DIM);
    }

    private BlockPos getTargetPos() {
        return dataManager.get(TARGET_POS);
    }

    @Override
    protected void entityInit() {
        dataManager.register(TARGET_DIM, 0);
        dataManager.register(TARGET_POS, new BlockPos(0, 64, 0));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setDead() {
        super.setDead();
        releaseActiveView();
    }

    @Override
    public void onUpdate() {
        // Countdown to die
        --lifetime;
        if (lifetime <= 0) {
            setDead();
            return;
        }
        super.onUpdate();
    }

    @SideOnly(Side.CLIENT)
    public IWorldView getActiveView() {
        if (!world.isRemote) return null;
        if (activeView == null) {
            activeView = ProxyWorldManager.createWorldView(getTarget(), getTargetPos(), 160, 240);
            if (activeView != null) {
                // We set the player animator on our portrait. This makes the view move a little depending on how the user looks at it. Not quite a replacement for portal rendering, but cool looking anyway.
                activeView.setAnimator(new CameraAnimatorPlayer(activeView.getCamera(), this, Minecraft.getMinecraft().player));
            }
        }
        return activeView;
    }

    @SideOnly(Side.CLIENT)
    public void releaseActiveView() {
        if (activeView != null) ProxyWorldManager.destroyWorldView((WorldView) activeView);
        activeView = null;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        setTarget(
                nbt.getInteger("Dimension"),
                new BlockPos(
                        nbt.getInteger("TargetX"),
                        nbt.getInteger("TargetY"),
                        nbt.getInteger("TargetZ")));
        lifetime = nbt.getLong("lifetime");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Dimension", getTarget());
        nbt.setInteger("TargetX", getTargetPos().getX());
        nbt.setInteger("TargetY", getTargetPos().getY());
        nbt.setInteger("TargetZ", getTargetPos().getZ());
        nbt.setLong("lifetime", lifetime);
    }
}
