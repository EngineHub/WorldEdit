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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.util.TreeGenerator;

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
    @Logging(PLACEMENT)
    public static void hcyl(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        double radius = Math.max(1, args.getDouble(1));
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
    @Logging(PLACEMENT)
    public static void cyl(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        double radius = Math.max(1, args.getDouble(1));
        int height = args.argsLength() > 2 ? args.getInteger(2) : 1;

        Vector pos = session.getPlacementPosition(player);
        int affected = editSession.makeCylinder(pos, block, radius, height);
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = {"/hsphere"},
        usage = "<block> <radius>[,<radius>,<radius>] [raised?] ",
        desc = "Generate a hollow sphere.",
        flags = "q",
        min = 2,
        max = 3
    )
    @CommandPermissions({"worldedit.generation.sphere"})
    @Logging(PLACEMENT)
    public static void hsphere(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        if (args.hasFlag('q')) {
            Pattern block = we.getBlockPattern(player, args.getString(0));
            String[] radiuses = args.getString(1).split(",");
            if (radiuses.length > 1) {
                throw new InsufficientArgumentsException("Cannot specify q flag and multiple radiuses."); 
            }
            double radius = Double.parseDouble(radiuses[0]);
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
            return;
        }

        final Pattern block = we.getBlockPattern(player, args.getString(0));
        String[] radiuses = args.getString(1).split(",");
        final double radiusX, radiusY, radiusZ;
        switch (radiuses.length) {
        case 1:
            radiusX = radiusY = radiusZ = Math.max(1, Double.parseDouble(radiuses[0]));
            break;

        case 3:
            radiusX = Math.max(1, Double.parseDouble(radiuses[0]));
            radiusY = Math.max(1, Double.parseDouble(radiuses[1]));
            radiusZ = Math.max(1, Double.parseDouble(radiuses[2]));
            break;

        default:
            player.printError("You must either specify 1 or 3 radius values.");
            return;
        }
        final boolean raised;
        if (args.argsLength() > 2) {
            raised = args.getString(2).equalsIgnoreCase("true") || args.getString(2).equalsIgnoreCase("yes");
        } else {
            raised = false;
        }

        Vector pos = session.getPlacementPosition(player);
        if (raised) {
            pos = pos.add(0, radiusY, 0);
        }

        int affected = editSession.makeSphere(pos, block, radiusX, radiusY, radiusZ, false);
        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = {"/sphere"},
        usage = "<block> <radius>[,<radius>,<radius>] [raised?] ",
        desc = "Generate a filled sphere.",
        flags = "q",
        min = 2,
        max = 3
    )
    @CommandPermissions({"worldedit.generation.sphere"})
    @Logging(PLACEMENT)
    public static void sphere(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        if (args.hasFlag('q')) {
            Pattern block = we.getBlockPattern(player, args.getString(0));
            String[] radiuses = args.getString(1).split(",");
            if (radiuses.length > 1) {
                throw new InsufficientArgumentsException("Cannot specify q flag and multiple radiuses."); 
            }
            double radius = Double.parseDouble(radiuses[0]);
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
            return;
        }

        Pattern block = we.getBlockPattern(player, args.getString(0));
        String[] radiuses = args.getString(1).split(",");
        final double radiusX, radiusY, radiusZ;
        switch (radiuses.length) {
        case 1:
            radiusX = radiusY = radiusZ = Math.max(1, Double.parseDouble(radiuses[0]));
            break;

        case 3:
            radiusX = Math.max(1, Double.parseDouble(radiuses[0]));
            radiusY = Math.max(1, Double.parseDouble(radiuses[1]));
            radiusZ = Math.max(1, Double.parseDouble(radiuses[2]));
            break;

        default:
            player.printError("You must either specify 1 or 3 radius values.");
            return;
        }
        final boolean raised;
        if (args.argsLength() > 2) {
            raised = args.getString(2).equalsIgnoreCase("true") || args.getString(2).equalsIgnoreCase("yes");
        } else {
            raised = false;
        }

        Vector pos = session.getPlacementPosition(player);
        if (raised) {
            pos = pos.add(0, radiusY, 0);
        }

        int affected = editSession.makeSphere(pos, block, radiusX, radiusY, radiusZ, true);
        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = {"forestgen"},
        usage = "[size] [type] [density]",
        desc = "Generate a forest",
        min = 0,
        max = 3
    )
    @CommandPermissions({"worldedit.generation.forest"})
    @Logging(POSITION)
    public static void forestGen(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 10;
        TreeGenerator.TreeType type = args.argsLength() > 1 ?
                type = TreeGenerator.lookup(args.getString(1))
                : TreeGenerator.TreeType.TREE;
        double density = args.argsLength() > 2 ? args.getDouble(2) / 100 : 0.05;

        if (type == null) {
            player.printError("Tree type '" + args.getString(1) + "' is unknown.");
            return;
        }
        
        int affected = editSession.makeForest(player.getPosition(),
                size, density, new TreeGenerator(type));
        player.print(affected + " trees created.");
    }
    
    @Command(
        aliases = {"pumpkins"},
        usage = "[size]",
        desc = "Generate pumpkin patches",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.generation.pumpkins"})
    @Logging(POSITION)
    public static void pumpkins(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 10;

        int affected = editSession.makePumpkinPatches(player.getPosition(), size);
        player.print(affected + " pumpkin patches created.");
    }
    
    @Command(
        aliases = {"/pyramid"},
        usage = "<block> <range>",
        desc = "Generate a filled pyramid",
        min = 2,
        max = 2
    )
    @CommandPermissions({"worldedit.generation.pyramid"})
    @Logging(PLACEMENT)
    public static void pyramid(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        int size = Math.max(1, args.getInteger(1));
        Vector pos = session.getPlacementPosition(player);
        
        int affected = editSession.makePyramid(pos, block, size, true);
        
        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }
    
    @Command(
        aliases = {"/hpyramid"},
        usage = "<block> <range>",
        desc = "Generate a hollow pyramid",
        min = 2,
        max = 2
    )
    @CommandPermissions({"worldedit.generation.pyramid"})
    @Logging(PLACEMENT)
    public static void hpyramid(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        int size = Math.max(1, args.getInteger(1));
        Vector pos = session.getPlacementPosition(player);
        
        int affected = editSession.makePyramid(pos, block, size, false);
        
        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }
}
