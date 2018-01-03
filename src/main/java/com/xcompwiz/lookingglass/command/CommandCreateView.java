package com.xcompwiz.lookingglass.command;

import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CommandCreateView extends CommandBase {
    @Override
    public String getName() {
        return "createview";
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("lg");
        return aliases;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + getName() + " dim x y z";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 4) throw new WrongUsageException("Could not parse command.");

        int targetDim = parseInt(args[0]);
        BlockPos targetPos = new BlockPos(parseInt(args[1]), parseInt(args[2]), parseInt(args[3]));

        EntityPortal portal = new EntityPortal(sender.getEntityWorld(), sender.getPosition(), targetDim, targetPos);
        sender.getEntityWorld().spawnEntity(portal);

        notifyCommandListener(sender, this, "A window to dimension " + targetDim + " at " + targetPos + " has been created.");
    }
}
