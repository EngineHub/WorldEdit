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
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Generation commands.
 * 
 * @author sk89q
 */
public class GenerationCommands {
    @Command(
        aliases = {"/hcyl"},
        usage = "<block> <radius> [height] ",
        desc = "Generate a hollow cylinder",
        min = 2,
        max = 3
    )
    @CommandPermissions({"worldedit.generation.cylinder"})
    public static void hcyl(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0));
        int radius = Math.max(1, args.getInteger(1));
        int height = args.argsLength() > 2 ? args.getInteger(2) : 1;

        Vector pos = session.getPlacementPosition(player);
        int affected = editSession.makeHollowCylinder(pos, block, radius, height);
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = {"/cyl"},
        usage = "<block> <radius> [height] ",
        desc = "Generate a cylinder",
        min = 2,
        max = 3
    )
    @CommandPermissions({"worldedit.generation.cylinder"})
    public static void cyl(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0));
        int radius = Math.max(1, args.getInteger(1));
        int height = args.argsLength() > 2 ? args.getInteger(2) : 1;

        Vector pos = session.getPlacementPosition(player);
        int affected = editSession.makeCylinder(pos, block, radius, height);
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = {"/hsphere"},
        usage = "<block> <radius> [raised?] ",
        desc = "Generate a hollow sphere",
        min = 2,
        max = 3
    )
    @CommandPermissions({"worldedit.generation.sphere"})
    public static void hsphere(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0));
        int radius = Math.max(1, args.getInteger(1));
        boolean raised = args.argsLength() > 2
                ? (args.getString(2).equalsIgnoreCase("true")
                        || args.getString(2).equalsIgnoreCase("yes"))
                : false;

        Vector pos = session.getPlacementPosition(player);
        if (raised) {
            pos = pos.add(0, radius, 0);
        }

        int affected = editSession.makeSphere(pos, block, radius, false);
        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = {"/sphere"},
        usage = "<block> <radius> [raised?] ",
        desc = "Generate a filled sphere",
        min = 2,
        max = 3
    )
    @CommandPermissions({"worldedit.generation.sphere"})
    public static void sphere(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0));
        int radius = Math.max(1, args.getInteger(1));
        boolean raised = args.argsLength() > 2
                ? (args.getString(2).equalsIgnoreCase("true")
                        || args.getString(2).equalsIgnoreCase("yes"))
                : false;

        Vector pos = session.getPlacementPosition(player);
        if (raised) {
            pos = pos.add(0, radius, 0);
        }

        int affected = editSession.makeSphere(pos, block, radius, true);
        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = {"forestgen"},
        usage = "[size] [density] ",
        desc = "Generate a forest",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.generation.forest"})
    public static void forestGen(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 10;
        double density = args.argsLength() > 1 ? Double.parseDouble(args.getString(1)) / 100 : 0.05;

        int affected = editSession.makeForest(player.getPosition(),
                size, density, false);
        player.print(affected + " trees created.");
    }
    
    @Command(
        aliases = {"pinegen"},
        usage = "[size] [density]",
        desc = "Generate a pine forest",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.generation.forest"})
    public static void pineGen(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 10;
        double density = args.argsLength() > 1 ? Double.parseDouble(args.getString(1)) / 100 : 0.05;

        int affected = editSession.makeForest(player.getPosition(),
                size, density, true);
        player.print(affected + " pine trees created.");
    }
    
    @Command(
        aliases = {"pumpkins"},
        usage = "[size]",
        desc = "Generate pumpkin patches",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.generation.pumpkins"})
    public static void pumpkins(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 10;

        int affected = editSession.makePumpkinPatches(player.getPosition(), size);
        player.print(affected + " pumpkin patches created.");
    }
}
