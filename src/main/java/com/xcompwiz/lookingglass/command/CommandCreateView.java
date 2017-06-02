package com.xcompwiz.lookingglass.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.xcompwiz.lookingglass.entity.EntityPortal;

public class CommandCreateView extends CommandBaseAdv {
	@Override
	public String getName() {
		return "lg-viewdim";
	}

	@Override
	public String getUsage(ICommandSender par1ICommandSender) {
		return "/" + this.getName() + " targetdim [dim, x, y, z]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender agent, String[] args) {
		int targetdim = 0;
		Integer dim = null;
		BlockPos coords = null;

		//XXX: Set Coordinates of view location?
		if (args.length > 0) {
			String sTarget = args[0];
			try {
				targetdim = parseInt(sTarget);
			} catch (NumberInvalidException e) {
				e.printStackTrace();
			}
		} else {
			try {
				throw new WrongUsageException("Could not parse command.");
			} catch (WrongUsageException e) {
				e.printStackTrace();
			}
		}
		if (args.length > 4) {
			try {
				dim = parseInt(args[1]);
			} catch (NumberInvalidException e) {
				e.printStackTrace();
			}
			Entity caller = null;
			try {
				caller = getCommandSenderAsPlayer(agent);
			} catch (Exception e) {
			}
			int x = 0;
			try {
				x = (int) handleRelativeNumber(agent, (caller != null ? caller.posX : 0), args[2]);
				int y = (int) handleRelativeNumber(agent, (caller != null ? caller.posY : 0), args[3], 0, 0);
				int z = (int) handleRelativeNumber(agent, (caller != null ? caller.posZ : 0), args[4]);
				coords = new BlockPos(x, y, z);
			} catch (NumberInvalidException e) {
				e.printStackTrace();
			}
		}
		if (coords == null) {
			dim = getSenderDimension(agent);
			coords = agent.getPosition();
		}
		if (coords == null) try {
			throw new WrongUsageException("Location Required");
		} catch (WrongUsageException e) {
			e.printStackTrace();
		}

		WorldServer worldObj = DimensionManager.getWorld(dim);
		if (worldObj == null) {
			try {
				throw new CommandException("The target world is not loaded");
			} catch (CommandException e) {
				e.printStackTrace();
			}
		}

		EntityPortal portal = new EntityPortal(worldObj, targetdim, coords.getX(), coords.getY(), coords.getZ());
		worldObj.spawnEntity(portal);

		sendToAdmins(agent, "A window to dimension " + targetdim + " has been created.", new Object[0]);
	}
}
