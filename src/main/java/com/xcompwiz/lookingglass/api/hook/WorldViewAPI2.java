package com.xcompwiz.lookingglass.api.hook;

import com.xcompwiz.lookingglass.api.view.IWorldView;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.BlockPos;

/**
 * Available via "view-2" from the API provider
 *
 * @author xcompwiz
 */
public interface WorldViewAPI2 {

    /**
     * Creates a world viewer object which will handle the rendering and retrieval of the remote location. Can return null.
     *
     * @param dim  The target dimension
     * @param pos The coordinates of the target location. If null, world spawn is used.
     * @param width  Texture resolution width
     * @param height Texture resolution height
     * @return A IWorldView object for your use or null if something goes wrong.
     */
    @SideOnly(Side.CLIENT)
    IWorldView createWorldView(Integer dim, BlockPos pos, int width, int height);

    /**
     * This function is available should you wish to explicitly have the world view clean up its framebuffer. You should not use a view after calling this on
     * the view.
     *
     * @param worldView The view to clean up (effectively "destroy")
     */
    void cleanupWorldView(IWorldView worldView);
}
