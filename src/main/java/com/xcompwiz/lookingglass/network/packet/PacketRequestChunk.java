package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestChunk implements IMessage {
	public int x, z, dim;

	public PacketRequestChunk(int xPos, int zPos, int dim) {
		this.dim = dim;
		this.x = xPos;
		this.z = zPos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dim = buf.readInt();
		x = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dim);
		buf.writeInt(x);
		buf.writeInt(z);
	}

	public static class Handler implements IMessageHandler<PacketRequestChunk, PacketChunkInfo> {

		@Override
		public PacketChunkInfo onMessage(PacketRequestChunk message, MessageContext ctx) {
			if (ModConfigs.disabled) return null;

			if (!DimensionManager.isDimensionRegistered(message.dim)) return null;
			WorldServer world = DimensionManager.getWorld(message.dim);
			if (world == null) return null;
			Chunk chunk = world.getChunkFromChunkCoords(message.x, message.z);
			if (!chunk.isLoaded()) chunk = world.getChunkProvider().loadChunk(message.x, message.z);
			return new PacketChunkInfo(chunk, message.dim);
		}
	}
}
