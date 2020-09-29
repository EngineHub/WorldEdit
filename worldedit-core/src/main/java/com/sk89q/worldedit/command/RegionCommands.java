/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.generator.FloraGenerator;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.NoiseFilter2D;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.annotation.Offset;
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
import com.sk89q.worldedit.util.formatting.component.TextUtils;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.World;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
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
    public int set(Actor actor, EditSession editSession,
                   @Selection Region region,
                   @Arg(desc = "The pattern of blocks to set")
                       Pattern pattern) {
        RegionFunction set = new BlockReplace(editSession, pattern);
        RegionVisitor visitor = new RegionVisitor(region, set);

        Operations.completeBlindly(visitor);
        List<Component> messages = Lists.newArrayList(visitor.getStatusMessages());
        if (messages.isEmpty()) {
            actor.printInfo(TranslatableComponent.of("worldedit.set.done"));
        } else {
            actor.printInfo(TranslatableComponent.of("worldedit.set.done.verbose", TextUtils.join(messages, TextComponent.of(", "))));
        }

        return visitor.getAffected();
    }

    @Command(
        name = "/line",
        desc = "Draws line segments between cuboid selection corners or convex polyhedral selection vertices",
        descFooter = "Can only be used with a cuboid selection or a convex polyhedral selection"
    )
    @CommandPermissions("worldedit.region.line")
    @Logging(REGION)
    public int line(Actor actor, EditSession editSession,
                    @Selection Region region,
                    @Arg(desc = "The pattern of blocks to place")
                        Pattern pattern,
                    @Arg(desc = "The thickness of the line", def = "0")
                        int thickness,
                    @Switch(name = 'h', desc = "Generate only a shell")
                        boolean shell) throws WorldEditException {
        if (!((region instanceof CuboidRegion) || (region instanceof ConvexPolyhedralRegion))) {
            actor.printError(TranslatableComponent.of("worldedit.line.invalid-type"));
            return 0;
        }
        checkCommandArgument(thickness >= 0, "Thickness must be >= 0");


        List<BlockVector3> vectors;

        if (region instanceof CuboidRegion) {
            CuboidRegion cuboidRegion = (CuboidRegion) region;
            vectors = ImmutableList.of(cuboidRegion.getPos1(), cuboidRegion.getPos2());
        } else {
            ConvexPolyhedralRegion convexRegion = (ConvexPolyhedralRegion) region;
            vectors = ImmutableList.copyOf(convexRegion.getVertices());
        }

        int blocksChanged = editSession.drawLine(pattern, vectors, thickness, !shell);

        actor.printInfo(TranslatableComponent.of("worldedit.line.changed", TextComponent.of(blocksChanged)));
        return blocksChanged;
    }

    @Command(
        name = "/curve",
        desc = "Draws a spline through selected points",
        descFooter = "Can only be used with a convex polyhedral selection"
    )
    @CommandPermissions("worldedit.region.curve")
    @Logging(REGION)
    public int curve(Actor actor, EditSession editSession,
                     @Selection Region region,
                     @Arg(desc = "The pattern of blocks to place")
                         Pattern pattern,
                     @Arg(desc = "The thickness of the curve", def = "0")
                         int thickness,
                     @Switch(name = 'h', desc = "Generate only a shell")
                         boolean shell) throws WorldEditException {
        if (!(region instanceof ConvexPolyhedralRegion)) {
            actor.printError(TranslatableComponent.of("worldedit.curve.invalid-type"));
            return 0;
        }
        checkCommandArgument(thickness >= 0, "Thickness must be >= 0");

        ConvexPolyhedralRegion cpregion = (ConvexPolyhedralRegion) region;
        List<BlockVector3> vectors = new ArrayList<>(cpregion.getVertices());

        int blocksChanged = editSession.drawSpline(pattern, vectors, 0, 0, 0, 10, thickness, !shell);

        actor.printInfo(TranslatableComponent.of("worldedit.curve.changed", TextComponent.of(blocksChanged)));
        return blocksChanged;
    }

    @Command(
        name = "/replace",
        aliases = { "/re", "/rep" },
        desc = "Replace all blocks in the selection with another"
    )
    @CommandPermissions("worldedit.region.replace")
    @Logging(REGION)
    public int replace(Actor actor, EditSession editSession, @Selection Region region,
                       @Arg(desc = "The mask representing blocks to replace", def = "")
                           Mask from,
                       @Arg(desc = "The pattern of blocks to replace with")
                           Pattern to) throws WorldEditException {
        if (from == null) {
            from = new ExistingBlockMask(editSession);
        }
        int affected = editSession.replaceBlocks(region, from, to);
        actor.printInfo(TranslatableComponent.of("worldedit.replace.replaced", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/overlay",
        desc = "Set a block on top of blocks in the region"
    )
    @CommandPermissions("worldedit.region.overlay")
    @Logging(REGION)
    public int overlay(Actor actor, EditSession editSession, @Selection Region region,
                       @Arg(desc = "The pattern of blocks to overlay")
                           Pattern pattern) throws WorldEditException {
        int affected = editSession.overlayCuboidBlocks(region, pattern);
        actor.printInfo(TranslatableComponent.of("worldedit.overlay.overlaid", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/center",
        aliases = { "/middle" },
        desc = "Set the center block(s)"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.region.center")
    public int center(Actor actor, EditSession editSession, @Selection Region region,
                      @Arg(desc = "The pattern of blocks to set")
                          Pattern pattern) throws WorldEditException {
        int affected = editSession.center(region, pattern);
        actor.printInfo(TranslatableComponent.of("worldedit.center.changed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/naturalize",
        desc = "3 layers of dirt on top then rock below"
    )
    @CommandPermissions("worldedit.region.naturalize")
    @Logging(REGION)
    public int naturalize(Actor actor, EditSession editSession, @Selection Region region) throws WorldEditException {
        int affected = editSession.naturalizeCuboidBlocks(region);
        actor.printInfo(TranslatableComponent.of("worldedit.naturalize.naturalized", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/walls",
        desc = "Build the four sides of the selection"
    )
    @CommandPermissions("worldedit.region.walls")
    @Logging(REGION)
    public int walls(Actor actor, EditSession editSession, @Selection Region region,
                     @Arg(desc = "The pattern of blocks to set")
                         Pattern pattern) throws WorldEditException {
        int affected = editSession.makeWalls(region, pattern);
        actor.printInfo(TranslatableComponent.of("worldedit.walls.changed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/faces",
        aliases = { "/outline" },
        desc = "Build the walls, ceiling, and floor of a selection"
    )
    @CommandPermissions("worldedit.region.faces")
    @Logging(REGION)
    public int faces(Actor actor, EditSession editSession, @Selection Region region,
                     @Arg(desc = "The pattern of blocks to set")
                         Pattern pattern) throws WorldEditException {
        int affected = editSession.makeCuboidFaces(region, pattern);
        actor.printInfo(TranslatableComponent.of("worldedit.faces.changed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/smooth",
        desc = "Smooth the elevation in the selection",
        descFooter = "Example: '//smooth 1 grass_block,dirt,stone' would only smooth natural surface terrain."
    )
    @CommandPermissions("worldedit.region.smooth")
    @Logging(REGION)
    public int smooth(Actor actor, EditSession editSession, @Selection Region region,
                      @Arg(desc = "# of iterations to perform", def = "1")
                          int iterations,
                      @Arg(desc = "The mask of blocks to use as the height map", def = "")
                          Mask mask) throws WorldEditException {
        HeightMap heightMap = new HeightMap(editSession, region, mask);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        int affected = heightMap.applyFilter(filter, iterations);
        actor.printInfo(TranslatableComponent.of("worldedit.smooth.changed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/move",
        desc = "Move the contents of the selection"
    )
    @CommandPermissions("worldedit.region.move")
    @Logging(ORIENTATION_REGION)
    public int move(Actor actor, World world, EditSession editSession, LocalSession session,
                    @Selection Region region,
                    @Arg(desc = "number of times to apply the offset", def = "1")
                        int multiplier,
                    @Arg(desc = "The offset to move", def = Offset.FORWARD)
                    @Offset
                        BlockVector3 offset,
                    @Arg(desc = "The pattern of blocks to leave", def = "air")
                        Pattern replace,
                    @Switch(name = 's', desc = "Shift the selection to the target location")
                        boolean moveSelection,
                    @Switch(name = 'a', desc = "Ignore air blocks")
                        boolean ignoreAirBlocks,
                    @Switch(name = 'e', desc = "Also copy entities")
                        boolean copyEntities,
                    @Switch(name = 'b', desc = "Also copy biomes")
                        boolean copyBiomes,
                    @ArgFlag(name = 'm', desc = "Set the include mask, non-matching blocks become air")
                        Mask mask) throws WorldEditException {
        checkCommandArgument(multiplier >= 1, "Multiplier must be >= 1");

        Mask combinedMask;
        if (ignoreAirBlocks) {
            if (mask == null) {
                combinedMask = new ExistingBlockMask(editSession);
            } else {
                combinedMask = new MaskIntersection(mask, new ExistingBlockMask(editSession));
            }
        } else {
            combinedMask = mask;
        }

        int affected = editSession.moveRegion(region, offset, multiplier, copyEntities, copyBiomes, combinedMask, replace);

        if (moveSelection) {
            try {
                region.shift(offset.multiply(multiplier));

                session.getRegionSelector(world).learnChanges();
                session.getRegionSelector(world).explainRegionAdjust(actor, session);
            } catch (RegionOperationException e) {
                actor.printError(e.getRichMessage());
            }
        }

        actor.printInfo(TranslatableComponent.of("worldedit.move.moved", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/stack",
        desc = "Repeat the contents of the selection"
    )
    @CommandPermissions("worldedit.region.stack")
    @Logging(ORIENTATION_REGION)
    public int stack(Actor actor, World world, EditSession editSession, LocalSession session,
                     @Selection Region region,
                     @Arg(desc = "# of copies to stack", def = "1")
                         int count,
                     @Arg(desc = "How far to move the contents each stack", def = Offset.FORWARD)
                     @Offset
                         BlockVector3 offset,
                     @Switch(name = 's', desc = "Shift the selection to the last stacked copy")
                         boolean moveSelection,
                     @Switch(name = 'a', desc = "Ignore air blocks")
                         boolean ignoreAirBlocks,
                     @Switch(name = 'e', desc = "Also copy entities")
                         boolean copyEntities,
                     @Switch(name = 'b', desc = "Also copy biomes")
                         boolean copyBiomes,
                     @Switch(name = 'r', desc = "Use block units")
                        boolean blockUnits,
                     @ArgFlag(name = 'm', desc = "Set the include mask, non-matching blocks become air")
                         Mask mask) throws WorldEditException {
        Mask combinedMask;
        if (ignoreAirBlocks) {
            if (mask == null) {
                combinedMask = new ExistingBlockMask(editSession);
            } else {
                combinedMask = new MaskIntersection(mask, new ExistingBlockMask(editSession));
            }
        } else {
            combinedMask = mask;
        }

        int affected;
        if (blockUnits) {
            affected = editSession.stackRegionBlockUnits(region, offset, count, copyEntities, copyBiomes, combinedMask);
        } else {
            affected = editSession.stackCuboidRegion(region, offset, count, copyEntities, copyBiomes, combinedMask);
        }

        if (moveSelection) {
            try {
                final BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);

                final BlockVector3 shiftSize = blockUnits ? offset : offset.multiply(size);
                final BlockVector3 shiftVector = shiftSize.multiply(count);
                region.shift(shiftVector);

                session.getRegionSelector(world).learnChanges();
                session.getRegionSelector(world).explainRegionAdjust(actor, session);
            } catch (RegionOperationException e) {
                actor.printError(e.getRichMessage());
            }
        }

        actor.printInfo(TranslatableComponent.of("worldedit.stack.changed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/regen",
        desc = "Regenerates the contents of the selection"
    )
    @CommandPermissions("worldedit.regen")
    @Logging(REGION)
    void regenerate(Actor actor, World world, LocalSession session, EditSession editSession,
                    @Selection Region region,
                    @Arg(desc = "The seed to regenerate with, otherwise uses world seed", def = "")
                        Long seed,
                    @Switch(name = 'b', desc = "Regenerate biomes as well")
                        boolean regenBiomes) {
        Mask mask = session.getMask();
        boolean success;
        try {
            session.setMask(null);
            RegenOptions options = RegenOptions.builder()
                .seed(seed)
                .regenBiomes(regenBiomes)
                .build();
            success = world.regenerate(region, editSession, options);
        } finally {
            session.setMask(mask);
        }
        if (success) {
            actor.printInfo(TranslatableComponent.of("worldedit.regen.regenerated"));
        } else {
            actor.printError(TranslatableComponent.of("worldedit.regen.failed"));
        }
    }

    @Command(
        name = "/deform",
        desc = "Deforms a selected region with an expression",
        descFooter = "The expression is executed for each block and is expected\n"
            + "to modify the variables x, y and z to point to a new block\n"
            + "to fetch. See also https://tinyurl.com/weexpr"
    )
    @CommandPermissions("worldedit.region.deform")
    @Logging(ALL)
    public int deform(Actor actor, LocalSession session, EditSession editSession,
                      @Selection Region region,
                      @Arg(desc = "The expression to use", variable = true)
                          List<String> expression,
                      @Switch(name = 'r', desc = "Use the game's coordinate origin")
                          boolean useRawCoords,
                      @Switch(name = 'o', desc = "Use the placement's coordinate origin")
                          boolean offset,
                      @Switch(name = 'c', desc = "Use the selection's center as origin")
                          boolean offsetCenter) throws WorldEditException {
        final Vector3 zero;
        Vector3 unit;

        if (useRawCoords) {
            zero = Vector3.ZERO;
            unit = Vector3.ONE;
        } else if (offset) {
            zero = session.getPlacementPosition(actor).toVector3();
            unit = Vector3.ONE;
        } else if (offsetCenter) {
            final Vector3 min = region.getMinimumPoint().toVector3();
            final Vector3 max = region.getMaximumPoint().toVector3();

            zero = max.add(min).multiply(0.5);
            unit = Vector3.ONE;
        } else {
            final Vector3 min = region.getMinimumPoint().toVector3();
            final Vector3 max = region.getMaximumPoint().toVector3();

            zero = max.add(min).divide(2);
            unit = max.subtract(zero);

            if (unit.getX() == 0) {
                unit = unit.withX(1.0);
            }
            if (unit.getY() == 0) {
                unit = unit.withY(1.0);
            }
            if (unit.getZ() == 0) {
                unit = unit.withZ(1.0);
            }
        }

        try {
            final int affected = editSession.deformRegion(region, zero, unit, String.join(" ", expression), session.getTimeout());
            if (actor instanceof Player) {
                ((Player) actor).findFreePosition();
            }
            actor.printInfo(TranslatableComponent.of("worldedit.deform.deformed", TextComponent.of(affected)));
            return affected;
        } catch (ExpressionException e) {
            actor.printError(TextComponent.of(e.getMessage()));
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
    public int hollow(Actor actor, EditSession editSession,
                      @Selection Region region,
                      @Arg(desc = "Thickness of the shell to leave", def = "0")
                          int thickness,
                      @Arg(desc = "The pattern of blocks to replace the hollowed area with", def = "air")
                          Pattern pattern) throws WorldEditException {
        checkCommandArgument(thickness >= 0, "Thickness must be >= 0");

        int affected = editSession.hollowOutRegion(region, thickness, pattern);
        actor.printInfo(TranslatableComponent.of("worldedit.hollow.changed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/forest",
        desc = "Make a forest within the region"
    )
    @CommandPermissions("worldedit.region.forest")
    @Logging(REGION)
    public int forest(Actor actor, EditSession editSession, @Selection Region region,
                      @Arg(desc = "The type of tree to place", def = "tree")
                          TreeType type,
                      @Arg(desc = "The density of the forest", def = "5")
                          double density) throws WorldEditException {
        checkCommandArgument(0 <= density && density <= 100, "Density must be in [0, 100]");
        int affected = editSession.makeForest(region, density / 100, type);
        actor.printInfo(TranslatableComponent.of("worldedit.forest.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/flora",
        desc = "Make flora within the region"
    )
    @CommandPermissions("worldedit.region.flora")
    @Logging(REGION)
    public int flora(Actor actor, EditSession editSession, @Selection Region region,
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
        actor.printInfo(TranslatableComponent.of("worldedit.flora.created", TextComponent.of(affected)));
        return affected;
    }

}
