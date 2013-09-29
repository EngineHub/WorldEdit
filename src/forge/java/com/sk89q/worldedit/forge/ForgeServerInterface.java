package com.sk89q.worldedit.forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;

import cpw.mods.fml.common.FMLCommonHandler;

public class ForgeServerInterface extends ServerInterface {
    private MinecraftServer server;
    private ForgeBiomeTypes biomes;

    public ForgeServerInterface() {
        this.server = FMLCommonHandler.instance().getMinecraftServerInstance();
        this.biomes = new ForgeBiomeTypes();
    }

    public int resolveItem(String name) {
    	if (name == null) return 0;
        for (Item item : Item.itemsList) {
            if (item == null) continue;
            if (item.getUnlocalizedName() == null) continue;
            if (item.getUnlocalizedName().startsWith("item.")) {
                if (item.getUnlocalizedName().equalsIgnoreCase("item." + name)) return item.itemID;
            }
            if (item.getUnlocalizedName().startsWith("tile.")) {
                if (item.getUnlocalizedName().equalsIgnoreCase("tile." + name)) return item.itemID;
            }
            if (item.getUnlocalizedName().equalsIgnoreCase(name)) return item.itemID;
        }
        return 0;
    }

    public boolean isValidMobType(String type) {
        return EntityList.stringToClassMapping.containsKey(type);
    }

    public void reload() {
    }

    public BiomeTypes getBiomes() {
        return this.biomes;
    }

    public int schedule(long delay, long period, Runnable task) {
        return -1;
    }

    public List<LocalWorld> getWorlds() {
        List<WorldServer> worlds = Arrays.asList(DimensionManager.getWorlds());
        List<LocalWorld> ret = new ArrayList<LocalWorld>(worlds.size());
        for (WorldServer world : worlds) {
            ret.add(new ForgeWorld(world));
        }
        return ret;
    }

    @Override
    public void onCommandRegistration(List<Command> commands) {
        if (server == null) return;
        ServerCommandManager mcMan = (ServerCommandManager) server.getCommandManager();
        for (final Command cmd : commands) {
            mcMan.registerCommand(new CommandBase() {
                @Override
                public String getCommandName() {
                    return cmd.aliases()[0];
                }

                @Override
                public List<String> getCommandAliases() {
                    return Arrays.asList(cmd.aliases());
                }

                @Override
                public void processCommand(ICommandSender var1, String[] var2) {}

                @Override
                public String getCommandUsage(ICommandSender icommandsender) {
                    return "/" + cmd.aliases()[0] + " " + cmd.usage();
                }
            });
        }
    }
}
