package com.xcompwiz.lookingglass.client.proxyworld;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;
import com.xcompwiz.lookingglass.api.view.IViewCamera;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.render.FrameBufferContainer;
import com.xcompwiz.lookingglass.entity.EntityCamera;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@SideOnly(Side.CLIENT)
public class WorldView implements IWorldView {
    private WorldClient world;
    public final BlockPos pos;
    public final EntityCamera camera;
    public final IViewCamera cameraWrapper;

    public final int width;
    public final int height;

    private boolean update;
    private boolean ready;
    private boolean hasChunks;
    private long lastRenderTime = -1;

    private RenderGlobal renderGlobal;
    private ParticleManager effectRenderer;

    private FrameBufferContainer fbo;

    public WorldView(WorldClient world, BlockPos pos, int width, int height) {
        this.width = width;
        this.height = height;
        this.world = world;
        this.pos = pos;
        camera = new EntityCamera(world, pos);
        cameraWrapper = new ViewCameraImpl(camera);
        renderGlobal = new RenderGlobal(Minecraft.getMinecraft());
        effectRenderer = new ParticleManager(world, Minecraft.getMinecraft().getTextureManager());
        // Technically speaking, this is poor practice as it leaks a reference to the view before it's done constructing.
        fbo = FrameBufferContainer.createNewFramebuffer(this, width, height);
    }

    /**
     * Explicitly shuts down the view. Informs the frame buffer manager that we don't want our framebuffer anymore (so it can be cleaned up for certain on the
     * next cleanup pass) and kills our fbo reference. The view is no longer usable after this is called.
     */
    public void cleanup() {
        fbo = null;
        FrameBufferContainer.removeWorldView(this);
    }

    @Override
    public boolean isReady() {
        return fbo != null && ready;
    }

    public boolean hasChunks() {
        return fbo != null && hasChunks;
    }

    @Override
    public void markDirty() {
        update = true;
    }

    public boolean markClean() {
        if (fbo == null) return false;
        ready = true;
        boolean temp = update;
        update = false;
        return temp;
    }

    public int getFramebuffer() {
        return fbo == null ? 0 : fbo.getFramebuffer();
    }

    public RenderGlobal getRenderGlobal() {
        return renderGlobal;
    }

    public ParticleManager getEffectRenderer() {
        return effectRenderer;
    }

    @Override
    public int getTexture() {
        return fbo == null ? 0 : fbo.getTexture();
    }

    @Override
    public void grab() {}

    @Override
    public boolean release() {
        return false;
    }

    public void onChunkReceived(int x, int z) {
        hasChunks = true;
        int cameraX = MathHelper.floor(camera.posX) >> 4;
        int cameraZ = MathHelper.floor(camera.posZ) >> 4;
        if (cameraX >= x - 1 && cameraX <= x + 1 && cameraZ > z - 1 && cameraZ < z + 1) camera.refreshAnimator();
    }

    public void updateWorldSpawn(BlockPos pos) {
        camera.updateWorldSpawn(pos);
    }

    public void startRender(long renderTime) {
        if (lastRenderTime > 0) camera.tick(renderTime - lastRenderTime);
        lastRenderTime = renderTime;
    }

    @Override
    public void setAnimator(ICameraAnimator animator) {
        camera.setAnimator(animator);
    }

    @Override
    public IViewCamera getCamera() {
        return cameraWrapper;
    }

    /**
     * This is a really complex bit. As we want to reuse the current client world when rendering, if possible, we need to handle when that world changes. We
     * could simply destroy all the views pointing to the existing proxy world, but that would be annoying to mods using the API. Instead, we replace our proxy
     * world with the new client world. This should only be called by LookingGlass, and only from the handling of the client world change detection.
     *
     * @param world The new world
     */
    public void replaceWorldObject(WorldClient world) {
        this.world = world;
        camera.world = world;
        effectRenderer.clearEffects(world);
        renderGlobal.setWorldAndLoadRenderers(world);
    }

    public WorldClient getWorld() {
        return world;
    }
}
