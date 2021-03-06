package com.xcompwiz.lookingglass.network;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.network.packet.PacketHandlerBase;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;

import java.util.HashMap;

public class LookingGlassPacketManager {

    public static final String CHANNEL = "lookingglass";
    public static FMLEventChannel bus;

    private static HashMap<Byte, PacketHandlerBase> packethandlers = new HashMap<>();
    private static HashMap<Class<? extends PacketHandlerBase>, Byte> idmap = new HashMap<>();

    /**
     * Register a new packet handler to the manager. We use pre-defined packet ids to avoid mismatched packet ids across client-server communications.
     *
     * @param handler The packet handler to register
     * @param id      The id to which the handler should be bound
     */
    public static void registerPacketHandler(PacketHandlerBase handler, byte id) {
        if (packethandlers.get(id) != null) {
            throw new RuntimeException("Multiple id registrations for packet type on " + CHANNEL + " channel");
        }
        packethandlers.put(id, handler);
        idmap.put(handler.getClass(), id);
    }

    public static byte getId(PacketHandlerBase handler) {
        return getId(handler.getClass());
    }

    public static byte getId(Class<? extends PacketHandlerBase> handlerclass) {
        if (!idmap.containsKey(handlerclass))
            throw new RuntimeException("Attempted to get id for unregistered network message handler.");
        return idmap.get(handlerclass);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPacketData(FMLNetworkEvent.ClientCustomPacketEvent event) {
        FMLProxyPacket pkt = event.getPacket();

        onPacketData(event.getManager(), pkt, Minecraft.getMinecraft().player);
    }

    @SubscribeEvent
    public void onPacketData(FMLNetworkEvent.ServerCustomPacketEvent event) {
        FMLProxyPacket pkt = event.getPacket();

        onPacketData(event.getManager(), pkt, ((NetHandlerPlayServer) event.getHandler()).player);
    }

    public void onPacketData(NetworkManager manager, FMLProxyPacket packet, EntityPlayer player) {
        try {
            if (packet == null || packet.payload() == null) {
                throw new RuntimeException("Empty packet sent to " + CHANNEL + " channel");
            }
            PacketBuffer data = new PacketBuffer(packet.payload());
            byte type = data.readByte();

            try {
                PacketHandlerBase handler = packethandlers.get(type);
                if (handler == null) {
                    throw new RuntimeException("Unrecognized packet sent to " + CHANNEL + " channel");
                }
                handler.handle(data, player);
            } catch (Exception e) {
                LookingGlass.log.warn("PacketHandler: Failed to handle packet type " + type, e);
            }
        } catch (Exception e) {
            LookingGlass.log.warn("PacketHandler: Failed to read packet", e);
        }
    }
}
