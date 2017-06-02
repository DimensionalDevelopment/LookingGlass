package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestTE implements IMessage {
	int dim, x, y, z;
	public PacketRequestTE(int xPos, int yPos, int zPos, int dim) {
		this.dim = dim;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dim = buf.readInt();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dim);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	public static class Handler implements IMessageHandler<PacketRequestTE, PacketTileEntityNBT> {

		@Override
		public PacketTileEntityNBT onMessage(PacketRequestTE message, MessageContext ctx) {
			if (ModConfigs.disabled) return null;

			if (!DimensionManager.isDimensionRegistered(message.dim)) return null;
			WorldServer world = DimensionManager.getWorld(message.dim);
			BlockPos pos = new BlockPos(message.x, message.y, message.z);
			if (world == null) return null;
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null) {
				//FIXME: This is currently a very "forceful" method of doing this, and not technically guaranteed to produce correct results
				// This would be much better handled by using the getDescriptionPacket method and wrapping that packet in a LookingGlass
				// packet to control delivery timing, allowing for processing the packet while the correct target world is the active world
				// This idea requires that that system be in place, though, so until then this hack will hopefully hold.
				NBTTagCompound tag = new NBTTagCompound();
				tile.writeToNBT(tag);
				return new PacketTileEntityNBT(message.x, message.y, message.z, tag, message.dim);
			}

			return null;
		}
	}
}
