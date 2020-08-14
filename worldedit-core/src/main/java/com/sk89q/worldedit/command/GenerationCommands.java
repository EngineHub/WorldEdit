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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Radii;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.command.util.Logging.LogMode.ALL;
import static com.sk89q.worldedit.command.util.Logging.LogMode.PLACEMENT;
import static com.sk89q.worldedit.command.util.Logging.LogMode.POSITION;
import static com.sk89q.worldedit.internal.command.CommandUtil.checkCommandArgument;

/**
 * Commands for the generation of shapes and other objects.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class GenerationCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public GenerationCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "/hcyl",
        desc = "Generates a hollow cylinder."
    )
    @CommandPermissions("worldedit.generation.cylinder")
    @Logging(PLACEMENT)
    public int hcyl(Actor actor, LocalSession session, EditSession editSession,
                    @Arg(desc = "The pattern of blocks to generate")
                        Pattern pattern,
                    @Arg(desc = "The radii of the cylinder. 1st is N/S, 2nd is E/W")
                    @Radii(2)
                        List<Double> radii,
                    @Arg(desc = "The height of the cylinder", def = "1")
                        int height) throws WorldEditException {
        return cyl(actor, session, editSession, pattern, radii, height, true);
    }

    @Command(
        name = "/cyl",
        desc = "Generates a cylinder."
    )
    @CommandPermissions("worldedit.generation.cylinder")
    @Logging(PLACEMENT)
    public int cyl(Actor actor, LocalSession session, EditSession editSession,
                   @Arg(desc = "The pattern of blocks to generate")
                       Pattern pattern,
                   @Arg(desc = "The radii of the cylinder. 1st is N/S, 2nd is E/W")
                   @Radii(2)
                       List<Double> radii,
                   @Arg(desc = "The height of the cylinder", def = "1")
                       int height,
                   @Switch(name = 'h', desc = "Make a hollow cylinder")
                       boolean hollow) throws WorldEditException {
        double radiusX;
        double radiusZ;
        switch (radii.size()) {
            case 1:
                radiusX = radiusZ = Math.max(1, radii.get(0));
                break;

            case 2:
                radiusX = Math.max(1, radii.get(0));
                radiusZ = Math.max(1, radii.get(1));
                break;

            default:
                actor.printError(TranslatableComponent.of("worldedit.cyl.invalid-radius"));
                return 0;
        }

        worldEdit.checkMaxRadius(radiusX);
        worldEdit.checkMaxRadius(radiusZ);
        worldEdit.checkMaxRadius(height);

        BlockVector3 pos = session.getPlacementPosition(actor);
        int affected = editSession.makeCylinder(pos, pattern, radiusX, radiusZ, height, !hollow);
        actor.printInfo(TranslatableComponent.of("worldedit.cyl.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/hsphere",
        desc = "Generates a hollow sphere."
    )
    @CommandPermissions("worldedit.generation.sphere")
    @Logging(PLACEMENT)
    public int hsphere(Actor actor, LocalSession session, EditSession editSession,
                       @Arg(desc = "The pattern of blocks to generate")
                           Pattern pattern,
                       @Arg(desc = "The radii of the sphere. Order is N/S, U/D, E/W")
                       @Radii(3)
                           List<Double> radii,
                       @Switch(name = 'r', desc = "Raise the bottom of the sphere to the placement position")
                           boolean raised) throws WorldEditException {
        return sphere(actor, session, editSession, pattern, radii, raised, true);
    }

    @Command(
        name = "/sphere",
        desc = "Generates a filled sphere."
    )
    @CommandPermissions("worldedit.generation.sphere")
    @Logging(PLACEMENT)
    public int sphere(Actor actor, LocalSession session, EditSession editSession,
                      @Arg(desc = "The pattern of blocks to generate")
                          Pattern pattern,
                      @Arg(desc = "The radii of the sphere. Order is N/S, U/D, E/W")
                      @Radii(3)
                          List<Double> radii,
                      @Switch(name = 'r', desc = "Raise the bottom of the sphere to the placement position")
                          boolean raised,
                      @Switch(name = 'h', desc = "Make a hollow sphere")
                          boolean hollow) throws WorldEditException {
        double radiusX;
        double radiusY;
        double radiusZ;
        switch (radii.size()) {
            case 1:
                radiusX = radiusY = radiusZ = Math.max(0, radii.get(0));
                break;

            case 3:
                radiusX = Math.max(0, radii.get(0));
                radiusY = Math.max(0, radii.get(1));
                radiusZ = Math.max(0, radii.get(2));
                break;

            default:
                actor.printError(TranslatableComponent.of("worldedit.sphere.invalid-radius"));
                return 0;
        }

        worldEdit.checkMaxRadius(radiusX);
        worldEdit.checkMaxRadius(radiusY);
        worldEdit.checkMaxRadius(radiusZ);

        BlockVector3 pos = session.getPlacementPosition(actor);
        if (raised) {
            pos = pos.add(0, (int) radiusY, 0);
        }

        int affected = editSession.makeSphere(pos, pattern, radiusX, radiusY, radiusZ, !hollow);
        if (actor instanceof Player) {
            ((Player) actor).findFreePosition();
        }
        actor.printInfo(TranslatableComponent.of("worldedit.sphere.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "forestgen",
        desc = "Generate a forest"
    )
    @CommandPermissions("worldedit.generation.forest")
    @Logging(POSITION)
    public int forestGen(Actor actor, LocalSession session, EditSession editSession,
                         @Arg(desc = "The size of the forest, in blocks", def = "10")
                             int size,
                         @Arg(desc = "The type of forest", def = "tree")
                             TreeType type,
                         @Arg(desc = "The density of the forest, between 0 and 100", def = "5")
                             double density) throws WorldEditException {
        checkCommandArgument(0 <= density && density <= 100, "Density must be between 0 and 100");
        worldEdit.checkMaxRadius(size);
        density /= 100;
        int affected = editSession.makeForest(session.getPlacementPosition(actor), size, density, type);
        actor.printInfo(TranslatableComponent.of("worldedit.forestgen.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "pumpkins",
        desc = "Generate pumpkin patches"
    )
    @CommandPermissions("worldedit.generation.pumpkins")
    @Logging(POSITION)
    public int pumpkins(Actor actor, LocalSession session, EditSession editSession,
                        @Arg(desc = "The size of the patch", def = "10")
                            int size) throws WorldEditException {
        worldEdit.checkMaxRadius(size);
        int affected = editSession.makePumpkinPatches(session.getPlacementPosition(actor), size);
        actor.printInfo(TranslatableComponent.of("worldedit.pumpkins.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/hpyramid",
        desc = "Generate a hollow pyramid"
    )
    @CommandPermissions("worldedit.generation.pyramid")
    @Logging(PLACEMENT)
    public int hollowPyramid(Actor actor, LocalSession session, EditSession editSession,
                             @Arg(desc = "The pattern of blocks to set")
                                 Pattern pattern,
                             @Arg(desc = "The size of the pyramid")
                                 int size) throws WorldEditException {
        return pyramid(actor, session, editSession, pattern, size, true);
    }

    @Command(
        name = "/pyramid",
        desc = "Generate a filled pyramid"
    )
    @CommandPermissions("worldedit.generation.pyramid")
    @Logging(PLACEMENT)
    public int pyramid(Actor actor, LocalSession session, EditSession editSession,
                       @Arg(desc = "The pattern of blocks to set")
                           Pattern pattern,
                       @Arg(desc = "The size of the pyramid")
                           int size,
                       @Switch(name = 'h', desc = "Make a hollow pyramid")
                           boolean hollow) throws WorldEditException {
        worldEdit.checkMaxRadius(size);
        BlockVector3 pos = session.getPlacementPosition(actor);
        int affected = editSession.makePyramid(pos, pattern, size, !hollow);
        if (actor instanceof Player) {
            ((Player) actor).findFreePosition();
        }
        actor.printInfo(TranslatableComponent.of("worldedit.pyramid.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/generate",
        aliases = { "/gen", "/g" },
        desc = "Generates a shape according to a formula.",
        descFooter = "See also https://tinyurl.com/weexpr."
    )
    @CommandPermissions("worldedit.generation.shape")
    @Logging(ALL)
    public int generate(Actor actor, LocalSession session, EditSession editSession,
                        @Selection Region region,
                        @Arg(desc = "The pattern of blocks to set")
                            Pattern pattern,
                        @Arg(desc = "Expression to test block placement locations and set block type", variable = true)
                            List<String> expression,
                        @Switch(name = 'h', desc = "Generate a hollow shape")
                            boolean hollow,
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

            zero = max.add(min).multiply(0.5);
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
            final int affected = editSession.makeShape(region, zero, unit, pattern, String.join(" ", expression), hollow, session.getTimeout());
            if (actor instanceof Player) {
                ((Player) actor).findFreePosition();
            }
            actor.printInfo(TranslatableComponent.of("worldedit.generate.created", TextComponent.of(affected)));
            return affected;
        } catch (ExpressionException e) {
            actor.printError(TextComponent.of(e.getMessage()));
            return 0;
        }
    }

    @Command(
        name = "/generatebiome",
        aliases = { "/genbiome", "/gb" },
        desc = "Sets biome according to a formula.",
        descFooter = "See also https://tinyurl.com/weexpr."
    )
    @CommandPermissions("worldedit.generation.shape.biome")
    @Logging(ALL)
    public int generateBiome(Actor actor, LocalSession session, EditSession editSession,
                             @Selection Region region,
                             @Arg(desc = "The biome type to set")
                                 BiomeType target,
                             @Arg(desc = "Expression to test block placement locations and set biome type", variable = true)
                                 List<String> expression,
                             @Switch(name = 'h', desc = "Generate a hollow shape")
                                 boolean hollow,
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

            zero = max.add(min).multiply(0.5);
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
            final int affected = editSession.makeBiomeShape(region, zero, unit, target, String.join(" ", expression), hollow, session.getTimeout());
            actor.printInfo(TranslatableComponent.of("worldedit.generatebiome.changed", TextComponent.of(affected)));
            return affected;
        } catch (ExpressionException e) {
            actor.printError(TextComponent.of(e.getMessage()));
            return 0;
        }
    }

}
