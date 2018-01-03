package com.xcompwiz.lookingglass;

import com.google.common.collect.ImmutableList;
import com.xcompwiz.lookingglass.apiimpl.APIProviderImpl;
import com.xcompwiz.lookingglass.command.CommandCreateView;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.core.LookingGlassForgeEventHandler;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import com.xcompwiz.lookingglass.imc.IMCHandler;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.network.packet.*;
import com.xcompwiz.lookingglass.proxyworld.LookingGlassEventHandler;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.io.File;

@Mod(modid = LookingGlass.MODID, name = "LookingGlass", version = LookingGlass.VERSION)
public class LookingGlass {
    public static final String MODID = "lookingglass";
    public static final String VERSION = "${version}";

    @Mod.Instance(LookingGlass.MODID)
    public static LookingGlass instance;

    @SidedProxy(clientSide = "com.xcompwiz.lookingglass.client.ClientProxy", serverSide = "com.xcompwiz.lookingglass.core.CommonProxy")
    public static CommonProxy sidedProxy;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        //Initialize the packet handling
        LookingGlassPacketManager.registerPacketHandler(new PacketCreateView(), (byte) 10);
        LookingGlassPacketManager.registerPacketHandler(new PacketCloseView(), (byte) 11);
        LookingGlassPacketManager.registerPacketHandler(new PacketWorldInfo(), (byte) 100);
        LookingGlassPacketManager.registerPacketHandler(new PacketChunkInfo(), (byte) 101);
        LookingGlassPacketManager.registerPacketHandler(new PacketTileEntityNBT(), (byte) 102);
        LookingGlassPacketManager.registerPacketHandler(new PacketRequestWorldInfo(), (byte) 200);
        LookingGlassPacketManager.registerPacketHandler(new PacketRequestChunk(), (byte) 201);
        LookingGlassPacketManager.registerPacketHandler(new PacketRequestTE(), (byte) 202);

        LookingGlassPacketManager.bus = NetworkRegistry.INSTANCE.newEventDrivenChannel(LookingGlassPacketManager.CHANNEL);
        LookingGlassPacketManager.bus.register(new LookingGlassPacketManager());

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

        sidedProxy.preinit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Our one and only entity.
        EntityRegistry.registerModEntity(new ResourceLocation(LookingGlass.MODID, "portal"), EntityPortal.class, "lookingglass.portal", 216, this, 64, 10, false);
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
        ((CommandHandler) mcserver.getCommandManager()).registerCommand(new CommandCreateView());
        // Start up the packet dispatcher we use for throttled data to client.
        ServerPacketDispatcher.getInstance().start(); //Note: This might need to be preceded by a force init of the ServerPacketDispatcher.  Doesn't seem to currently have any issues, though.
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
        // Shutdown our throttled packet dispatcher
        ServerPacketDispatcher.getInstance().halt();
        ServerPacketDispatcher.shutdown();
    }
}
