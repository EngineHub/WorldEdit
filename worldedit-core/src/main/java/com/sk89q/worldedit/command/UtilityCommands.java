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
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.HeightConverter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.CreatureButcher;
import com.sk89q.worldedit.command.util.EntityRemover;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.PrintCommandHelp;
import com.sk89q.worldedit.command.util.WorldEditAsyncCommandBuilder;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.EntityVisitor;
import com.sk89q.worldedit.internal.annotation.VertHeight;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.component.SubtleFormat;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static com.sk89q.worldedit.command.util.Logging.LogMode.PLACEMENT;

/**
 * Utility commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class UtilityCommands {

    private final WorldEdit we;

    public UtilityCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        name = "/fill",
        desc = "Fill a hole"
    )
    @CommandPermissions("worldedit.fill")
    @Logging(PLACEMENT)
    public int fill(Actor actor, LocalSession session, EditSession editSession,
                    @Arg(desc = "The blocks to fill with")
                        Pattern pattern,
                    @Arg(desc = "The radius to fill in")
                        double radius,
                    @Arg(desc = "The depth to fill", def = "1")
                        int depth) throws WorldEditException {
        radius = Math.max(1, radius);
        we.checkMaxRadius(radius);
        depth = Math.max(1, depth);

        BlockVector3 pos = session.getPlacementPosition(actor);
        int affected = editSession.fillXZ(pos, pattern, radius, depth, false);
        actor.printInfo(TranslatableComponent.of("worldedit.fill.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/fillr",
        desc = "Fill a hole recursively"
    )
    @CommandPermissions("worldedit.fill.recursive")
    @Logging(PLACEMENT)
    public int fillr(Actor actor, LocalSession session, EditSession editSession,
                     @Arg(desc = "The blocks to fill with")
                         Pattern pattern,
                     @Arg(desc = "The radius to fill in")
                         double radius,
                     @Arg(desc = "The depth to fill", def = "")
                         Integer depth) throws WorldEditException {
        radius = Math.max(1, radius);
        we.checkMaxRadius(radius);
        depth = depth == null ? Integer.MAX_VALUE : Math.max(1, depth);
        we.checkMaxRadius(radius);

        BlockVector3 pos = session.getPlacementPosition(actor);
        int affected = editSession.fillXZ(pos, pattern, radius, depth, true);
        actor.printInfo(TranslatableComponent.of("worldedit.fillr.created", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "/drain",
        desc = "Drain a pool"
    )
    @CommandPermissions("worldedit.drain")
    @Logging(PLACEMENT)
    public int drain(Actor actor, LocalSession session, EditSession editSession,
                     @Arg(desc = "The radius to drain")
                         double radius,
                     @Switch(name = 'w', desc = "Also un-waterlog blocks")
                         boolean waterlogged) throws WorldEditException {
        radius = Math.max(0, radius);
        we.checkMaxRadius(radius);
        int affected = editSession.drainArea(session.getPlacementPosition(actor), radius, waterlogged);
        actor.printInfo(TranslatableComponent.of("worldedit.drain.drained", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "fixlava",
        aliases = { "/fixlava" },
        desc = "Fix lava to be stationary"
    )
    @CommandPermissions("worldedit.fixlava")
    @Logging(PLACEMENT)
    public int fixLava(Actor actor, LocalSession session, EditSession editSession,
                       @Arg(desc = "The radius to fix in")
                           double radius) throws WorldEditException {
        radius = Math.max(0, radius);
        we.checkMaxRadius(radius);
        int affected = editSession.fixLiquid(session.getPlacementPosition(actor), radius, BlockTypes.LAVA);
        actor.printInfo(TranslatableComponent.of("worldedit.fixlava.fixed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "fixwater",
        aliases = { "/fixwater" },
        desc = "Fix water to be stationary"
    )
    @CommandPermissions("worldedit.fixwater")
    @Logging(PLACEMENT)
    public int fixWater(Actor actor, LocalSession session, EditSession editSession,
                        @Arg(desc = "The radius to fix in")
                            double radius) throws WorldEditException {
        radius = Math.max(0, radius);
        we.checkMaxRadius(radius);
        int affected = editSession.fixLiquid(session.getPlacementPosition(actor), radius, BlockTypes.WATER);
        actor.printInfo(TranslatableComponent.of("worldedit.fixwater.fixed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "removeabove",
        aliases = { "/removeabove" },
        desc = "Remove blocks above your head."
    )
    @CommandPermissions("worldedit.removeabove")
    @Logging(PLACEMENT)
    public int removeAbove(Actor actor, LocalSession session, EditSession editSession,
                           @Arg(desc = "The apothem of the square to remove from", def = "1")
                               int size,
                           @Arg(
                               desc = "The maximum height above you to remove from",
                               def = HeightConverter.DEFAULT_VALUE
                           )
                           @VertHeight
                               int height) throws WorldEditException {
        size = Math.max(1, size);
        we.checkMaxRadius(size);

        int affected = editSession.removeAbove(session.getPlacementPosition(actor), size, height);
        actor.printInfo(TranslatableComponent.of("worldedit.removeabove.removed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "removebelow",
        aliases = { "/removebelow" },
        desc = "Remove blocks below you."
    )
    @CommandPermissions("worldedit.removebelow")
    @Logging(PLACEMENT)
    public int removeBelow(Actor actor, LocalSession session, EditSession editSession,
                           @Arg(desc = "The apothem of the square to remove from", def = "1")
                               int size,
                           @Arg(
                               desc = "The maximum height below you to remove from",
                               def = HeightConverter.DEFAULT_VALUE
                           )
                           @VertHeight
                               int height) throws WorldEditException {
        size = Math.max(1, size);
        we.checkMaxRadius(size);

        int affected = editSession.removeBelow(session.getPlacementPosition(actor), size, height);
        actor.printInfo(TranslatableComponent.of("worldedit.removebelow.removed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "removenear",
        aliases = { "/removenear" },
        desc = "Remove blocks near you."
    )
    @CommandPermissions("worldedit.removenear")
    @Logging(PLACEMENT)
    public int removeNear(Actor actor, LocalSession session, EditSession editSession,
                          @Arg(desc = "The mask of blocks to remove")
                              Mask mask,
                          @Arg(desc = "The radius of the square to remove from", def = "50")
                              int radius) throws WorldEditException {
        radius = Math.max(1, radius);
        we.checkMaxRadius(radius);

        int affected = editSession.removeNear(session.getPlacementPosition(actor), mask, radius);
        actor.printInfo(TranslatableComponent.of("worldedit.removenear.removed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "replacenear",
        aliases = { "/replacenear" },
        desc = "Replace nearby blocks"
    )
    @CommandPermissions("worldedit.replacenear")
    @Logging(PLACEMENT)
    public int replaceNear(Actor actor, World world, LocalSession session, EditSession editSession,
                           @Arg(desc = "The radius of the square to remove in")
                               int radius,
                           @Arg(desc = "The mask matching blocks to remove", def = "")
                               Mask from,
                           @Arg(desc = "The pattern of blocks to replace with")
                               Pattern to) throws WorldEditException {
        radius = Math.max(1, radius);
        we.checkMaxRadius(radius);

        BlockVector3 base = session.getPlacementPosition(actor);
        BlockVector3 min = base.subtract(radius, radius, radius);
        BlockVector3 max = base.add(radius, radius, radius);
        Region region = new CuboidRegion(world, min, max);

        if (from == null) {
            from = new ExistingBlockMask(editSession);
        }

        int affected = editSession.replaceBlocks(region, from, to);
        actor.printInfo(TranslatableComponent.of("worldedit.replacenear.replaced", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "snow",
        aliases = { "/snow" },
        desc = "Simulates snow"
    )
    @CommandPermissions("worldedit.snow")
    @Logging(PLACEMENT)
    public int snow(Actor actor, LocalSession session, EditSession editSession,
                    @Arg(desc = "The radius of the cylinder to snow in", def = "10")
                        double size,
                    @Arg(
                        desc = "The height of the cylinder to snow in",
                        def = HeightConverter.DEFAULT_VALUE
                    )
                    @VertHeight
                        int height,
                    @Switch(name = 's', desc = "Stack snow layers")
                        boolean stack) throws WorldEditException {
        size = Math.max(1, size);
        height = Math.max(1, height);
        we.checkMaxRadius(size);

        BlockVector3 position = session.getPlacementPosition(actor);

        CylinderRegion region = new CylinderRegion(position, Vector2.at(size, size), position.getBlockY() - height, position.getBlockY() + height);
        int affected = editSession.simulateSnow(region, stack);
        actor.printInfo(TranslatableComponent.of(
            "worldedit.snow.created", TextComponent.of(affected)
        ));
        return affected;
    }

    @Command(
        name = "thaw",
        aliases = { "/thaw" },
        desc = "Thaws the area"
    )
    @CommandPermissions("worldedit.thaw")
    @Logging(PLACEMENT)
    public int thaw(Actor actor, LocalSession session, EditSession editSession,
                    @Arg(desc = "The radius of the cylinder to thaw in", def = "10")
                        double size,
                    @Arg(
                        desc = "The height of the cylinder to thaw in",
                        def = HeightConverter.DEFAULT_VALUE
                    )
                    @VertHeight
                        int height) throws WorldEditException {
        size = Math.max(1, size);
        height = Math.max(1, height);
        we.checkMaxRadius(size);

        int affected = editSession.thaw(session.getPlacementPosition(actor), size, height);
        actor.printInfo(TranslatableComponent.of(
            "worldedit.thaw.removed", TextComponent.of(affected)
        ));
        return affected;
    }

    @Command(
        name = "green",
        aliases = { "/green" },
        desc = "Converts dirt to grass blocks in the area"
    )
    @CommandPermissions("worldedit.green")
    @Logging(PLACEMENT)
    public int green(Actor actor, LocalSession session, EditSession editSession,
                     @Arg(desc = "The radius of the cylinder to convert in", def = "10")
                         double size,
                     @Arg(
                         desc = "The height of the cylinder to convert in",
                         def = HeightConverter.DEFAULT_VALUE
                     )
                     @VertHeight
                         int height,
                     @Switch(name = 'f', desc = "Also convert coarse dirt")
                         boolean convertCoarse) throws WorldEditException {
        size = Math.max(1, size);
        height = Math.max(1, height);
        we.checkMaxRadius(size);
        final boolean onlyNormalDirt = !convertCoarse;

        final int affected = editSession.green(
            session.getPlacementPosition(actor), size, height, onlyNormalDirt
        );
        actor.printInfo(TranslatableComponent.of(
            "worldedit.green.changed", TextComponent.of(affected)
        ));
        return affected;
    }

    @Command(
        name = "extinguish",
        aliases = { "/ex", "/ext", "/extinguish", "ex", "ext" },
        desc = "Extinguish nearby fire"
    )
    @CommandPermissions("worldedit.extinguish")
    @Logging(PLACEMENT)
    public int extinguish(Actor actor, LocalSession session, EditSession editSession,
                           @Arg(desc = "The radius of the square to remove in", def = "")
                               Integer radius) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        int defaultRadius = config.maxRadius != -1 ? Math.min(40, config.maxRadius) : 40;
        int size = radius != null ? Math.max(1, radius) : defaultRadius;
        we.checkMaxRadius(size);

        Mask mask = new BlockTypeMask(editSession, BlockTypes.FIRE);
        int affected = editSession.removeNear(session.getPlacementPosition(actor), mask, size);
        actor.printInfo(TranslatableComponent.of("worldedit.extinguish.removed", TextComponent.of(affected)));
        return affected;
    }

    @Command(
        name = "butcher",
        desc = "Kill all or nearby mobs"
    )
    @CommandPermissions("worldedit.butcher")
    @Logging(PLACEMENT)
    public int butcher(Actor actor,
                       @Arg(desc = "Radius to kill mobs in", def = "")
                           Integer radius,
                       @Switch(name = 'p', desc = "Also kill pets")
                           boolean killPets,
                       @Switch(name = 'n', desc = "Also kill NPCs")
                           boolean killNpcs,
                       @Switch(name = 'g', desc = "Also kill golems")
                           boolean killGolems,
                       @Switch(name = 'a', desc = "Also kill animals")
                           boolean killAnimals,
                       @Switch(name = 'b', desc = "Also kill ambient mobs")
                           boolean killAmbient,
                       @Switch(name = 't', desc = "Also kill mobs with name tags")
                           boolean killWithName,
                       @Switch(name = 'f', desc = "Also kill all friendly mobs (Applies the flags `-abgnpt`)")
                           boolean killFriendly,
                       @Switch(name = 'r', desc = "Also destroy armor stands")
                           boolean killArmorStands,
                       @Switch(name = 'w', desc = "Also kill water mobs")
                           boolean killWater) throws WorldEditException {
        LocalConfiguration config = we.getConfiguration();

        if (radius == null) {
            radius = config.butcherDefaultRadius;
        } else if (radius < -1) {
            actor.printError(TranslatableComponent.of("worldedit.butcher.explain-all"));
            return 0;
        } else if (radius == -1) {
            if (config.butcherMaxRadius != -1) {
                radius = config.butcherMaxRadius;
            }
        }
        if (config.butcherMaxRadius != -1) {
            radius = Math.min(radius, config.butcherMaxRadius);
        }

        CreatureButcher flags = new CreatureButcher(actor);
        flags.or(CreatureButcher.Flags.FRIENDLY, killFriendly); // No permission check here. Flags will instead be filtered by the subsequent calls.
        flags.or(CreatureButcher.Flags.PETS, killPets, "worldedit.butcher.pets");
        flags.or(CreatureButcher.Flags.NPCS, killNpcs, "worldedit.butcher.npcs");
        flags.or(CreatureButcher.Flags.GOLEMS, killGolems, "worldedit.butcher.golems");
        flags.or(CreatureButcher.Flags.ANIMALS, killAnimals, "worldedit.butcher.animals");
        flags.or(CreatureButcher.Flags.AMBIENT, killAmbient, "worldedit.butcher.ambient");
        flags.or(CreatureButcher.Flags.TAGGED, killWithName, "worldedit.butcher.tagged");
        flags.or(CreatureButcher.Flags.ARMOR_STAND, killArmorStands, "worldedit.butcher.armorstands");
        flags.or(CreatureButcher.Flags.WATER, killWater, "worldedit.butcher.water");

        int killed = killMatchingEntities(radius, actor, flags::createFunction);

        actor.printInfo(TranslatableComponent.of(
                "worldedit.butcher.killed",
                TextComponent.of(killed),
                TextComponent.of(radius)
        ));

        return killed;
    }

    @Command(
        name = "remove",
        aliases = { "rem", "rement" },
        desc = "Remove all entities of a type"
    )
    @CommandPermissions("worldedit.remove")
    @Logging(PLACEMENT)
    public int remove(Actor actor,
                      @Arg(desc = "The type of entity to remove")
                          EntityRemover remover,
                      @Arg(desc = "The radius of the cuboid to remove from")
                          int radius) throws WorldEditException {
        if (radius < -1) {
            actor.printError(TranslatableComponent.of("worldedit.remove.explain-all"));
            return 0;
        }

        int removed = killMatchingEntities(radius, actor, remover::createFunction);
        actor.printInfo(TranslatableComponent.of("worldedit.remove.removed", TextComponent.of(removed)));
        return removed;
    }

    private int killMatchingEntities(Integer radius, Actor actor, Supplier<EntityFunction> func) throws IncompleteRegionException,
            MaxChangedBlocksException {
        List<EntityVisitor> visitors = new ArrayList<>();

        LocalSession session = we.getSessionManager().get(actor);
        BlockVector3 center = session.getPlacementPosition(actor);
        EditSession editSession = session.createEditSession(actor);
        List<? extends Entity> entities;
        if (radius >= 0) {
            CylinderRegion region = CylinderRegion.createRadius(editSession, center, radius);
            entities = editSession.getEntities(region);
        } else {
            entities = editSession.getEntities();
        }
        visitors.add(new EntityVisitor(entities.iterator(), func.get()));

        int killed = 0;
        for (EntityVisitor visitor : visitors) {
            Operations.completeLegacy(visitor);
            killed += visitor.getAffected();
        }

        session.remember(editSession);
        editSession.close();
        return killed;
    }

    private DecimalFormat formatForLocale(Locale locale) {
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(locale);
        format.applyPattern("#,##0.#####");
        return format;
    }

    @Command(
        name = "/calculate",
        aliases = { "/calc", "/eval", "/evaluate", "/solve" },
        desc = "Evaluate a mathematical expression"
    )
    @CommandPermissions("worldedit.calc")
    public void calc(Actor actor,
                     @Arg(desc = "Expression to evaluate", variable = true)
                         List<String> input) {
        Expression expression;
        try {
            expression = Expression.compile(String.join(" ", input));
        } catch (ExpressionException e) {
            actor.printError(TranslatableComponent.of(
                "worldedit.calc.invalid.with-error",
                TextComponent.of(String.join(" ", input)),
                TextComponent.of(e.getMessage())
            ));
            return;
        }
        WorldEditAsyncCommandBuilder.createAndSendMessage(actor, () -> {
            double result = expression.evaluate(
                    new double[]{}, WorldEdit.getInstance().getSessionManager().get(actor).getTimeout());
            String formatted = Double.isNaN(result) ? "NaN" : formatForLocale(actor.getLocale()).format(result);
            return SubtleFormat.wrap(input + " = ").append(TextComponent.of(formatted, TextColor.LIGHT_PURPLE));
        }, (Component) null);
    }

    @Command(
        name = "/help",
        desc = "Displays help for WorldEdit commands"
    )
    @CommandPermissions("worldedit.help")
    public void help(Actor actor,
                     @Switch(name = 's', desc = "List sub-commands of the given command, if applicable")
                         boolean listSubCommands,
                     @ArgFlag(name = 'p', desc = "The page to retrieve", def = "1")
                         int page,
                     @Arg(desc = "The command to retrieve help for", def = "", variable = true)
                         List<String> command) throws WorldEditException {
        PrintCommandHelp.help(command, page, listSubCommands,
                we.getPlatformManager().getPlatformCommandManager().getCommandManager(), actor, "//help");
    }

}
