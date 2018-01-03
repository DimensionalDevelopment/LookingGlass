package com.xcompwiz.lookingglass.client;

import com.xcompwiz.lookingglass.client.render.RenderPortal;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void onPreInitialization() {
        // We register the portal renderer here
        RenderingRegistry.registerEntityRenderingHandler(EntityPortal.class, RenderPortal::new);
    }
}
