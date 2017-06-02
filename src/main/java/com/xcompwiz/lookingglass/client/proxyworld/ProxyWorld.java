package com.xcompwiz.lookingglass.client.proxyworld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

// FIXME: AAHH! Fake world classes! EXTERMINATE!
public class ProxyWorld extends WorldClient {
	public ProxyWorld(int dimensionID) {
		super(Minecraft.getMinecraft().getConnection(), new WorldSettings(0L, GameType.SURVIVAL, true, false, WorldType.DEFAULT), dimensionID, Minecraft.getMinecraft().gameSettings.difficulty, Minecraft.getMinecraft().world.theProfiler);
	}
}
