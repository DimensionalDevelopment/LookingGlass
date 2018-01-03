package com.xcompwiz.lookingglass.apiimpl;

import com.xcompwiz.lookingglass.api.hook.WorldViewAPI2;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.BlockPos;

/**
 * This is the API wrapper (instance) class for the WorldView API at version 2.
 *
 * @author xcompwiz
 */
public class LookingGlassAPI2Impl implements WorldViewAPI2 {

    public LookingGlassAPI2Impl() {}

    @Override
    @SideOnly(Side.CLIENT)
    public IWorldView createWorldView(Integer dim, BlockPos pos, int width, int height) { // TODO: unbreak API
        return ProxyWorldManager.createWorldView(dim, pos, width, height);
    }

    @Override
    public void cleanupWorldView(IWorldView worldView) {
        if (worldView == null) return;
        if (!(worldView instanceof WorldView))
            throw new RuntimeException("[%s] is misusing the LookingGlass API. Cannot cleanup custom IWorldView objects.");
        ProxyWorldManager.destroyWorldView((WorldView) worldView);
    }
}
