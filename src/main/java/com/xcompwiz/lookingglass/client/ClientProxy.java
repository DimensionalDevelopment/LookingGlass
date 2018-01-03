package com.xcompwiz.lookingglass.client;

import com.xcompwiz.lookingglass.client.render.RenderPortal;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * Our faithful proxy class.  Allows for running code differently dependent on whether we are client- or server-side.
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    /**
     * Run during mod preinit.
     */
    @Override
    public void preinit() {
        // We register the portal renderer here
        RenderingRegistry.registerEntityRenderingHandler(EntityPortal.class, new IRenderFactory<EntityPortal>() {
            @Override public Render<? super EntityPortal> createRenderFor(RenderManager manager) {
                return new RenderPortal(manager);
            }
        });
    }
}
