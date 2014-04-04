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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.generator.FloraGenerator;
import com.sk89q.worldedit.function.generator.ForestGenerator;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.function.mask.NoiseFilter2D;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.math.noise.RandomNoise;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;
import static com.sk89q.worldedit.regions.Regions.*;

/**
 * Region related commands.
 *
 * @author sk89q
 */
public class RegionCommands {
    private final WorldEdit we;

    public RegionCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "/set" },
        usage = "<block>",
        desc = "Set all the blocks inside the selection to a block",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.region.set")
    @Logging(REGION)
    public void set(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern pattern = we.getBlockPattern(player, args.getString(0));

        int affected;

        if (pattern instanceof SingleBlockPattern) {
            affected = editSession.setBlocks(session.getSelection(player.getWorld()),
                    ((SingleBlockPattern) pattern).getBlock());
        } else {
            affected = editSession.setBlocks(session.getSelection(player.getWorld()), pattern);
        }

        player.print(affected + " block(s) have been changed.");
    }

    @Command(
            aliases = { "/line" },
            usage = "<block> [thickness]",
            desc = "Draws a line segment between cuboid selection corners",
            help =
                "Draws a line segment between cuboid selection corners.\n" +
                "Can only be used with cuboid selections.\n" +
                "Flags:\n" +
                "  -h generates only a shell",
            flags = "h",
            min = 1,
            max = 2
    )
    @CommandPermissions("worldedit.region.line")
    @Logging(REGION)
    public void line(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Region region = session.getSelection(session.getSelectionWorld());
        if (!(region instanceof CuboidRegion)) {
            player.printError("Invalid region type");
            return;
        }
        if (args.argsLength() < 2 ? false : args.getDouble(1) < 0) {
            player.printError("Invalid thickness. Must not be negative");
            return;
        }

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        CuboidRegion cuboidregion = (CuboidRegion) region;
        Vector pos1 = cuboidregion.getPos1();
        Vector pos2 = cuboidregion.getPos2();
        int blocksChanged = editSession.drawLine(pattern, pos1, pos2, args.argsLength() < 2 ? 0 : args.getDouble(1), !args.hasFlag('h'));

        player.print(blocksChanged + " block(s) have been changed.");
    }

    @Command(
            aliases = { "/curve" },
            usage = "<block> [thickness]",
            desc = "Draws a spline through selected points",
            help =
                "Draws a spline through selected points.\n" +
                "Can only be uesd with convex polyhedral selections.\n" +
                "Flags:\n" +
                "  -h generates only a shell",
            flags = "h",
            min = 1,
            max = 2
    )
    @CommandPermissions("worldedit.region.curve")
    @Logging(REGION)
    public void curve(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Region region = session.getSelection(session.getSelectionWorld());
        if (!(region instanceof ConvexPolyhedralRegion)) {
            player.printError("Invalid region type");
            return;
        }
        if (args.argsLength() < 2 ? false : args.getDouble(1) < 0) {
            player.printError("Invalid thickness. Must not be negative");
            return;
        }

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        ConvexPolyhedralRegion cpregion = (ConvexPolyhedralRegion) region;
        List<Vector> vectors = new ArrayList<Vector>(cpregion.getVertices());

        int blocksChanged = editSession.drawSpline(pattern, vectors, 0, 0, 0, 10, args.argsLength() < 2 ? 0 : args.getDouble(1), !args.hasFlag('h'));

        player.print(blocksChanged + " block(s) have been changed.");
    }

    @Command(
        aliases = { "/replace", "/re", "/rep" },
        usage = "[from-block] <to-block>",
        desc = "Replace all blocks in the selection with another",
        flags = "f",
        min = 1,
        max = 2
    )
    @CommandPermissions("worldedit.region.replace")
    @Logging(REGION)
    public void replace(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Set<BaseBlock> from;
        Pattern to;
        if (args.argsLength() == 1) {
            from = null;
            to = we.getBlockPattern(player, args.getString(0));
        } else {
            from = we.getBlocks(player, args.getString(0), true, !args.hasFlag('f'));
            to = we.getBlockPattern(player, args.getString(1));
        }

        final int affected;
        if (to instanceof SingleBlockPattern) {
            affected = editSession.replaceBlocks(session.getSelection(player.getWorld()), from,
                    ((SingleBlockPattern) to).getBlock());
        } else {
            affected = editSession.replaceBlocks(session.getSelection(player.getWorld()), from, to);
        }

        player.print(affected + " block(s) have been replaced.");
    }

    @Command(
        aliases = { "/overlay" },
        usage = "<block>",
        desc = "Set a block on top of blocks in the region",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.region.overlay")
    @Logging(REGION)
    public void overlay(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern pat = we.getBlockPattern(player, args.getString(0));

        Region region = session.getSelection(player.getWorld());
        int affected = 0;
        if (pat instanceof SingleBlockPattern) {
            affected = editSession.overlayCuboidBlocks(region,
                    ((SingleBlockPattern) pat).getBlock());
        } else {
            affected = editSession.overlayCuboidBlocks(region, pat);
        }
        player.print(affected + " block(s) have been overlayed.");
    }

    @Command(
        aliases = { "/center", "/middle" },
        usage = "<block>",
        desc = "Set the center block(s)",
        min = 1,
        max = 1
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.region.center")
    public void center(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        Region region = session.getSelection(player.getWorld());

        int affected = editSession.center(region, pattern);
        player.print("Center set ("+ affected + " blocks changed)");
    }

    @Command(
        aliases = { "/naturalize" },
        usage = "",
        desc = "3 layers of dirt on top then rock below",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.region.naturalize")
    @Logging(REGION)
    public void naturalize(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Region region = session.getSelection(player.getWorld());
        int affected = editSession.naturalizeCuboidBlocks(region);
        player.print(affected + " block(s) have been naturalized.");
    }

    @Command(
        aliases = { "/walls" },
        usage = "<block>",
        desc = "Build the four sides of the selection",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.region.walls")
    @Logging(REGION)
    public void walls(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final Pattern pattern = we.getBlockPattern(player, args.getString(0));
        final int affected;
        final Region region = session.getSelection(player.getWorld());
        if (!(region instanceof CuboidRegion)) {
            affected = editSession.makeWalls(region, pattern);
        } else if (pattern instanceof SingleBlockPattern) {
            affected = editSession.makeCuboidWalls(region, ((SingleBlockPattern) pattern).getBlock());
        } else {
            affected = editSession.makeCuboidWalls(region, pattern);
        }

        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = { "/faces", "/outline" },
        usage = "<block>",
        desc = "Build the walls, ceiling, and floor of a selection",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.region.faces")
    @Logging(REGION)
    public void faces(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final Pattern pattern = we.getBlockPattern(player, args.getString(0));
        final int affected;
        final Region region = session.getSelection(player.getWorld());
        if (!(region instanceof CuboidRegion)) {
            affected = editSession.makeFaces(region, pattern);
        } else if (pattern instanceof SingleBlockPattern) {
            affected = editSession.makeCuboidFaces(region, ((SingleBlockPattern) pattern).getBlock());
        } else {
            affected = editSession.makeCuboidFaces(region, pattern);
        }

        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = { "/smooth" },
        usage = "[iterations]",
        flags = "n",
        desc = "Smooth the elevation in the selection",
        help =
            "Smooths the elevation in the selection.\n" +
            "The -n flag makes it only consider naturally occuring blocks.",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.region.smooth")
    @Logging(REGION)
    public void smooth(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        int iterations = 1;
        if (args.argsLength() > 0) {
            iterations = args.getInteger(0);
        }

        HeightMap heightMap = new HeightMap(editSession, session.getSelection(player.getWorld()), args.hasFlag('n'));
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        int affected = heightMap.applyFilter(filter, iterations);
        player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");

    }

    @Command(
        aliases = { "/move" },
        usage = "[count] [direction] [leave-id]",
        flags = "s",
        desc = "Move the contents of the selection",
        help =
            "Moves the contents of the selection.\n" +
            "The -s flag shifts the selection to the target location.\n" +
            "Optionally fills the old location with <leave-id>.",
        min = 0,
        max = 3
    )
    @CommandPermissions("worldedit.region.move")
    @Logging(ORIENTATION_REGION)
    public void move(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        Vector dir = we.getDirection(player,
                args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");
        BaseBlock replace;

        // Replacement block argument
        if (args.argsLength() > 2) {
            replace = we.getBlock(player, args.getString(2));
        } else {
            replace = new BaseBlock(BlockID.AIR);
        }

        int affected = editSession.moveRegion(session.getSelection(player.getWorld()),
                dir, count, true, replace);

        if (args.hasFlag('s')) {
            try {
                Region region = session.getSelection(player.getWorld());
                region.shift(dir.multiply(count));

                session.getRegionSelector(player.getWorld()).learnChanges();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " blocks moved.");
    }

    @Command(
        aliases = { "/stack" },
        usage = "[count] [direction]",
        flags = "sa",
        desc = "Repeat the contents of the selection",
        help =
            "Repeats the contents of the selection.\n" +
            "Flags:\n" +
            "  -s shifts the selection to the last stacked copy\n" +
            "  -a skips air blocks",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.region.stack")
    @Logging(ORIENTATION_REGION)
    public void stack(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        Vector dir = we.getDiagonalDirection(player,
                args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");

        int affected = editSession.stackCuboidRegion(session.getSelection(player.getWorld()),
                dir, count, !args.hasFlag('a'));

        if (args.hasFlag('s')) {
            try {
                final Region region = session.getSelection(player.getWorld());
                final Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint());

                final Vector shiftVector = dir.multiply(count * (Math.abs(dir.dot(size)) + 1));
                region.shift(shiftVector);

                session.getRegionSelector(player.getWorld()).learnChanges();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " blocks changed. Undo with //undo");
    }

    @Command(
        aliases = { "/regen" },
        usage = "",
        desc = "Regenerates the contents of the selection",
        help =
            "Regenerates the contents of the current selection.\n" +
            "This command might affect things outside the selection,\n" +
            "if they are within the same chunk.",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.regen")
    @Logging(REGION)
    public void regenerateChunk(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Region region = session.getSelection(player.getWorld());
        Mask mask = session.getMask();
        session.setMask(null);
        player.getWorld().regenerate(region, editSession);
        session.setMask(mask);
        player.print("Region regenerated.");
    }

    @Command(
            aliases = { "/deform" },
            usage = "<expression>",
            desc = "Deforms a selected region with an expression",
            help =
                "Deforms a selected region with an expression\n" +
                "The expression is executed for each block and is expected\n" +
                "to modify the variables x, y and z to point to a new block\n" +
                "to fetch. See also tinyurl.com/wesyntax.",
            flags = "ro",
            min = 1,
            max = -1
    )
    @CommandPermissions("worldedit.region.deform")
    @Logging(ALL)
    public void deform(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final Region region = session.getSelection(player.getWorld());

        final String expression = args.getJoinedStrings(0);

        final Vector zero;
        Vector unit;

        if (args.hasFlag('r')) {
            zero = Vector.ZERO;
            unit = Vector.ONE;
        } else if (args.hasFlag('o')) {
            zero = session.getPlacementPosition(player);
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
            final int affected = editSession.deformRegion(region, zero, unit, expression);
            player.findFreePosition();
            player.print(affected + " block(s) have been deformed.");
        } catch (ExpressionException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = { "/hollow" },
        usage = "[<thickness>[ <block>]]",
        desc = "Hollows out the object contained in this selection",
        help =
            "Hollows out the object contained in this selection.\n" +
            "Optionally fills the hollowed out part with the given block.\n" +
            "Thickness is measured in manhattan distance.",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.region.hollow")
    @Logging(REGION)
    public void hollow(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final int thickness = args.argsLength() >= 1 ? Math.max(1, args.getInteger(0)) : 1;
        final Pattern pattern = args.argsLength() >= 2 ? we.getBlockPattern(player, args.getString(1)) : new SingleBlockPattern(new BaseBlock(BlockID.AIR));

        final int affected = editSession.hollowOutRegion(session.getSelection(player.getWorld()), thickness, pattern);

        player.print(affected + " block(s) have been changed.");
    }

    @Command(
            aliases = { "/forest" },
            usage = "[type] [density]",
            desc = "Make a forest within the region",
            min = 0,
            max = 2
    )
    @CommandPermissions("worldedit.region.forest")
    @Logging(REGION)
    public void forest(CommandContext args, LocalSession session, LocalPlayer player,
                           EditSession editSession) throws WorldEditException {
        TreeGenerator.TreeType type = args.argsLength() > 0 ? TreeGenerator.lookup(args.getString(0)) : TreeGenerator.TreeType.TREE;
        double density = args.argsLength() > 1 ? args.getDouble(1) / 100 : 0.05;

        if (type == null) {
            player.printError("Tree type '" + args.getString(0) + "' is unknown.");
            return;
        }

        Region region = session.getSelection(player.getWorld());

        ForestGenerator generator = new ForestGenerator(editSession, new TreeGenerator(type));
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(editSession), generator);
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
        Operations.completeLegacy(visitor);

        player.print(ground.getAffected() + " trees created.");
    }

    @Command(
            aliases = { "/flora" },
            usage = "[density]",
            desc = "Make flora within the region",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.region.flora")
    @Logging(REGION)
    public void flora(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession) throws WorldEditException {
        double density = args.argsLength() > 0 ? args.getDouble(0) / 100 : 0.1;

        Region region = session.getSelection(player.getWorld());
        FloraGenerator generator = new FloraGenerator(editSession);
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(editSession), generator);
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
        Operations.completeLegacy(visitor);

        player.print(ground.getAffected() + " flora created.");
    }

}
