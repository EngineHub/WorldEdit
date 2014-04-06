/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.forge;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.ServerInterface;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ForgePlatform extends ServerInterface {
    private final ForgeWorldEdit mod;
    private final MinecraftServer server;
    private final ForgeBiomeTypes biomes;

    public ForgePlatform(ForgeWorldEdit mod) {
        this.mod = mod;
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

    public List<? extends com.sk89q.worldedit.world.World> getWorlds() {
        List<WorldServer> worlds = Arrays.asList(DimensionManager.getWorlds());
        List<com.sk89q.worldedit.world.World> ret = new ArrayList<com.sk89q.worldedit.world.World>(worlds.size());
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

                @Override
                public int compareTo(Object o) {
                    if (o instanceof ICommand) {
                        return super.compareTo((ICommand) o);
                    } else {
                        return -1;
                    }
                }
            });
        }
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "Forge-Official";
    }

    @Override
    public String getPlatformVersion() {
        return mod.getInternalVersion();
    }
}
