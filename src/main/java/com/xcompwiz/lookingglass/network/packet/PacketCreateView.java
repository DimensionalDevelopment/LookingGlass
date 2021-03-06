package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinder;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinderManager;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

public class PacketCreateView extends PacketHandlerBase {
    @SideOnly(Side.CLIENT)
    public static FMLProxyPacket createPacket(WorldView worldView) {
        PacketBuffer data = PacketHandlerBase.createDataBuffer(PacketCreateView.class);

        int x = 0;
        int y = -1;
        int z = 0;
        if (worldView.pos != null) {
            x = worldView.pos.getX() >> 4;
            y = worldView.pos.getY() >> 4;
            z = worldView.pos.getZ() >> 4;
        }

        data.writeInt(worldView.getWorld().provider.getDimension());
        data.writeInt(x);
        data.writeInt(y);
        data.writeInt(z);
        data.writeByte(Math.min(ModConfigs.renderDistance, Minecraft.getMinecraft().gameSettings.renderDistanceChunks));

        return buildPacket(data);
    }

    @Override
    public void handle(PacketBuffer data, EntityPlayer player) {
        if (ModConfigs.disabled) return;
        int dim = data.readInt();
        int xPos = data.readInt();
        int yPos = data.readInt();
        int zPos = data.readInt();
        byte renderDistance = data.readByte();

        if (!DimensionManager.isDimensionRegistered(dim)) return;
        WorldServer world = DimensionManager.getWorld(0).getMinecraftServer().getWorld(dim);
        if (world == null) return;
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
        ChunkFinderManager.instance.addFinder(new ChunkFinder(new BlockPos(x, y, z), dim, world.getChunkProvider(), player, renderDistance));
        //TODO: Add to tracking list.  Send time/data updates at intervals. Keep in mind to catch player disconnects when tracking clients.
        //Register ChunkFinder, and support change of finder location.
        //TODO: This is a repeat of the handling of PacketRequestWorldInfo
        MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dim, (EntityPlayerMP) player));
        LookingGlassPacketManager.bus.sendTo(PacketWorldInfo.createPacket(dim), (EntityPlayerMP) player);
    }
}
