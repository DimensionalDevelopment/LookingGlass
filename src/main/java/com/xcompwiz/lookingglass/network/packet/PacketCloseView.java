package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketCloseView extends PacketHandlerBase {
    @SideOnly(Side.CLIENT)
    public static FMLProxyPacket createPacket(WorldView worldView) {
        PacketBuffer data = PacketHandlerBase.createDataBuffer(PacketCloseView.class);
        // TODO
        return buildPacket(data);
    }

    @Override
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void handle(PacketBuffer data, EntityPlayer player) {
        if (ModConfigs.disabled) return;
        //TODO: make closing viewpoint aware. See PacketCreateView
    }
}
