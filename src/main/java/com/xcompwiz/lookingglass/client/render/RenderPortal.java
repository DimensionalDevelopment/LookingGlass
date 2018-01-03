package com.xcompwiz.lookingglass.client.render;

import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderPortal extends Render<EntityPortal> {

    public RenderPortal(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPortal entity, double x, double y, double z, float entityYaw, float partialTicks) {
        IWorldView activeview = entity.getActiveView();
        if (activeview == null) return;

        int texture = activeview.getTexture();
        if (texture == 0) return;

        int width = 2;
        int height = 3;
        double left = -width / 2.;
        double top = 0;


        activeview.markDirty();
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        //GlStateManager.rotate(entityYaw, 0.0F, 1.0F, 0.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();

        // Render the front of the portal
        GlStateManager.bindTexture(texture);
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(left, top, 0.0D).tex(0.0D, 0.0D).endVertex(); //inc=bl out; inc=bl down
        worldRenderer.pos(width + left, top, 0.0D).tex(1.0D, 0.0D).endVertex(); //dc=br out; inc=br down
        worldRenderer.pos(width + left, height + top, 0.0D).tex(1.0D, 1.0D).endVertex(); //dec=tr out; dec=tr up
        worldRenderer.pos(left, height + top, 0.0D).tex(0.0D, 1.0D).endVertex(); //inc=lt out; dec=tl up
        tessellator.draw();

        // Render the back of the portal TODO
        //TODO: Make the back of the portals a little nicer
        /*GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.color(0, 0, 1, 1);
        worldRenderer.pos(left, height + top, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldRenderer.pos(width + left, height + top, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldRenderer.pos(width + left, top, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldRenderer.pos(left, top, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();*/

        GlStateManager.popMatrix();

        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable @Override protected ResourceLocation getEntityTexture(EntityPortal entity) {
        return null;
    }
}
