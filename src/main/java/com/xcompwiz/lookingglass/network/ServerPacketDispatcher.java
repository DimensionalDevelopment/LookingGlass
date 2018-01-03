package com.xcompwiz.lookingglass.network;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraft.entity.player.EntityPlayer;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is a variant on the vanilla server packet dispatcher. We use it so that we can send data to
 * cleints in a limited (throttled) manner. This allows server admins to limit how much bandwidth LookingGlass
 * consumes on a server.
 */
public class ServerPacketDispatcher extends Thread {

    private static ServerPacketDispatcher instance;

    private List<PacketHolder> packets;
    private boolean isRunning = true;

    private ServerPacketDispatcher() {
        packets = new LinkedList<>();
    }

    public static ServerPacketDispatcher getInstance() {
        if (instance == null) instance = new ServerPacketDispatcher();
        return instance;
    }

    public static void shutdown() {
        if (instance != null) instance.halt();
        instance = null;
    }

    public void addPacket(EntityPlayer player, FMLProxyPacket packet) {
        synchronized (this) {
            packets.add(new PacketHolder(player, packet));
            notify();
        }
    }

    public void tick() {
        int byteLimit = ModConfigs.dataRate;
        for (int bytes = 0; bytes < byteLimit && !packets.isEmpty(); ) {
            PacketHolder p = packets.get(0);
            bytes += p.sendPacket();
            packets.remove(0);
        }
    }

    public void halt() {
        synchronized (this) {
            isRunning = false;
            packets.clear();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (packets.size() > 0) {
                try {
                    synchronized (this) {
                        tick();
                        wait(20);
                    }
                } catch (InterruptedException e) {
                    LookingGlass.log.warn(e);
                }
            } else {
                try {
                    synchronized (this) {
                        wait(1000);
                    }
                } catch (InterruptedException e) {
                    LookingGlass.log.warn(e);
                }
            }
        }
    }
}
