package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinder;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinderManager;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketCreateView implements IMessage {
	private int dim, x,y,z, renderDistance;

	@SideOnly(Side.CLIENT)
	public PacketCreateView(WorldView worldview) {
		x = 0;
		y = -1;
		z = 0;
		if (worldview.coords != null) {
			x = worldview.coords.getX() >> 4;
			y = worldview.coords.getY() >> 4;
			z = worldview.coords.getZ() >> 4;
		}

		dim = worldview.getWorldObj().provider.getDimension();
		renderDistance = Math.min(ModConfigs.renderDistance, Minecraft.getMinecraft().gameSettings.renderDistanceChunks);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dim = buf.readInt();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		renderDistance = buf.readInt();

	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dim);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(renderDistance);
	}

	public static class Handler implements IMessageHandler<PacketCreateView, PacketWorldInfo> {
		@Override
		public PacketWorldInfo onMessage(PacketCreateView message, MessageContext ctx) {
			if (ModConfigs.disabled) return null;
			int dim = message.dim;
			int xPos = message.x;
			int yPos = message.y;
			int zPos = message.z;
			int renderDistance = message.renderDistance;

			if (!DimensionManager.isDimensionRegistered(dim)) return null;
			WorldServer world = DimensionManager.getWorld(dim);
			if (world == null) return null;
			int x;
			int y;
			int z;
			if (yPos < 0) {
				BlockPos c = world.getSpawnPoint();
				x = c.getX() >> 4;
				y = c.getY() >> 4;
				z = c.getZ() >> 4;
			} else {
				x = xPos;
				y = yPos;
				z = zPos;
			}

			if (renderDistance > ModConfigs.renderDistance) renderDistance = ModConfigs.renderDistance;
			ChunkFinderManager.instance.addFinder(new ChunkFinder(new BlockPos(x, y, z), dim, world.getChunkProvider(), ctx.getServerHandler().playerEntity, renderDistance));
			//TODO: Add to tracking list.  Send time/data updates at intervals. Keep in mind to catch player disconnects when tracking clients.
			//Register ChunkFinder, and support change of finder location.
			//TODO: This is a repeat of the handling of PacketRequestWorldInfo
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dim, ctx.getServerHandler().playerEntity));
			return new PacketWorldInfo(dim);
		}
	}
}
