package com.xcompwiz.lookingglass.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.entity.EntityPortal;

public class RenderPortal extends Render<EntityPortal> {

	public RenderPortal(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityPortal portal, double d, double d1, double d2, float f, float f1) {
		IWorldView activeview = portal.getActiveView();

		if (activeview == null) return;

		int texture = activeview.getTexture();
		if (texture == 0) return;

		int width = 2;
		int height = 3;
		double left = -width / 2.;
		double top = 0;

		activeview.markDirty();
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glPushMatrix();
		GL11.glTranslatef((float) d, (float) d1, (float) d2);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();

		GlStateManager.color(1,1,1,1);

		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(left, top, 0.0D).tex(0.0D, 0.0D).endVertex(); //inc=bl out; inc=bl down
		buffer.pos(width + left, top, 0.0D).tex(1.0D, 0.0D).endVertex(); //dc=br out; inc=br down
		buffer.pos(width + left, height + top, 0.0D).tex(1.0D, 1.0D).endVertex(); //dec=tr out; dec=tr up
		buffer.pos(left, height + top, 0.0D).tex(0.0D, 1.0D).endVertex(); //inc=lt out; dec=tl up
		tessellator.draw();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		//XXX: Make the back of the portals a little nicer
		GlStateManager.color(0, 0, 1, 1);
		buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(left, height + top, 0.0D).tex(0.0D, 1.0D).endVertex();
		buffer.pos(width + left, height + top, 0.0D).tex(1.0D, 1.0D).endVertex();
		buffer.pos(width + left, top, 0.0D).tex(1.0D, 0.0D).endVertex();
		buffer.pos(left, top, 0.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPortal entity) {
		return null;
	}
}
