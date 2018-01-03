package com.xcompwiz.lookingglass.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

public class RenderUtils {

    @SideOnly(Side.CLIENT)
    public static void renderWorldToTexture(float renderTime, int framebuffer, int width, int height) {
        if (framebuffer == 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.skipRenderWorld) return;
        EntityRenderer entityRenderer = mc.entityRenderer;

        //Backup current render settings
        int heightBackup = mc.displayHeight;
        int widthBackup = mc.displayWidth;

        int thirdPersonBackup = mc.gameSettings.thirdPersonView;
        boolean hideGuiBackup = mc.gameSettings.hideGUI;
        int particleBackup = mc.gameSettings.particleSetting;
        boolean anaglyphBackup = mc.gameSettings.anaglyph;
        int renderDistanceBackup = mc.gameSettings.renderDistanceChunks;
        float FOVbackup = mc.gameSettings.fovSetting;

        //Render world
        try {
            //Set all of the render setting to work on the proxy world
            mc.displayHeight = height;
            mc.displayWidth = width;

            //TODO: params (FOV, Particle setting, renderDistance)
            mc.gameSettings.thirdPersonView = 0;
            mc.gameSettings.hideGUI = true;
            //mc.gameSettings.particleSetting = ;
            mc.gameSettings.anaglyph = false;
            //mc.gameSettings.renderDistanceChunks = ;
            //mc.gameSettings.fovSetting = ;

            //Set gl options
            GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
            GlStateManager.bindTexture(0);
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebuffer);
            GlStateManager.clearColor(1.0f, 0, 0, 0.5f);
            GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

            int i1 = mc.gameSettings.limitFramerate;
            if (mc.isFramerateLimitBelowMax()) {
                entityRenderer.renderWorld(renderTime, 1000000000 / i1);
            } else {
                entityRenderer.renderWorld(renderTime, 0L);
            }
        } catch (Exception e) {
            try {
                //Clean up the tessellator, just in case.
                Tessellator.getInstance().draw();
            } catch (Exception e2) {
                //It might throw an exception, but that just means we didn't need to clean it up (this time)
            }
            throw new RuntimeException("Error while rendering proxy world", e);
        } finally {
            GlStateManager.enableTexture2D();
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

            GlStateManager.viewport(0, 0, widthBackup, heightBackup);
            GlStateManager.loadIdentity();

            mc.gameSettings.thirdPersonView = thirdPersonBackup;
            mc.gameSettings.hideGUI = hideGuiBackup;
            mc.gameSettings.particleSetting = particleBackup;
            mc.gameSettings.anaglyph = anaglyphBackup;
            mc.gameSettings.renderDistanceChunks = renderDistanceBackup;
            mc.gameSettings.fovSetting = FOVbackup;

            mc.displayHeight = heightBackup;
            mc.displayWidth = widthBackup;
        }
    }
}
