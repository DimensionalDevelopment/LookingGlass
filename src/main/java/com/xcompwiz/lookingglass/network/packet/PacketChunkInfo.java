package com.xcompwiz.lookingglass.network.packet;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketChunkInfo implements IMessage {
	public int dim;
	public SPacketChunkData data;

	public PacketChunkInfo(Chunk chunk, int dim) {
		this.dim = dim;
		data = new SPacketChunkData(chunk, 65535);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		dim = buf.readInt();
		try {
			data.readPacketData(new PacketBuffer(buf));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(dim);
		try {
			data.writePacketData(new PacketBuffer(buf));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static class Handler implements IMessageHandler<PacketChunkInfo, IMessage> {
    	@SideOnly(Side.CLIENT)
	    public void receivedChunk(WorldClient worldObj, int cx, int cz) {
	    	worldObj.markBlockRangeForRenderUpdate(cx << 4, 0, cz << 4, (cx << 4) + 15, 256, (cz << 4) + 15);
		    Chunk c = worldObj.getChunkFromChunkCoords(cx, cz);
		    if (c == null || c.isEmpty()) return;

    		for (WorldView activeview : ProxyWorldManager.getWorldViews(worldObj.provider.getDimension())) {
	    		activeview.onChunkReceived(cx, cz);
		    }
        }

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(PacketChunkInfo message, MessageContext ctx) {
            int dim = message.dim;
            SPacketChunkData chunkData = message.data;

            if (chunkData == null) {
                return new PacketRequestChunk(chunkData.getChunkX(), chunkData.getChunkZ(), dim);
            }

            handleChunkData(chunkData, dim);

            return null;
        }

        /**
         * Updates the specified chunk with the supplied data, marks it for re-rendering and lighting recalculation
         */
        @SideOnly(Side.CLIENT)
        public void handleChunkData(SPacketChunkData packetIn, int dim) {
            WorldClient proxyworld = ProxyWorldManager.getProxyworld(dim);
            if (proxyworld == null) return;
            if (proxyworld.provider.getDimension() != dim) return;

            if (packetIn.doChunkLoad()) {
                proxyworld.doPreChunk(packetIn.getChunkX(), packetIn.getChunkZ(), true);
            }

            proxyworld.invalidateBlockReceiveRegion(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);
            Chunk chunk = proxyworld.getChunkFromChunkCoords(packetIn.getChunkX(), packetIn.getChunkZ());
            chunk.fillChunk(packetIn.getReadBuffer(), packetIn.getExtractedSize(), packetIn.doChunkLoad());
            receivedChunk(proxyworld, packetIn.getChunkX(), packetIn.getChunkZ());
            proxyworld.markBlockRangeForRenderUpdate(packetIn.getChunkX() << 4, 0, packetIn.getChunkZ() << 4, (packetIn.getChunkX() << 4) + 15, 256, (packetIn.getChunkZ() << 4) + 15);

            if (!packetIn.doChunkLoad() || !(proxyworld.provider instanceof WorldProviderSurface)) {
                chunk.resetRelightChecks();
            }

            for (NBTTagCompound nbttagcompound : packetIn.getTileEntityTags()) {
                BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
                TileEntity tileentity = proxyworld.getTileEntity(blockpos);

                if (tileentity != null) {
                    tileentity.handleUpdateTag(nbttagcompound);
                }
            }
        }
    }
}
