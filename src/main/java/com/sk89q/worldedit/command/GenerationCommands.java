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

package com.sk89q.worldedit.command;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.ALL;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.PLACEMENT;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.POSITION;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator;

/**
 * Generation commands.
 * 
 * @author sk89q
 */
public class GenerationCommands {
    private final WorldEdit we;
    
    public GenerationCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "/hcyl" },
        usage = "<block> <radius>[,<radius>] [height]",
        desc = "Generates a hollow cylinder.",
        help =
            "Generates a hollow cylinder.\n" +
            "By specifying 2 radii, separated by a comma,\n" +
            "you can generate elliptical cylinders.\n" +
            "The 1st radius is north/south, the 2nd radius is east/west.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.cylinder")
    @Logging(PLACEMENT)
    public void hcyl(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        String[] radiuses = args.getString(1).split(",");
        final double radiusX, radiusZ;
        switch (radiuses.length) {
        case 1:
            radiusX = radiusZ = Math.max(1, Double.parseDouble(radiuses[0]));
            break;

        case 2:
            radiusX = Math.max(1, Double.parseDouble(radiuses[0]));
            radiusZ = Math.max(1, Double.parseDouble(radiuses[1]));
            break;

        default:
            player.printError("You must either specify 1 or 2 radius values.");
            return;
        }
        int height = args.argsLength() > 2 ? args.getInteger(2) : 1;

        we.checkMaxRadius(radiusX);
        we.checkMaxRadius(radiusZ);
        we.checkMaxRadius(height);

        Vector pos = session.getPlacementPosition(player);
        int affected = editSession.makeCylinder(pos, block, radiusX, radiusZ, height, false);
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = { "/cyl" },
        usage = "<block> <radius>[,<radius>] [height]",
        desc = "Generates a cylinder.",
        help =
            "Generates a cylinder.\n" +
            "By specifying 2 radii, separated by a comma,\n" +
            "you can generate elliptical cylinders.\n" +
            "The 1st radius is north/south, the 2nd radius is east/west.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.cylinder")
    @Logging(PLACEMENT)
    public void cyl(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        String[] radiuses = args.getString(1).split(",");
        final double radiusX, radiusZ;
        switch (radiuses.length) {
        case 1:
            radiusX = radiusZ = Math.max(1, Double.parseDouble(radiuses[0]));
            break;

        case 2:
            radiusX = Math.max(1, Double.parseDouble(radiuses[0]));
            radiusZ = Math.max(1, Double.parseDouble(radiuses[1]));
            break;

        default:
            player.printError("You must either specify 1 or 2 radius values.");
            return;
        }
        int height = args.argsLength() > 2 ? args.getInteger(2) : 1;

        we.checkMaxRadius(radiusX);
        we.checkMaxRadius(radiusZ);
        we.checkMaxRadius(height);

        Vector pos = session.getPlacementPosition(player);
        int affected = editSession.makeCylinder(pos, block, radiusX, radiusZ, height, true);
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = { "/hsphere" },
        usage = "<block> <radius>[,<radius>,<radius>] [raised?]",
        desc = "Generates a hollow sphere.",
        help =
            "Generates a hollow sphere.\n" +
            "By specifying 3 radii, separated by commas,\n" +
            "you can generate an ellipsoid. The order of the ellipsoid radii\n" +
            "is north/south, up/down, east/west.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.sphere")
    @Logging(PLACEMENT)
    public void hsphere(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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

        we.checkMaxRadius(radiusX);
        we.checkMaxRadius(radiusY);
        we.checkMaxRadius(radiusZ);

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
        aliases = { "/sphere" },
        usage = "<block> <radius>[,<radius>,<radius>] [raised?]",
        desc = "Generates a filled sphere.",
        help =
            "Generates a filled sphere.\n" +
            "By specifying 3 radii, separated by commas,\n" +
            "you can generate an ellipsoid. The order of the ellipsoid radii\n" +
            "is north/south, up/down, east/west.",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.generation.sphere")
    @Logging(PLACEMENT)
    public void sphere(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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

        we.checkMaxRadius(radiusX);
        we.checkMaxRadius(radiusY);
        we.checkMaxRadius(radiusZ);

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
        aliases = { "forestgen" },
        usage = "[size] [type] [density]",
        desc = "Generate a forest",
        min = 0,
        max = 3
    )
    @CommandPermissions("worldedit.generation.forest")
    @Logging(POSITION)
    @SuppressWarnings("deprecation")
    public void forestGen(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 10;
        TreeGenerator.TreeType type = args.argsLength() > 1 ?
                TreeGenerator.lookup(args.getString(1))
                : TreeGenerator.TreeType.TREE;
        double density = args.argsLength() > 2 ? args.getDouble(2) / 100 : 0.05;

        if (type == null) {
            player.printError("Tree type '" + args.getString(1) + "' is unknown.");
            return;
        }

        int affected = editSession.makeForest(session.getPlacementPosition(player),
                size, density, new TreeGenerator(type));
        player.print(affected + " trees created.");
    }

    @Command(
        aliases = { "pumpkins" },
        usage = "[size]",
        desc = "Generate pumpkin patches",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.generation.pumpkins")
    @Logging(POSITION)
    public void pumpkins(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 10;

        int affected = editSession.makePumpkinPatches(session.getPlacementPosition(player), size);
        player.print(affected + " pumpkin patches created.");
    }

    @Command(
        aliases = { "/pyramid" },
        usage = "<block> <size>",
        desc = "Generate a filled pyramid",
        min = 2,
        max = 2
    )
    @CommandPermissions("worldedit.generation.pyramid")
    @Logging(PLACEMENT)
    public void pyramid(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        int size = Math.max(1, args.getInteger(1));
        Vector pos = session.getPlacementPosition(player);

        we.checkMaxRadius(size);

        int affected = editSession.makePyramid(pos, block, size, true);

        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = { "/hpyramid" },
        usage = "<block> <size>",
        desc = "Generate a hollow pyramid",
        min = 2,
        max = 2
    )
    @CommandPermissions("worldedit.generation.pyramid")
    @Logging(PLACEMENT)
    public void hpyramid(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern block = we.getBlockPattern(player, args.getString(0));
        int size = Math.max(1, args.getInteger(1));
        Vector pos = session.getPlacementPosition(player);

        we.checkMaxRadius(size);

        int affected = editSession.makePyramid(pos, block, size, false);

        player.findFreePosition();
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = { "/generate", "/gen", "/g" },
        usage = "<block> <expression>",
        desc = "Generates a shape according to a formula.",
        help =
            "Generates a shape according to a formula that is expected to\n" +
            "return positive numbers (true) if the point is inside the shape\n" +
            "Optionally set type/data to the desired block.\n" +
            "Flags:\n" +
            "  -h to generate a hollow shape\n" +
            "  -r to use raw minecraft coordinates\n" +
            "  -o is like -r, except offset from placement.\n" +
            "  -c is like -r, except offset selection center.\n" +
            "If neither -r nor -o is given, the selection is mapped to -1..1\n" +
            "See also tinyurl.com/wesyntax.",
        flags = "hroc",
        min = 2,
        max = -1
    )
    @CommandPermissions("worldedit.generation.shape")
    @Logging(ALL)
    public void generate(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final Pattern pattern = we.getBlockPattern(player, args.getString(0));
        final Region region = session.getSelection(player.getWorld());

        final boolean hollow = args.hasFlag('h');

        final String expression = args.getJoinedStrings(1);

        final Vector zero;
        Vector unit;

        if (args.hasFlag('r')) {
            zero = Vector.ZERO;
            unit = Vector.ONE;
        } else if (args.hasFlag('o')) {
            zero = session.getPlacementPosition(player);
            unit = Vector.ONE;
        } else if (args.hasFlag('c')) {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = Vector.ONE;
        } else {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = max.subtract(zero);

            if (unit.getX() == 0) unit = unit.setX(1.0);
            if (unit.getY() == 0) unit = unit.setY(1.0);
            if (unit.getZ() == 0) unit = unit.setZ(1.0);
        }

        try {
            final int affected = editSession.makeShape(region, zero, unit, pattern, expression, hollow);
            player.findFreePosition();
            player.print(affected + " block(s) have been created.");
        } catch (ExpressionException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = { "/generatebiome", "/genbiome", "/gb" },
        usage = "<block> <expression>",
        desc = "Sets biome according to a formula.",
        help =
            "Generates a shape according to a formula that is expected to\n" +
            "return positive numbers (true) if the point is inside the shape\n" +
            "Optionally set type/data to the desired block.\n" +
            "Flags:\n" +
            "  -h to generate a hollow shape\n" +
            "  -r to use raw minecraft coordinates\n" +
            "  -o is like -r, except offset from placement.\n" +
            "  -c is like -r, except offset selection center.\n" +
            "If neither -r nor -o is given, the selection is mapped to -1..1\n" +
            "See also tinyurl.com/wesyntax.",
        flags = "hroc",
        min = 2,
        max = -1
    )
    @CommandPermissions({"worldedit.generation.shape", "worldedit.biome.set"})
    @Logging(ALL)
    public void generateBiome(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final BiomeType target = we.getServer().getBiomes().get(args.getString(0));
        final Region region = session.getSelection(player.getWorld());

        final boolean hollow = args.hasFlag('h');

        final String expression = args.getJoinedStrings(1);

        final Vector zero;
        Vector unit;

        if (args.hasFlag('r')) {
            zero = Vector.ZERO;
            unit = Vector.ONE;
        } else if (args.hasFlag('o')) {
            zero = session.getPlacementPosition(player);
            unit = Vector.ONE;
        } else if (args.hasFlag('c')) {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = Vector.ONE;
        } else {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = max.subtract(zero);

            if (unit.getX() == 0) unit = unit.setX(1.0);
            if (unit.getY() == 0) unit = unit.setY(1.0);
            if (unit.getZ() == 0) unit = unit.setZ(1.0);
        }

        try {
            final int affected = editSession.makeBiomeShape(region, zero, unit, target, expression, hollow);
            player.findFreePosition();
            player.print("Biome changed to " + target.getName() + ". " + affected + " columns affected.");
        } catch (ExpressionException e) {
            player.printError(e.getMessage());
        }
    }
}
