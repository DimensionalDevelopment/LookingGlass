package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.IOException;

public class PacketChunkInfo extends PacketHandlerBase {

    public static FMLProxyPacket createPacket(Chunk chunk, int subid, int dim) {
        try {
            PacketBuffer data = PacketHandlerBase.createDataBuffer(PacketChunkInfo.class);
            data.writeInt(dim);
            SPacketChunkData packet = new SPacketChunkData(chunk, 0xFFFF); // TODO don't resend whole chunk
            packet.writePacketData(data);
            return buildPacket(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(PacketBuffer in, EntityPlayer player) {
        try {
            int dim = in.readInt();
            SPacketChunkData packet = new SPacketChunkData();
            packet.readPacketData(in);
            handleChunkData(dim, packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleChunkData(int dim, SPacketChunkData packet) {
        WorldClient proxyWorld = ProxyWorldManager.getProxyWorld(dim);
        if (proxyWorld == null) return;
        if (proxyWorld.provider.getDimension() != dim) return;

        //TODO: Test to see if this first part is even necessary
        Chunk chunk = proxyWorld.getChunkFromChunkCoords(packet.getChunkX(), packet.getChunkZ());
        if (chunk.isLoaded()) {
            LookingGlass.log.debug("Skipping loaded chunk at " + packet.getChunkX() + " " + packet.getChunkZ());
        } else {
            LookingGlass.log.debug("Setting chunk info for " + packet.getChunkX() + " " + packet.getChunkZ());

            if (packet.isFullChunk()) {
                proxyWorld.doPreChunk(packet.getChunkX(), packet.getChunkZ(), true);
            }

            proxyWorld.invalidateBlockReceiveRegion(packet.getChunkX() << 4, 0, packet.getChunkZ() << 4, (packet.getChunkX() << 4) + 15, 256, (packet.getChunkZ() << 4) + 15);
            chunk.read(packet.getReadBuffer(), packet.getExtractedSize(), packet.isFullChunk());
            proxyWorld.markBlockRangeForRenderUpdate(packet.getChunkX() << 4, 0, packet.getChunkZ() << 4, (packet.getChunkX() << 4) + 15, 256, (packet.getChunkZ() << 4) + 15);

            if (!packet.isFullChunk() || !(proxyWorld.provider instanceof WorldProviderSurface)) {
                chunk.resetRelightChecks();
            }

            for (NBTTagCompound nbttagcompound : packet.getTileEntityTags()) {
                BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
                TileEntity tileentity = proxyWorld.getTileEntity(blockpos);

                if (tileentity != null) {
                    tileentity.handleUpdateTag(nbttagcompound);
                }
            }
        }

        for (WorldView activeview : ProxyWorldManager.getWorldViews(proxyWorld.provider.getDimension())) { // TODO: dim?
            activeview.onChunkReceived(packet.getChunkX(), packet.getChunkZ());
        }
    }
}
