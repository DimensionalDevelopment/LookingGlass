package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketTileEntityNBT implements IMessage {
	public int dim, x, y, z;
	public NBTTagCompound tag;

	public PacketTileEntityNBT(int xPos, int yPos, int zPos, NBTTagCompound nbt, int dim) {
		this.dim = dim;
		this.x = x;
		this.y = y;
		this.z = z;
		tag = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dim = buf.readInt();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dim);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeTag(buf, tag);
	}

	public static class Handler implements IMessageHandler<PacketTileEntityNBT,IMessage> {

		@SideOnly(Side.CLIENT)
		@Override
		public IMessage onMessage(PacketTileEntityNBT message, MessageContext ctx) {
			BlockPos pos = new BlockPos(message.x, message.y, message.z);
			WorldClient proxyworld = ProxyWorldManager.getProxyworld(message.dim);
			if (proxyworld == null) return null;
			if (proxyworld.provider.getDimension() != message.dim) return null;
			if (proxyworld.isAirBlock(pos)) {
				TileEntity tileentity = proxyworld.getTileEntity(pos);

				if (tileentity != null) {
					tileentity.readFromNBT(message.tag);
				} else {
					//Create tile entity from data
					tileentity = TileEntity.create(proxyworld, message.tag);
					if (tileentity != null) {
						proxyworld.addTileEntity(tileentity);
					}
				}
				proxyworld.markChunkDirty(pos, tileentity);
				proxyworld.setTileEntity(pos, tileentity);
				//proxyworld.markBlockForUpdate(xPos, yPos, zPos);
			}

			return null;
		}
	}
}
