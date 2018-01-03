package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.util.Collection;

/**
 * Based on code from Ken Butler/shadowking97
 */
public class PacketWorldInfo extends PacketHandlerBase {

    public static FMLProxyPacket createPacket(int dimension) {
        WorldServer world = DimensionManager.getWorld(0).getMinecraftServer().getWorld(dimension);
        if (world == null) {
            LookingGlass.log.warn("Server-side world for dimension %i is null!", dimension);
            return null;
        }
        BlockPos pos = world.provider.getSpawnPoint();
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        int skylightSubtracted = world.getSkylightSubtracted();
        float thunderingStrength = world.thunderingStrength;
        float rainingStrength = world.rainingStrength;
        long worldTime = world.provider.getWorldTime();

        // This line may look like black magic (and, well, it is), but it's actually just returning a class reference for this class. Copy-paste safe.
        PacketBuffer data = PacketHandlerBase.createDataBuffer(PacketWorldInfo.class);

        data.writeInt(dimension);
        data.writeInt(posX);
        data.writeInt(posY);
        data.writeInt(posZ);
        data.writeInt(skylightSubtracted);
        data.writeFloat(thunderingStrength);
        data.writeFloat(rainingStrength);
        data.writeLong(worldTime);

        return buildPacket(data);
    }

    @Override
    public void handle(PacketBuffer in, EntityPlayer player) {
        int dimension = in.readInt();
        int posX = in.readInt();
        int posY = in.readInt();
        int posZ = in.readInt();
        int skylightSubtracted = in.readInt();
        float thunderingStrength = in.readFloat();
        float rainingStrength = in.readFloat();
        long worldTime = in.readLong();

        WorldClient proxyworld = ProxyWorldManager.getProxyWorld(dimension);

        if (proxyworld == null) return;
        if (proxyworld.provider.getDimension() != dimension) return;

        BlockPos pos = new BlockPos(posX, posY, posZ);
        Collection<WorldView> views = ProxyWorldManager.getWorldViews(dimension);
        for (WorldView view : views) {
            view.updateWorldSpawn(pos);
        }
        proxyworld.setSpawnPoint(pos);
        proxyworld.setSkylightSubtracted(skylightSubtracted);
        proxyworld.thunderingStrength = thunderingStrength;
        proxyworld.setRainStrength(rainingStrength);
        proxyworld.setWorldTime(worldTime);
    }
}
