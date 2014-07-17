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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.command.functions.AbstractNotifier;
import com.sk89q.worldedit.command.functions.BlocksChangedNotifier;
import com.sk89q.worldedit.command.functions.CommandFutureUtils;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.CommonOperationFactory;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.generator.FloraGenerator;
import com.sk89q.worldedit.function.generator.ForestGenerator;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.NoiseFilter2D;
import com.sk89q.worldedit.function.operation.CountDelegatedOperation;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationFuture;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.Patterns;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.util.command.binding.Range;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.binding.Text;
import com.sk89q.worldedit.util.command.parametric.Optional;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;
import static com.sk89q.worldedit.regions.Regions.*;

/**
 * Commands that operate on regions.
 */
public class RegionCommands {

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public RegionCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
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
    public void set(Player player, EditSession editSession, @Selection Region region, Pattern pattern) throws WorldEditException {
        CommandFutureUtils.withChangedBlocksMessage(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.setBlocks(editSession, region, pattern)));
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
    public void line(Player player, EditSession editSession,
                     @Selection Region region,
                     Pattern pattern,
                     @Optional("0") @Range(min = 0) int thickness,
                     @Switch('h') boolean shell) throws WorldEditException {

        if (!(region instanceof CuboidRegion)) {
            player.printError("//line only works with cuboid selections");
            return;
        }

        CuboidRegion cuboidregion = (CuboidRegion) region;
        Vector pos1 = cuboidregion.getPos1();
        Vector pos2 = cuboidregion.getPos2();
        int blocksChanged = editSession.drawLine(Patterns.wrap(pattern), pos1, pos2, thickness, !shell);

        player.print(blocksChanged + " block(s) have been changed.");
    }

    @Command(
            aliases = { "/curve" },
            usage = "<block> [thickness]",
            desc = "Draws a spline through selected points",
            help =
                "Draws a spline through selected points.\n" +
                "Can only be used with convex polyhedral selections.\n" +
                "Flags:\n" +
                "  -h generates only a shell",
            flags = "h",
            min = 1,
            max = 2
    )
    @CommandPermissions("worldedit.region.curve")
    @Logging(REGION)
    public void curve(Player player, EditSession editSession,
                      @Selection Region region,
                      Pattern pattern,
                      @Optional("0") @Range(min = 0) int thickness,
                      @Switch('h') boolean shell) throws WorldEditException {
        if (!(region instanceof ConvexPolyhedralRegion)) {
            player.printError("//line only works with convex polyhedral selections");
            return;
        }

        ConvexPolyhedralRegion cpregion = (ConvexPolyhedralRegion) region;
        List<Vector> vectors = new ArrayList<Vector>(cpregion.getVertices());

        int blocksChanged = editSession.drawSpline(Patterns.wrap(pattern), vectors, 0, 0, 0, 10, thickness, !shell);

        player.print(blocksChanged + " block(s) have been changed.");
    }

