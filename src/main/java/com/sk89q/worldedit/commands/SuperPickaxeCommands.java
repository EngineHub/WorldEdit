// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.tools.AreaPickaxe;
import com.sk89q.worldedit.tools.RecursivePickaxe;
import com.sk89q.worldedit.tools.SinglePickaxe;

public class SuperPickaxeCommands {    
    @Command(
        aliases = {"single"},
        usage = "",
        desc = "Enable the single block super pickaxe mode",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe"})
    public static void single(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setSuperPickaxe(new SinglePickaxe());
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }
    
    @Command(
        aliases = {"area"},
        usage = "<radius>",
        desc = "Enable the area super pickaxe pickaxe mode",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.superpickaxe.area"})
    public static void area(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        int range = args.getInteger(0);
        
        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }
        
        session.setSuperPickaxe(new AreaPickaxe(range));
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }
    
    @Command(
        aliases = {"recur", "recursive"},
        usage = "<radius>",
        desc = "Enable the recursive super pickaxe pickaxe mode",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.superpickaxe.recursive"})
    public static void recursive(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        double range = args.getDouble(0);
        
        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }
        
        session.setSuperPickaxe(new RecursivePickaxe(range));
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }
}
