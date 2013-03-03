package com.sk89q.worldedit.forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.EntityList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;

import cpw.mods.fml.common.FMLCommonHandler;

public class ForgeServerInterface extends ServerInterface {
    private WorldEditMod mod;
    private MinecraftServer server;
    private ForgeBiomeTypes biomes;

    public ForgeServerInterface() {
        this.mod = WorldEditMod.inst;
        this.server = FMLCommonHandler.instance().getMinecraftServerInstance();
        this.biomes = new ForgeBiomeTypes();
    }

    public int resolveItem(String name) {
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
        List<WorldServer> worlds = Arrays.asList(this.server.worldServers);
        List<LocalWorld> ret = new ArrayList(worlds.size());
        for (WorldServer world : worlds) {
            ret.add(new ForgeWorld(world));
        }
        return ret;
    }

    @Override
    public void onCommandRegistration(List<Command> commands) {
        if (server == null) return;
        ServerCommandManager mcMan = (ServerCommandManager) server.getCommandManager();
        for (Command cmd : commands) {
            for (int i = 0; i < cmd.aliases().length; i++) {
                final String name = cmd.aliases()[i];
                mcMan.registerCommand(new CommandBase() {
                    public String getCommandName() {
                        return name;
                    }

                    public void processCommand(ICommandSender var1, String[] var2) {
                    }
                });
            }
        }
    }
}