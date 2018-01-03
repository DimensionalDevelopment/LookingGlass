package com.xcompwiz.lookingglass.render;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.client.render.RenderUtils;
import com.xcompwiz.lookingglass.log.LoggerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.io.PrintStream;
import java.util.Collection;

public class WorldViewRenderManager {
    public static void onRenderTick(PrintStream printstream) {
        Minecraft mc = Minecraft.getMinecraft();
        Collection<WorldClient> worlds = ProxyWorldManager.getProxyworlds();
        if (worlds == null || worlds.isEmpty()) return;

        long renderT = Minecraft.getSystemTime();
        //TODO: This and the renderWorldToTexture need to be remixed
        WorldClient worldBackup = mc.world;
        RenderGlobal renderBackup = mc.renderGlobal;
        ParticleManager effectBackup = mc.effectRenderer;
        EntityPlayerSP playerBackup = mc.player;
        Entity viewportBackup = mc.getRenderViewEntity();

        //TODO: This is a hack to work around some of the vanilla rendering hacks... Yay hacks.
        //float fovmult = playerBackup.getFOVMultiplier();
        ItemStack currentClientItem = playerBackup.inventory.getCurrentItem();

        for (WorldClient proxyworld : worlds) {
            if (proxyworld == null) continue;
            mc.world = proxyworld;
            mc.getRenderManager().setWorld(proxyworld); // TODO!!!
            for (WorldView activeview : ProxyWorldManager.getWorldViews(proxyworld.provider.getDimension())) {
                if (activeview.hasChunks() && activeview.markClean()) {
                    activeview.startRender(renderT);

                    mc.renderGlobal = activeview.getRenderGlobal();
                    mc.effectRenderer = activeview.getEffectRenderer();
                    mc.setRenderViewEntity(activeview.camera);
                    mc.player = activeview.camera;
                    //Other half of hack
                    //activeview.camera.setFOVMult(fovmult); //Prevents the FOV from flickering
                    activeview.camera.inventory.currentItem = playerBackup.inventory.currentItem;
                    activeview.camera.inventory.mainInventory.set(playerBackup.inventory.currentItem, currentClientItem); //Prevents the hand from flickering

                    try {
                        mc.renderGlobal.updateClouds();
                        mc.world.doVoidFogParticles(MathHelper.floor(activeview.camera.posX), MathHelper.floor(activeview.camera.posY), MathHelper.floor(activeview.camera.posZ));
                        mc.effectRenderer.updateEffects();
                    } catch (Exception e) {
                        LoggerUtils.error("Client Proxy Dim had error while updating render elements: %s", e.getLocalizedMessage());
                        e.printStackTrace(printstream);
                    }

                    try {
                        RenderUtils.renderWorldToTexture(0.1f, activeview.getFramebuffer(), activeview.width, activeview.height);
                    } catch (Exception e) {
                        LoggerUtils.error("Client Proxy Dim had error while rendering: %s", e.getLocalizedMessage());
                        e.printStackTrace(printstream);
                    }
                }
            }
        }

        mc.setRenderViewEntity(viewportBackup);
        mc.player = playerBackup;
        mc.effectRenderer = effectBackup;
        mc.renderGlobal = renderBackup;
        mc.world = worldBackup;
        mc.getRenderManager().setWorld(worldBackup);
        // RenderManager.instance.set(mc.world); // TODO!!!
    }
}
