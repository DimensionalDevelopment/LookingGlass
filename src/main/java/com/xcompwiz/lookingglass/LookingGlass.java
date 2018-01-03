package com.xcompwiz.lookingglass;

import com.xcompwiz.lookingglass.command.CommandCreateView;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.core.LookingGlassForgeEventHandler;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.network.packet.PacketChunkInfo;
import com.xcompwiz.lookingglass.network.packet.PacketCloseView;
import com.xcompwiz.lookingglass.network.packet.PacketCreateView;
import com.xcompwiz.lookingglass.network.packet.PacketWorldInfo;
import com.xcompwiz.lookingglass.proxyworld.LookingGlassEventHandler;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = LookingGlass.MODID, name = "LookingGlass", version = LookingGlass.VERSION)
public class LookingGlass {
    public static final String MODID = "lookingglass";
    public static final String VERSION = "${version}";

    @Mod.Instance(LookingGlass.MODID)
    public static LookingGlass instance;
    public static Logger log;

    @SidedProxy(clientSide = "com.xcompwiz.lookingglass.client.ClientProxy",
            serverSide = "com.xcompwiz.lookingglass.core.CommonProxy")
    public static CommonProxy sidedProxy;

    @Mod.EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        log = event.getModLog();
        //Initialize the packet handling
        LookingGlassPacketManager.registerPacketHandler(new PacketCreateView(), (byte) 10);
        LookingGlassPacketManager.registerPacketHandler(new PacketCloseView(), (byte) 11);
        LookingGlassPacketManager.registerPacketHandler(new PacketWorldInfo(), (byte) 100);
        LookingGlassPacketManager.registerPacketHandler(new PacketChunkInfo(), (byte) 101);

        LookingGlassPacketManager.bus = NetworkRegistry.INSTANCE.newEventDrivenChannel(LookingGlassPacketManager.CHANNEL);
        LookingGlassPacketManager.bus.register(new LookingGlassPacketManager());

        // Load our basic configs
        ModConfigs.loadConfigs(new Configuration(event.getSuggestedConfigurationFile()));

        // Here we use the recommended config file to establish a good place to put a log file for any proxy world error logs.  Used primarily to log the full errors when ticking or rendering proxy worlds.
        File configroot = event.getSuggestedConfigurationFile().getParentFile();
        // Main tick handler. Handles FML events.
        MinecraftForge.EVENT_BUS.register(new LookingGlassEventHandler(new File(configroot.getParentFile(), "logs/proxyworlds.log")));
        // Forge event handler
        MinecraftForge.EVENT_BUS.register(new LookingGlassForgeEventHandler());

        sidedProxy.onPreInitialization();
    }

    @Mod.EventHandler
    public void onInitialization(FMLInitializationEvent event) {
        // Our one and only entity.
        EntityRegistry.registerModEntity(new ResourceLocation(LookingGlass.MODID, "portal"), EntityPortal.class, "lookingglass.portal", 216, this, 64, 10, false);
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        // Register commands
        ((CommandHandler) server.getCommandManager()).registerCommand(new CommandCreateView());
        // Start up the packet dispatcher we use for throttled data to client.
        // Note: This might need to be preceded by a force init of the ServerPacketDispatcher.  Doesn't seem to currently have any issues, though.
        ServerPacketDispatcher.getInstance().start();
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
        // Shutdown our throttled packet dispatcher
        ServerPacketDispatcher.getInstance().halt();
        ServerPacketDispatcher.shutdown();
    }
}
