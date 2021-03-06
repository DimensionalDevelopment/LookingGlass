package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

/**
 * This class is the parent of the packet handling classes for our network communication. Mostly contains helper functions.
 */
public abstract class PacketHandlerBase {

    /**
     * Called by our packet manager to process packet data
     */
    public abstract void handle(PacketBuffer data, EntityPlayer player);

    /**
     * Used by the progeny of this class in order to produce and prepare the buffer for packet data. Includes writing the correct packet id for the packet.
     */
    public static PacketBuffer createDataBuffer(Class<? extends PacketHandlerBase> handlerclass) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeByte(LookingGlassPacketManager.getId(handlerclass));
        return data;
    }

    /**
     * Used by the progeny of this class in order to produce a packet object from the data buffer. Automatically uses our packet channel so that the manager on
     * the other side will receive the packet.
     */
    protected static FMLProxyPacket buildPacket(PacketBuffer payload) {
        return new FMLProxyPacket(payload, LookingGlassPacketManager.CHANNEL);
    }
}
