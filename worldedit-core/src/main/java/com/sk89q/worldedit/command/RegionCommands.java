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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.generator.FloraGenerator;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.NoiseFilter2D;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

import java.util.ArrayList;
import java.util.List;

import static com.sk89q.worldedit.command.util.Logging.LogMode.ALL;
import static com.sk89q.worldedit.command.util.Logging.LogMode.ORIENTATION_REGION;
import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;
import static com.sk89q.worldedit.internal.command.CommandUtil.checkCommandArgument;
import static com.sk89q.worldedit.regions.Regions.asFlatRegion;
import static com.sk89q.worldedit.regions.Regions.maximumBlockY;
import static com.sk89q.worldedit.regions.Regions.minimumBlockY;

/**
 * Commands that operate on regions.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class RegionCommands {

    /**
     * Create a new instance.
     */
    public RegionCommands() {
    }

    @Command(
        name = "/set",
        desc = "Sets all the blocks in the region"
    )
    @CommandPermissions("worldedit.region.set")
    @Logging(REGION)
    public int set(Player player, EditSession editSession,
                   @Selection Region region,
                   @Arg(desc = "The pattern of blocks to set")
                       Pattern pattern) {
        RegionFunction set = new BlockReplace(editSession, pattern);
        RegionVisitor visitor = new RegionVisitor(region, set);

        Operations.completeBlindly(visitor);
        List<String> messages = Lists.newArrayList();
        visitor.addStatusMessages(messages);
        if (messages.isEmpty()) {
            player.print("Operation completed.");
        } else {
            player.print("Operation completed (" + Joiner.on(", ").join(messages) + ").");
        }

        return visitor.getAffected();
    }

    @Command(
        name = "/line",
        desc = "Draws a line segment between cuboid selection corners",
        descFooter = "Can only be used with a cuboid selection"
    )
    @CommandPermissions("worldedit.region.line")
    @Logging(REGION)
    public int line(Player player, EditSession editSession,
                    @Selection Region region,
                    @Arg(desc = "The pattern of blocks to place")
                        Pattern pattern,
                    @Arg(desc = "The thickness of the line", def = "0")
                        int thickness,
                    @Switch(name = 'h', desc = "Generate only a shell")
                        boolean shell) throws WorldEditException {
        if (!(region instanceof CuboidRegion)) {
            player.printError("//line only works with cuboid selections");
            return 0;
        }
        checkCommandArgument(thickness >= 0, "Thickness must be >= 0");

        CuboidRegion cuboidregion = (CuboidRegion) region;
        BlockVector3 pos1 = cuboidregion.getPos1();
        BlockVector3 pos2 = cuboidregion.getPos2();
        int blocksChanged = editSession.drawLine(pattern, pos1, pos2, thickness, !shell);

        player.print(blocksChanged + " block(s) have been changed.");
        return blocksChanged;
    }

    @Command(
        name = "/curve",
        desc = "Draws a spline through selected points",
        descFooter = "Can only be used with a convex polyhedral selection"
    )
    @CommandPermissions("worldedit.region.curve")
    @Logging(REGION)
    public int curve(Player player, EditSession editSession,
                     @Selection Region region,
                     @Arg(desc = "The pattern of blocks to place")
                         Pattern pattern,
                     @Arg(desc = "The thickness of the curve", def = "0")
                         int thickness,
                     @Switch(name = 'h', desc = "Generate only a shell")
                         boolean shell) throws WorldEditException {
        if (!(region instanceof ConvexPolyhedralRegion)) {
            player.printError("//curve only works with convex polyhedral selections");
            return 0;
        }
        checkCommandArgument(thickness >= 0, "Thickness must be >= 0");

        ConvexPolyhedralRegion cpregion = (ConvexPolyhedralRegion) region;
        List<BlockVector3> vectors = new ArrayList<>(cpregion.getVertices());

        int blocksChanged = editSession.drawSpline(pattern, vectors, 0, 0, 0, 10, thickness, !shell);

        player.print(blocksChanged + " block(s) have been changed.");
        return blocksChanged;
    }

    @Command(
        name = "/replace",
        aliases = { "/re", "/rep" },
        desc = "Replace all blocks in the selection with another"
    )
    @CommandPermissions("worldedit.region.replace")
    @Logging(REGION)
    public int replace(Player player, EditSession editSession, @Selection Region region,
                       @Arg(desc = "The mask representing blocks to replace", def = "")
                           Mask from,
                       @Arg(desc = "The pattern of blocks to replace with")
                           Pattern to) throws WorldEditException {
        if (from == null) {
            from = new ExistingBlockMask(editSession);
        }
        int affected = editSession.replaceBlocks(region, from, to);
        player.print(affected + " block(s) have been replaced.");
        return affected;
    }

    @Command(
        name = "/overlay",
        desc = "Set a block on top of blocks in the region"
    )
    @CommandPermissions("worldedit.region.overlay")
    @Logging(REGION)
    public int overlay(Player player, EditSession editSession, @Selection Region region,
                       @Arg(desc = "The pattern of blocks to overlay")
                           Pattern pattern) throws WorldEditException {
        int affected = editSession.overlayCuboidBlocks(region, pattern);
        player.print(affected + " block(s) have been overlaid.");
        return affected;
    }

    @Command(
        name = "/center",
        aliases = { "/middle" },
        desc = "Set the center block(s)"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.region.center")
    public int center(Player player, EditSession editSession, @Selection Region region,
                      @Arg(desc = "The pattern of blocks to set")
                          Pattern pattern) throws WorldEditException {
        int affected = editSession.center(region, pattern);
        player.print("Center set (" + affected + " block(s) changed)");
        return affected;
    }

    @Command(
        name = "/naturalize",
        desc = "3 layers of dirt on top then rock below"
    )
    @CommandPermissions("worldedit.region.naturalize")
    @Logging(REGION)
    public int naturalize(Player player, EditSession editSession, @Selection Region region) throws WorldEditException {
        int affected = editSession.naturalizeCuboidBlocks(region);
        player.print(affected + " block(s) have been made to look more natural.");
        return affected;
    }

    @Command(
        name = "/walls",
        desc = "Build the four sides of the selection"
    )
    @CommandPermissions("worldedit.region.walls")
    @Logging(REGION)
    public int walls(Player player, EditSession editSession, @Selection Region region,
                     @Arg(desc = "The pattern of blocks to set")
                         Pattern pattern) throws WorldEditException {
        int affected = editSession.makeWalls(region, pattern);
        player.print(affected + " block(s) have been changed.");
        return affected;
    }

    @Command(
        name = "/faces",
        aliases = { "/outline" },
        desc = "Build the walls, ceiling, and floor of a selection"
    )
    @CommandPermissions("worldedit.region.faces")
    @Logging(REGION)
    public int faces(Player player, EditSession editSession, @Selection Region region,
                     @Arg(desc = "The pattern of blocks to set")
                         Pattern pattern) throws WorldEditException {
        int affected = editSession.makeCuboidFaces(region, pattern);
        player.print(affected + " block(s) have been changed.");
        return affected;
    }

    @Command(
        name = "/smooth",
        desc = "Smooth the elevation in the selection",
        descFooter = "Example: '//smooth 1 grass_block,dirt,stone' would only smooth natural surface terrain."
    )
    @CommandPermissions("worldedit.region.smooth")
    @Logging(REGION)
    public int smooth(Player player, EditSession editSession, @Selection Region region,
                      @Arg(desc = "# of iterations to perform", def = "1")
                          int iterations,
                      @Arg(desc = "The mask of blocks to use as the height map", def = "")
                          Mask mask) throws WorldEditException {
        HeightMap heightMap = new HeightMap(editSession, region, mask);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        int affected = heightMap.applyFilter(filter, iterations);
        player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");
        return affected;
    }

    @Command(
        name = "/move",
        desc = "Move the contents of the selection"
    )
    @CommandPermissions("worldedit.region.move")
    @Logging(ORIENTATION_REGION)
    public int move(Player player, EditSession editSession, LocalSession session,
                    @Selection Region region,
                    @Arg(desc = "# of blocks to move", def = "1")
                        int count,
                    @Arg(desc = "The direction to move", def = Direction.AIM)
                    @Direction(includeDiagonals = true)
                        BlockVector3 direction,
                    @Arg(desc = "The pattern of blocks to leave", def = "air")
                        Pattern replace,
                    @Switch(name = 's', desc = "Shift the selection to the target location")
                        boolean moveSelection,
                    @Switch(name = 'a', desc = "Ignore air blocks")
                        boolean ignoreAirBlocks) throws WorldEditException {
        checkCommandArgument(count >= 1, "Count must be >= 1");

        int affected = editSession.moveRegion(region, direction, count, !ignoreAirBlocks, replace);

        if (moveSelection) {
            try {
                region.shift(direction.multiply(count));

                session.getRegionSelector(player.getWorld()).learnChanges();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " block(s) moved.");
        return affected;
    }

    @Command(
        name = "/stack",
        desc = "Repeat the contents of the selection"
    )
    @CommandPermissions("worldedit.region.stack")
    @Logging(ORIENTATION_REGION)
    public int stack(Player player, EditSession editSession, LocalSession session,
                     @Selection Region region,
                     @Arg(desc = "# of copies to stack", def = "1")
                         int count,
                     @Arg(desc = "The direction to stack", def = Direction.AIM)
                     @Direction(includeDiagonals = true)
                         BlockVector3 direction,
                     @Switch(name = 's', desc = "Shift the selection to the last stacked copy")
                         boolean moveSelection,
                     @Switch(name = 'a', desc = "Ignore air blocks")
                         boolean ignoreAirBlocks) throws WorldEditException {
        int affected = editSession.stackCuboidRegion(region, direction, count, !ignoreAirBlocks);

        if (moveSelection) {
            try {
                final BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint());

                final BlockVector3 shiftVector = direction.toVector3().multiply(count * (Math.abs(direction.dot(size)) + 1)).toBlockPoint();
                region.shift(shiftVector);

                session.getRegionSelector(player.getWorld()).learnChanges();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " block(s) changed. Undo with //undo");
        return affected;
    }

    @Command(
        name = "/regen",
        desc = "Regenerates the contents of the selection",
        descFooter = "This command might affect things outside the selection,\n" +
            "if they are within the same chunk."
    )
    @CommandPermissions("worldedit.regen")
    @Logging(REGION)
    public void regenerateChunk(Player player, LocalSession session, EditSession editSession, @Selection Region region) throws WorldEditException {
        Mask mask = session.getMask();
        try {
            session.setMask(null);
            player.getWorld().regenerate(region, editSession);
        } finally {
            session.setMask(mask);
        }
        player.print("Region regenerated.");
    }

    @Command(
        name = "/deform",
        desc = "Deforms a selected region with an expression",
        descFooter = "The expression is executed for each block and is expected\n" +
            "to modify the variables x, y and z to point to a new block\n" +
            "to fetch. See also tinyurl.com/wesyntax."
    )
    @CommandPermissions("worldedit.region.deform")
    @Logging(ALL)
    public int deform(Player player, LocalSession session, EditSession editSession,
                      @Selection Region region,
                      @Arg(desc = "The expression to use", variable = true)
                          List<String> expression,
                      @Switch(name = 'r', desc = "Use the game's coordinate origin")
                          boolean useRawCoords,
                      @Switch(name = 'o', desc = "Use the selection's center as origin")
                          boolean offset) throws WorldEditException {
        final Vector3 zero;
        Vector3 unit;

        if (useRawCoords) {
            zero = Vector3.ZERO;
            unit = Vector3.ONE;
        } else if (offset) {
            zero = session.getPlacementPosition(player).toVector3();
            unit = Vector3.ONE;
        } else {
            final Vector3 min = region.getMinimumPoint().toVector3();
            final Vector3 max = region.getMaximumPoint().toVector3();

            zero = max.add(min).divide(2);
            unit = max.subtract(zero);

            if (unit.getX() == 0) unit = unit.withX(1.0);
            if (unit.getY() == 0) unit = unit.withY(1.0);
            if (unit.getZ() == 0) unit = unit.withZ(1.0);
        }

        try {
            final int affected = editSession.deformRegion(region, zero, unit, String.join(" ", expression), session.getTimeout());
            player.findFreePosition();
            player.print(affected + " block(s) have been deformed.");
            return affected;
        } catch (ExpressionException e) {
            player.printError(e.getMessage());
            return 0;
        }
    }

    @Command(
        name = "/hollow",
        desc = "Hollows out the object contained in this selection",
        descFooter = "Thickness is measured in manhattan distance."
    )
    @CommandPermissions("worldedit.region.hollow")
    @Logging(REGION)
    public int hollow(Player player, EditSession editSession,
                      @Selection Region region,
                      @Arg(desc = "Thickness of the shell to leave", def = "0")
                          int thickness,
                      @Arg(desc = "The pattern of blocks to replace the hollowed area with", def = "air")
                          Pattern pattern) throws WorldEditException {
        checkCommandArgument(thickness >= 0, "Thickness must be >= 0");

        int affected = editSession.hollowOutRegion(region, thickness, pattern);
        player.print(affected + " block(s) have been changed.");
        return affected;
    }

    @Command(
        name = "/forest",
        desc = "Make a forest within the region"
    )
    @CommandPermissions("worldedit.region.forest")
    @Logging(REGION)
    public int forest(Player player, EditSession editSession, @Selection Region region,
                      @Arg(desc = "The type of tree to place", def = "tree")
                          TreeType type,
                      @Arg(desc = "The density of the forest", def = "5")
                          double density) throws WorldEditException {
        checkCommandArgument(0 <= density && density <= 100, "Density must be in [0, 100]");
        int affected = editSession.makeForest(region, density / 100, type);
        player.print(affected + " trees created.");
        return affected;
    }

    @Command(
        name = "/flora",
        desc = "Make flora within the region"
    )
    @CommandPermissions("worldedit.region.flora")
    @Logging(REGION)
    public int flora(Player player, EditSession editSession, @Selection Region region,
                     @Arg(desc = "The density of the forest", def = "5")
                         double density) throws WorldEditException {
        checkCommandArgument(0 <= density && density <= 100, "Density must be in [0, 100]");
        density = density / 100;
        FloraGenerator generator = new FloraGenerator(editSession);
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(editSession), generator);
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
        Operations.completeLegacy(visitor);

        int affected = ground.getAffected();
        player.print(affected + " flora created.");
        return affected;
    }

}
