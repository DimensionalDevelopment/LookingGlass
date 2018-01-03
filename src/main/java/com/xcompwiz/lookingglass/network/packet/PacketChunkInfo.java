package com.xcompwiz.lookingglass.network.packet;

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
import java.util.concurrent.Semaphore;
import java.util.zip.Deflater;

public class PacketChunkInfo extends PacketHandlerBase {
    private static byte[] inflatearray;
    private static byte[] dataarray;
    private static Semaphore deflateGate = new Semaphore(1);

    private static int deflate(byte[] chunkData, byte[] compressedChunkData) {
        Deflater deflater = new Deflater(-1);
        if (compressedChunkData == null) return 0;
        int bytesize = 0;
        try {
            deflater.setInput(chunkData, 0, chunkData.length);
            deflater.finish();
            bytesize = deflater.deflate(compressedChunkData);
        } finally {
            deflater.end();
        }
        return bytesize;
    }

    public static FMLProxyPacket createPacket(Chunk chunk, boolean includeinit, int subid, int dim) {
        try {
            SPacketChunkData packet = new SPacketChunkData(chunk, 0xFFFF); // TODO

            // This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
            PacketBuffer data = PacketHandlerBase.createDataBuffer((Class<? extends PacketHandlerBase>) new Object() {}.getClass().getEnclosingClass());

            data.writeInt(dim);
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
        //PacketThreadUtil.checkThreadAndEnqueue(packet, this, this.gameController); // TODO!!!


        WorldClient proxyworld = ProxyWorldManager.getProxyworld(dim);
        if (proxyworld == null) return;
        if (proxyworld.provider.getDimension() != dim) return;

        //TODO: Test to see if this first part is even necessary
        Chunk chunk = proxyworld.getChunkProvider().provideChunk(packet.getChunkX(), packet.getChunkZ());
        if (chunk.isLoaded()) {
            System.out.println("Skipping loaded chunk at " + packet.getChunkX() + " " + packet.getChunkZ());
        } else {
            System.out.println("Setting chunk info for " + packet.getChunkX() + " " + packet.getChunkZ());

            if (packet.isFullChunk()) {
                proxyworld.doPreChunk(packet.getChunkX(), packet.getChunkZ(), true);
            }

            proxyworld.invalidateBlockReceiveRegion(packet.getChunkX() << 4, 0, packet.getChunkZ() << 4, (packet.getChunkX() << 4) + 15, 256, (packet.getChunkZ() << 4) + 15);
            //Chunk chunk = proxyworld.getChunkFromChunkCoords(packetIn.getChunkX(), packetIn.getChunkZ()); // TODO: or is this better?
            chunk.read(packet.getReadBuffer(), packet.getExtractedSize(), packet.isFullChunk());
            proxyworld.markBlockRangeForRenderUpdate(packet.getChunkX() << 4, 0, packet.getChunkZ() << 4, (packet.getChunkX() << 4) + 15, 256, (packet.getChunkZ() << 4) + 15);

            if (!packet.isFullChunk() || !(proxyworld.provider instanceof WorldProviderSurface)) {
                chunk.resetRelightChecks();
            }

            for (NBTTagCompound nbttagcompound : packet.getTileEntityTags()) {
                BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
                TileEntity tileentity = proxyworld.getTileEntity(blockpos);

                if (tileentity != null) {
                    tileentity.handleUpdateTag(nbttagcompound);
                }
            }
        }

        for (WorldView activeview : ProxyWorldManager.getWorldViews(proxyworld.provider.getDimension())) { // TODO: dim?
            activeview.onChunkReceived(packet.getChunkX(), packet.getChunkZ());
        }
    }
}
