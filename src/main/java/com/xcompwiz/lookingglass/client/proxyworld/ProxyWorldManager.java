package com.xcompwiz.lookingglass.client.proxyworld;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.render.FrameBufferContainer;
import com.xcompwiz.lookingglass.entity.EntityCamera;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.packet.PacketCloseView;
import com.xcompwiz.lookingglass.network.packet.PacketCreateView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public final class ProxyWorldManager {
    private static Map<Integer, WorldClient> proxyWorlds = new HashMap<>();
    private static Collection<WorldClient> proxyWorldSet = Collections.unmodifiableCollection(proxyWorlds.values());
    /**
     * We actually populate this with weak sets. This allows for the world views to be freed without us needing to do anything.
     */
    private static Map<Integer, Collection<WorldView>> worldViewSets = new HashMap<>();

    /**
     * This is a complex bit. As we want to reuse the current client world when rendering, if possible, we need to handle when that world changes. We could
     * simply destroy all the views pointing to the existing proxy world, but that would be annoying to mods using the API. Instead, we replace our proxy world
     * with the new client world. This should only be called by LookingGlass, and only from the handling of the client world change detection.
     *
     * @param world The new client world
     */
    public static void handleWorldChange(WorldClient world) {
        if (ModConfigs.disabled) return;
        if (world == null) return;
        int dim = world.provider.getDimension();
        if (!proxyWorlds.containsKey(dim)) return; //BEST CASE! We don't have to do anything!
        proxyWorlds.put(dim, world);
        Collection<WorldView> worldViews = worldViewSets.get(dim);
        for (WorldView view : worldViews) {
            // Handle the change on the view object
            view.replaceWorldObject(world);
        }
    }

    public static synchronized void detectFreedWorldViews() {
        FrameBufferContainer.detectFreedWorldViews();
        //TODO: closeViewConnection(view);
        HashSet<Integer> emptyLists = new HashSet<>();
        for (Map.Entry<Integer, Collection<WorldView>> entry : worldViewSets.entrySet()) {
            if (entry.getValue().isEmpty()) emptyLists.add(entry.getKey());
        }
        for (int dim : emptyLists) {
            unloadProxyWorld(dim);
        }
    }

    public static synchronized WorldClient getProxyWorld(int dim) {
        if (ModConfigs.disabled) return null;
        WorldClient proxyworld = proxyWorlds.get(dim);
        if (proxyworld == null) {
            if (!DimensionManager.isDimensionRegistered(dim)) return null;
            // We really don't want to be doing this during a render cycle
            if (Minecraft.getMinecraft().player instanceof EntityCamera)
                return null; //TODO: This check probably needs to be altered
            WorldClient world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dim) proxyworld = world;
            if (proxyworld == null) proxyworld = new ProxyWorld(dim);
            proxyWorlds.put(dim, proxyworld);
            worldViewSets.put(dim, Collections.newSetFromMap(new WeakHashMap<>()));
        }
        return proxyworld;
    }

    private static void unloadProxyWorld(int dim) {
        Collection<WorldView> set = worldViewSets.remove(dim);
        if (set != null && set.size() > 0) LookingGlass.log.warn("Unloading ProxyWorld with live views");
        WorldClient proxyworld = proxyWorlds.remove(dim);
        WorldClient world = Minecraft.getMinecraft().world;
        if (world != null && world == proxyworld) return;
        if (proxyworld != null)
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(proxyworld));
    }

    public static void clearProxyworlds() {
        while (!proxyWorlds.isEmpty()) {
            unloadProxyWorld(proxyWorlds.keySet().iterator().next());
        }
    }

    public static Collection<WorldClient> getProxyWorlds() {
        return proxyWorldSet;
    }

    public static Collection<WorldView> getWorldViews(int dim) {
        Collection<WorldView> set = worldViewSets.get(dim);
        if (set == null) return Collections.emptySet();
        return Collections.unmodifiableCollection(set);
    }

    public static WorldView createWorldView(int dim, BlockPos spawn, int width, int height) {
        if (ModConfigs.disabled) return null;
        if (!DimensionManager.isDimensionRegistered(dim)) return null;

        WorldClient proxyWorld = ProxyWorldManager.getProxyWorld(dim);
        if (proxyWorld == null) return null;

        Collection<WorldView> worldViews = worldViewSets.get(dim);
        if (worldViews == null) return null;

        WorldView view = new WorldView(proxyWorld, spawn, width, height);

        // Initialize the view rendering system
        Minecraft mc = Minecraft.getMinecraft();
        Entity backup = mc.getRenderViewEntity();
        mc.setRenderViewEntity(view.camera);
        try {
            view.getRenderGlobal().setWorldAndLoadRenderers(proxyWorld);
        } catch (OutOfMemoryError error) {
            LookingGlass.log.warn("Failed to create world view, not enough memory!");
            view.getRenderGlobal().setWorldAndLoadRenderers(null);
            throw new RuntimeException(error); // TODO
        }
        mc.setRenderViewEntity(backup);

        // Inform the server of the new view
        LookingGlassPacketManager.bus.sendToServer(PacketCreateView.createPacket(view));
        worldViews.add(view);
        return view;
    }

    private static void closeViewConnection(WorldView view) {
        LookingGlassPacketManager.bus.sendToServer(PacketCloseView.createPacket(view));
    }

    /**
     * Handles explicit shutdown of a world view. Tells the view to clean itself up and removes it from the tracked world views here (encouraging the world to unload).
     *
     * @param view The view to kill
     */
    public static void destroyWorldView(WorldView view) {
        Collection<WorldView> set = worldViewSets.get(view.getWorld().provider.getDimension());
        view.getRenderGlobal().setWorldAndLoadRenderers(null);
        if (set != null) set.remove(view);
        closeViewConnection(view);
        view.cleanup();
    }
}
