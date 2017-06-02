package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class PacketRequestWorldInfo implements IMessage {
	public int dim;

	public PacketRequestWorldInfo(int xPos, int yPos, int zPos, int dim) {
		this.dim = dim;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dim = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dim);
	}

	public static class Handler implements IMessageHandler<PacketRequestWorldInfo, PacketWorldInfo> {

        @Override
        public PacketWorldInfo onMessage(PacketRequestWorldInfo message, MessageContext ctx) {
            if (ModConfigs.disabled) return null;
            int dim = message.dim;

            if (!DimensionManager.isDimensionRegistered(dim)) return null;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dim, ctx.getServerHandler().playerEntity));
            return new PacketWorldInfo(dim);
        }
    }
}
