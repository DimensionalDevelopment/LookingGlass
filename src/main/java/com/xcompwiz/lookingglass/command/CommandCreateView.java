package com.xcompwiz.lookingglass.command;

import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class CommandCreateView extends CommandBaseAdv {
    @Override
    public String getName() {
        return "lg-viewdim";
    }

    @Override
    public String getUsage(ICommandSender par1ICommandSender) {
        return "/" + getName() + " dim x y z";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 4) throw new WrongUsageException("Could not parse command.");

        int targetDim = parseInt(args[0]);
        BlockPos targetPos = new BlockPos(parseInt(args[1]), parseInt(args[2]), parseInt(args[3]));

        EntityPortal portal = new EntityPortal(sender.getEntityWorld(), sender.getPosition(), targetDim, targetPos);
        sender.getEntityWorld().spawnEntity(portal);

        sendToAdmins(sender, "A window to dimension " + targetDim + " at " + targetPos + " has been created.", new Object[0]);
    }
}