    @Command(
        aliases = { "/replace", "/re", "/rep" },
        usage = "[from-block] <to-block>",
        desc = "Replace all blocks in the selection with another",
        min = 1,
        max = 2
    )
    @CommandPermissions("worldedit.region.replace")
    @Logging(REGION)
    public void replace(Player player, EditSession editSession,
                        @Selection Region region,
                        @Optional Mask from,
                        Pattern to) throws WorldEditException {
        if (from == null) {
            from = new ExistingBlockMask(editSession);
        }

        CommandFutureUtils.withChangedBlocksMessage(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.replaceBlocks(editSession, region, from, to)));
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
    public void overlay(Player player, EditSession editSession, @Selection Region region, Pattern pattern) throws WorldEditException {
        CommandFutureUtils.withChangedBlocksMessage(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.groundOverlay(editSession, asFlatRegion(region), pattern)));
    }

    @Command(
        aliases = { "/center", "/middle" },
        usage = "<block>",
        desc = "Set the center block to the given block.",
        min = 1,
        max = 1
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.region.center")
    public void center(Player player, EditSession editSession, @Selection Region region, Pattern pattern) throws WorldEditException {
        Vector center = region.getCenter();
        editSession.setBlock(region.getCenter(), pattern.apply(center));
        player.print("Center block set.");
    }

    @Command(
        aliases = { "/naturalize" },
        usage = "",
        desc = "'Naturalize' the selection -- 3 layers of dirt on top then rock below.",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.region.naturalize")
    @Logging(REGION)
    public void naturalize(Player player, EditSession editSession, @Selection Region region) throws WorldEditException {
        OperationFuture future = Operations.completeSlowly(editSession,
                CommonOperationFactory.naturalize(editSession, asFlatRegion(region)));
        Futures.addCallback(future, new AbstractNotifier(player) {
            @Override
            public void onSuccess(Operation result) {
                CountDelegatedOperation op = (CountDelegatedOperation) result;
                player.print(op.getAffected() + " blocks naturalized.");
            }
        }, Operations.getExecutor());
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
    public void walls(Player player, EditSession editSession, @Selection Region region, Pattern pattern) throws WorldEditException {
        CuboidRegion cuboid = CuboidRegion.makeCuboid(region);
        Region walls = cuboid.getWalls();

        set(player, editSession, walls, pattern);
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
    public void faces(Player player, EditSession editSession, @Selection Region region, Pattern pattern) throws WorldEditException {
        CuboidRegion cuboid = CuboidRegion.makeCuboid(region);
        Region faces = cuboid.getFaces();

        set(player, editSession, faces, pattern);
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
    public void smooth(Player player, EditSession editSession,
                       @Selection Region region,
                       @Optional("1") int iterations,
                       @Switch('n') boolean affectNatural) throws WorldEditException {

        HeightMap heightMap = new HeightMap(editSession, region, affectNatural);
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
    public void move(final Player player, EditSession editSession, final LocalSession session,
                     @Selection final Region region,
                     @Optional("1") @Range(min = 1) final int count,
                     @Optional(Direction.AIM) @Direction final Vector direction,
                     @Optional("air") BaseBlock replace,
                     @Switch('s') boolean moveSelection) throws WorldEditException {

        OperationFuture future;
        future = Operations.completeSlowly(editSession,
                CommonOperationFactory.moveRegion(editSession, region, direction, count, true, replace));

        Futures.addCallback(future, new BlocksChangedNotifier(player,
                moveSelection ? "Selection moved" : "Blocks moved", false), Operations.getExecutor());

        if (moveSelection) {
            Futures.addCallback(future, new FutureCallback<Operation>() {
                @Override
                public void onSuccess(Operation result) {
                    try {
                        region.shift(direction.multiply(count));

                        session.getRegionSelector(player.getWorld()).learnChanges();
                        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
                    } catch (RegionOperationException e) {
                        player.printError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                }
            }, Operations.getExecutor());
        }
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
    public void stack(final Player player, EditSession editSession, final LocalSession session,
                      @Selection final Region region,
                      @Optional("1") @Range(min = 1) final int count,
                      @Optional(Direction.AIM) @Direction final Vector direction,
                      @Switch('s') boolean moveSelection,
                      @Switch('a') boolean ignoreAirBlocks) throws WorldEditException {
        OperationFuture future;
        future = Operations.completeSlowly(editSession,
                CommonOperationFactory.stackCubiodRegion(editSession, region, direction, count, !ignoreAirBlocks));

        Futures.addCallback(future, new BlocksChangedNotifier(player, "Undo with //undo", true), Operations.getExecutor());

        if (moveSelection) {
            Futures.addCallback(future, new FutureCallback<Operation>() {
                @Override
                public void onSuccess(Operation result) {
                    try {
                        final Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint());

                        final Vector shiftVector = direction.multiply(count * (Math.abs(direction.dot(size)) + 1));
                        region.shift(shiftVector);

                        session.getRegionSelector(player.getWorld()).learnChanges();
                        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
                    } catch (RegionOperationException e) {
                        player.printError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                }
            }, Operations.getExecutor());
        }
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
    public void regenerateChunk(Player player, LocalSession session, EditSession editSession, @Selection Region region) throws WorldEditException {
        Mask mask = session.getMask();
        try {
            session.setMask((Mask) null);
            player.getWorld().regenerate(region, editSession);
        } finally {
            session.setMask(mask);
        }
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
                "to fetch. See also tinyurl.com/wesyntax.\n" +
                "Flags:\n" +
                "  -r: Use raw coordinates (start from 0,0)\n" +
                "  -o: Use offset coordinates (start from you)",
            flags = "ro",
            min = 1,
            max = -1
    )
    @CommandPermissions("worldedit.region.deform")
    @Logging(ALL)
    public void deform(Player player, LocalSession session, EditSession editSession,
                       @Selection Region region,
                       @Text String expression,
                       @Switch('r') boolean useRawCoords,
                       @Switch('o') boolean offset) throws WorldEditException {
        final Vector zero;
        Vector unit;

        if (useRawCoords) {
            zero = Vector.ZERO;
            unit = Vector.ONE;
        } else if (offset) {
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
        usage = "[thickness] [block]",
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
    public void hollow(Player player, EditSession editSession,
                       @Selection Region region,
                       @Optional("0") @Range(min = 0) int thickness,
                       @Optional("air") Pattern pattern) throws WorldEditException {

        int affected = editSession.hollowOutRegion(region, thickness, Patterns.wrap(pattern));
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
    public void forest(Player player, EditSession editSession, @Selection Region region, @Optional("tree") TreeType type,
                       @Optional("5") @Range(min = 0, max = 100) double density) throws WorldEditException {
        density = density / 100;
        ForestGenerator generator = new ForestGenerator(editSession, new TreeGenerator(type));
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(editSession), generator);
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));

        CommandFutureUtils.withChangedBlocksMessage(player,
                Operations.completeSlowly(editSession, new CountDelegatedOperation(visitor, ground)));
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
    public void flora(Player player, EditSession editSession, @Selection Region region, @Optional("10") @Range(min = 0, max = 100) double density) throws WorldEditException {
        density = density / 100;
        FloraGenerator generator = new FloraGenerator(editSession);
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(editSession), generator);
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));

        CommandFutureUtils.withChangedBlocksMessage(player,
                Operations.completeSlowly(editSession, new CountDelegatedOperation(visitor, ground)));
    }

}
