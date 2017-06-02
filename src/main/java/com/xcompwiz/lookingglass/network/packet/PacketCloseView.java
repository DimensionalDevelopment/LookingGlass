package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketCloseView implements IMessage {
	@SideOnly(Side.CLIENT)
	public PacketCloseView(WorldView worldview) {}

	@Override
	public void fromBytes(ByteBuf buf) {

	}

	@Override
	public void toBytes(ByteBuf buf) {

	}

	public static class Handler implements IMessageHandler<PacketCloseView, IMessage> {
		@Override
		public IMessage onMessage(PacketCloseView message, MessageContext ctx) {
			if (ModConfigs.disabled) return null;

			//TODO: make closing viewpoint aware.  See PacketCreateView
			return null;
		}
	}
}
