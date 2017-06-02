package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;

import java.util.Collection;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.log.LoggerUtils;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketWorldInfo implements IMessage {
	int dim, x, y, z, skylightSubtracted;
	float thunderingStrength, rainingStrength;
	long worldTime;

	public PacketWorldInfo(int dimension) {
		WorldServer world = DimensionManager.getWorld(dimension);
		if (world == null) {
			LoggerUtils.warn("Server-side world for dimension %i is null!", dimension);
			return;
		}

		dim = dimension;
		BlockPos cc = world.provider.getSpawnPoint();
		x = cc.getX();
		y = cc.getY();
		z = cc.getZ();
		skylightSubtracted = world.getSkylightSubtracted();
		thunderingStrength = world.thunderingStrength;
		rainingStrength = world.rainingStrength;
		worldTime = world.provider.getWorldTime();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
        dim = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        skylightSubtracted = buf.readInt();
        thunderingStrength = buf.readFloat();
        rainingStrength = buf.readFloat();
        worldTime = buf.readLong();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dim);
		buf.writeInt(x);
		buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(skylightSubtracted);
        buf.writeFloat(thunderingStrength);
        buf.writeFloat(rainingStrength);
        buf.writeLong(worldTime);
	}

	@SideOnly(Side.CLIENT)
	public static class Handler implements IMessageHandler<PacketWorldInfo, IMessage> {

        @Override
        public IMessage onMessage(PacketWorldInfo message, MessageContext ctx) {
            int dimension = message.dim;
            int posX = message.x;
            int posY = message.y;
            int posZ = message.z;
            int skylightSubtracted = message.skylightSubtracted;
            float thunderingStrength = message.thunderingStrength;
            float rainingStrength = message.rainingStrength;
            long worldTime = message.worldTime;

            WorldClient proxyworld = ProxyWorldManager.getProxyworld(dimension);

            if (proxyworld == null) return null;
            if (proxyworld.provider.getDimension() != dimension) return null;

            BlockPos cc = new BlockPos(posX, posY, posZ);

            Collection<WorldView> views = ProxyWorldManager.getWorldViews(dimension);
            for (WorldView view : views) {
                view.updateWorldSpawn(cc);
            }
            proxyworld.setSpawnPoint(cc);
            proxyworld.setSkylightSubtracted(skylightSubtracted);
            proxyworld.thunderingStrength = thunderingStrength;
            proxyworld.setRainStrength(rainingStrength);
            proxyworld.setWorldTime(worldTime);

            return null;
        }
    }
}
