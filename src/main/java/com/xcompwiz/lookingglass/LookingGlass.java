package com.xcompwiz.lookingglass;

import java.io.File;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import com.google.common.collect.ImmutableList;
import com.xcompwiz.lookingglass.apiimpl.APIProviderImpl;
import com.xcompwiz.lookingglass.command.CommandCreateView;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.core.LookingGlassForgeEventHandler;
import com.xcompwiz.lookingglass.imc.IMCHandler;
import com.xcompwiz.lookingglass.network.packet.PacketChunkInfo;
import com.xcompwiz.lookingglass.network.packet.PacketCloseView;
import com.xcompwiz.lookingglass.network.packet.PacketCreateView;
import com.xcompwiz.lookingglass.network.packet.PacketRequestChunk;
import com.xcompwiz.lookingglass.network.packet.PacketRequestTE;
import com.xcompwiz.lookingglass.network.packet.PacketRequestWorldInfo;
import com.xcompwiz.lookingglass.network.packet.PacketTileEntityNBT;
import com.xcompwiz.lookingglass.network.packet.PacketWorldInfo;
import com.xcompwiz.lookingglass.proxyworld.LookingGlassEventHandler;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = LookingGlass.MODID, name = "LookingGlass", version = LookingGlass.VERSION)
public class LookingGlass {
	public static final String	MODID	= "LookingGlass";
	public static final String	VERSION	= "@VERSION@";

	public SimpleNetworkWrapper INSTANCE;

	@Mod.Instance(LookingGlass.MODID)
	public static LookingGlass	instance;

	@SidedProxy(clientSide = "com.xcompwiz.lookingglass.client.ClientProxy", serverSide = "com.xcompwiz.lookingglass.core.CommonProxy")
	public static CommonProxy	sidedProxy;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		//Initialize the packet handling
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

		INSTANCE.registerMessage(PacketCreateView.Handler.class, PacketCreateView.class, 10, Side.CLIENT);
		INSTANCE.registerMessage(PacketCloseView.Handler.class, PacketCloseView.class, 11, Side.CLIENT);
		INSTANCE.registerMessage(PacketWorldInfo.Handler.class, PacketWorldInfo.class, 100, Side.SERVER);
		INSTANCE.registerMessage(PacketChunkInfo.Handler.class, PacketChunkInfo.class, 101, Side.SERVER);
		INSTANCE.registerMessage(PacketTileEntityNBT.Handler.class, PacketTileEntityNBT.class, 102, Side.SERVER);
		INSTANCE.registerMessage(PacketRequestWorldInfo.Handler.class, PacketRequestWorldInfo.class, 200, Side.CLIENT);
		INSTANCE.registerMessage(PacketRequestChunk.Handler.class, PacketRequestChunk.class, 201, Side.CLIENT);
		INSTANCE.registerMessage(PacketRequestTE.Handler.class, PacketRequestTE.class, 202, Side.CLIENT);

		// Load our basic configs
		ModConfigs.loadConfigs(new Configuration(event.getSuggestedConfigurationFile()));

		// Here we use the recommended config file to establish a good place to put a log file for any proxy world error logs.  Used primarily to log the full errors when ticking or rendering proxy worlds. 
		File configroot = event.getSuggestedConfigurationFile().getParentFile();
		// Main tick handler. Handles FML events.
		FMLCommonHandler.instance().bus().register(new LookingGlassEventHandler(new File(configroot.getParentFile(), "logs/proxyworlds.log")));
		// Forge event handler
		MinecraftForge.EVENT_BUS.register(new LookingGlassForgeEventHandler());

		// Initialize the API provider system.  Beware, this way be dragons.
		APIProviderImpl.init();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		// Our one and only entity.
		EntityRegistry.registerModEntity(com.xcompwiz.lookingglass.entity.EntityPortal.class, "lookingglass.portal", 216, this, 64, 10, false);

		sidedProxy.init();
	}

	@Mod.EventHandler
	public void handleIMC(FMLInterModComms.IMCEvent event) {
		// Catch IMC messages and send them off to our IMC handler
		ImmutableList<FMLInterModComms.IMCMessage> messages = event.getMessages();
		IMCHandler.process(messages);
	}

	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event) {}

	@Mod.EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		MinecraftServer mcserver = event.getServer();
		// Register commands
		((ServerCommandManager) mcserver.getCommandManager()).registerCommand(new CommandCreateView());
	}
}
