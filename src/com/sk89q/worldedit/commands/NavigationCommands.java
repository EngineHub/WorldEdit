// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.*;

/**
 * Navigation commands.
 * 
 * @author sk89q
 */
public class NavigationCommands {
    @Command(
        aliases = {"unstuck"},
        usage = "",
        desc = "Escape from being stuck inside a block",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.navigation.unstuck"})
    public static void unstuck(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        player.print("There you go!");
        player.findFreePosition();
    }

    @Command(
        aliases = {"ascend"},
        usage = "",
        desc = "Go up a floor",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.navigation.ascend"})
    public static void ascend(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        if (player.ascendLevel()) {
            player.print("Ascended a level.");
        } else {
            player.printError("No free spot above you found.");
        }
    }

    @Command(
        aliases = {"descend"},
        usage = "",
        desc = "Go down a floor",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.navigation.descend"})
    public static void descend(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        if (player.descendLevel()) {
            player.print("Descended a level.");
        } else {
            player.printError("No free spot below you found.");
        }
    }

    @Command(
        aliases = {"ceil"},
        usage = "[clearance]",
        desc = "Go to the celing",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.navigation.ceiling"})
    public static void ceiling(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int clearence = args.argsLength() > 0 ?
            Math.max(0, args.getInteger(0)) : 0;

        if (player.ascendToCeiling(clearence)) {
            player.print("Whoosh!");
        } else {
            player.printError("No free spot above you found.");
        }
    }
    
    @Command(
        aliases = {"thru"},
        usage = "",
        desc = "Passthrough walls",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.navigation.thru"})
    public static void thru(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        if (player.passThroughForwardWall(6)) {
            player.print("Whoosh!");
        } else {
            player.printError("No free spot ahead of you found.");
        }
    }

    @Command(
        aliases = {"jumpto"},
        usage = "",
        desc = "Teleport to a location",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.navigation.jumpto"})
    public static void jumpTo(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        WorldVector pos = player.getSolidBlockTrace(300);
        if (pos != null) {
            player.findFreePosition(pos);
            player.print("Poof!");
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        aliases = {"up"},
        usage = "<block>",
        desc = "Go upwards some distance",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.navigation.up"})
    public static void up(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int distance = args.getInteger(0);

        if (player.ascendUpwards(distance)) {
            player.print("Whoosh!");
        } else {
            player.printError("You would hit something above you.");
        }
    }
}
