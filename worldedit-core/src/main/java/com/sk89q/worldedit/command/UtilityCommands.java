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

import static com.sk89q.minecraft.util.commands.Logging.LogMode.PLACEMENT;

import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CreatureButcher;
import com.sk89q.worldedit.command.util.EntityRemover;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.EntityVisitor;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.PrimaryAliasComparator;
import com.sk89q.worldedit.util.command.binding.Text;
import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.formatting.StyledFragment;
import com.sk89q.worldedit.util.formatting.component.Code;
import com.sk89q.worldedit.util.formatting.component.CommandListBox;
import com.sk89q.worldedit.util.formatting.component.CommandUsageBox;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility commands.
 */
public class UtilityCommands {

    private final WorldEdit we;

    public UtilityCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "/fill" },
        usage = "<block> <radius> [depth]",
        desc = "Fill a hole",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.fill")
    @Logging(PLACEMENT)
    public void fill(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        Pattern pattern = we.getPatternFactory().parseFromInput(args.getString(0), context);

        double radius = Math.max(1, args.getDouble(1));
        we.checkMaxRadius(radius);
        int depth = args.argsLength() > 2 ? Math.max(1, args.getInteger(2)) : 1;

        Vector pos = session.getPlacementPosition(player);
        int affected = editSession.fillXZ(pos, pattern, radius, depth, false);
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = { "/fillr" },
        usage = "<block> <radius> [depth]",
        desc = "Fill a hole recursively",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.fill.recursive")
    @Logging(PLACEMENT)
    public void fillr(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        Pattern pattern = we.getPatternFactory().parseFromInput(args.getString(0), context);

        double radius = Math.max(1, args.getDouble(1));
        we.checkMaxRadius(radius);
        int depth = args.argsLength() > 2 ? Math.max(1, args.getInteger(2)) : Integer.MAX_VALUE;

        Vector pos = session.getPlacementPosition(player);
        int affected = 0;
        if (pattern instanceof BlockPattern) {
            affected = editSession.fillXZ(pos, ((BlockPattern) pattern).getBlock(), radius, depth, true);
        } else {
            affected = editSession.fillXZ(pos, pattern, radius, depth, true);
        }
        player.print(affected + " block(s) have been created.");
    }

    @Command(
        aliases = { "/drain" },
        usage = "<radius>",
        desc = "Drain a pool",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.drain")
    @Logging(PLACEMENT)
    public void drain(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        double radius = Math.max(0, args.getDouble(0));
        we.checkMaxRadius(radius);
        int affected = editSession.drainArea(
                session.getPlacementPosition(player), radius);
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = { "/fixlava", "fixlava" },
        usage = "<radius>",
        desc = "Fix lava to be stationary",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.fixlava")
    @Logging(PLACEMENT)
    public void fixLava(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        double radius = Math.max(0, args.getDouble(0));
        we.checkMaxRadius(radius);
        int affected = editSession.fixLiquid(session.getPlacementPosition(player), radius, BlockTypes.LAVA);
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = { "/fixwater", "fixwater" },
        usage = "<radius>",
        desc = "Fix water to be stationary",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.fixwater")
    @Logging(PLACEMENT)
    public void fixWater(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        double radius = Math.max(0, args.getDouble(0));
        we.checkMaxRadius(radius);
        int affected = editSession.fixLiquid(session.getPlacementPosition(player), radius, BlockTypes.WATER);
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = { "/removeabove", "removeabove" },
        usage = "[size] [height]",
        desc = "Remove blocks above your head.",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.removeabove")
    @Logging(PLACEMENT)
    public void removeAbove(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        
        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        we.checkMaxRadius(size);
        World world = player.getWorld();
        int height = args.argsLength() > 1 ? Math.min((world.getMaxY() + 1), args.getInteger(1) + 2) : (world.getMaxY() + 1);

        int affected = editSession.removeAbove(
                session.getPlacementPosition(player), size, height);
        player.print(affected + " block(s) have been removed.");
    }

    @Command(
        aliases = { "/removebelow", "removebelow" },
        usage = "[size] [height]",
        desc = "Remove blocks below you.",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.removebelow")
    @Logging(PLACEMENT)
    public void removeBelow(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        we.checkMaxRadius(size);
        World world = player.getWorld();
        int height = args.argsLength() > 1 ? Math.min((world.getMaxY() + 1), args.getInteger(1) + 2) : (world.getMaxY() + 1);

        int affected = editSession.removeBelow(session.getPlacementPosition(player), size, height);
        player.print(affected + " block(s) have been removed.");
    }

    @Command(
        aliases = { "/removenear", "removenear" },
        usage = "<block> [size]",
        desc = "Remove blocks near you.",
        min = 1,
        max = 2
    )
    @CommandPermissions("worldedit.removenear")
    @Logging(PLACEMENT)
    public void removeNear(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(false);
        context.setPreferringWildcard(false);

        BlockStateHolder block = we.getBlockFactory().parseFromInput(args.getString(0), context);
        int size = Math.max(1, args.getInteger(1, 50));
        we.checkMaxRadius(size);

        int affected = editSession.removeNear(session.getPlacementPosition(player), block.getBlockType(), size);
        player.print(affected + " block(s) have been removed.");
    }

    @Command(
        aliases = { "/replacenear", "replacenear" },
        usage = "<size> <from-id> <to-id>",
        desc = "Replace nearby blocks",
        flags = "f",
        min = 3,
        max = 3
    )
    @CommandPermissions("worldedit.replacenear")
    @Logging(PLACEMENT)
    public void replaceNear(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        
        int size = Math.max(1, args.getInteger(0));
        int affected;
        Set<BlockStateHolder> from;
        Pattern to;

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(false);
        context.setPreferringWildcard(!args.hasFlag('f'));

        if (args.argsLength() == 2) {
            from = null;
            to = we.getPatternFactory().parseFromInput(args.getString(1), context);
        } else {
            from = we.getBlockFactory().parseFromListInput(args.getString(1), context);
            to = we.getPatternFactory().parseFromInput(args.getString(2), context);
        }

        Vector base = session.getPlacementPosition(player);
        Vector min = base.subtract(size, size, size);
        Vector max = base.add(size, size, size);
        Region region = new CuboidRegion(player.getWorld(), min, max);

        if (to instanceof BlockPattern) {
            affected = editSession.replaceBlocks(region, from, ((BlockPattern) to).getBlock());
        } else {
            affected = editSession.replaceBlocks(region, from, to);
        }
        player.print(affected + " block(s) have been replaced.");
    }

    @Command(
        aliases = { "/snow", "snow" },
        usage = "[radius]",
        desc = "Simulates snow",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.snow")
    @Logging(PLACEMENT)
    public void snow(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        double size = args.argsLength() > 0 ? Math.max(1, args.getDouble(0)) : 10;

        int affected = editSession.simulateSnow(session.getPlacementPosition(player), size);
        player.print(affected + " surfaces covered. Let it snow~");
    }

    @Command(
        aliases = {"/thaw", "thaw"},
        usage = "[radius]",
        desc = "Thaws the area",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.thaw")
    @Logging(PLACEMENT)
    public void thaw(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        double size = args.argsLength() > 0 ? Math.max(1, args.getDouble(0)) : 10;

        int affected = editSession.thaw(session.getPlacementPosition(player), size);
        player.print(affected + " surfaces thawed.");
    }

    @Command(
        aliases = { "/green", "green" },
        usage = "[radius]",
        desc = "Greens the area",
        flags = "f",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.green")
    @Logging(PLACEMENT)
    public void green(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        final double size = args.argsLength() > 0 ? Math.max(1, args.getDouble(0)) : 10;
        final boolean onlyNormalDirt = !args.hasFlag('f');

        final int affected = editSession.green(session.getPlacementPosition(player), size, onlyNormalDirt);
        player.print(affected + " surfaces greened.");
    }

    @Command(
            aliases = { "/ex", "/ext", "/extinguish", "ex", "ext", "extinguish" },
            usage = "[radius]",
            desc = "Extinguish nearby fire",
            min = 0,
            max = 1
        )
    @CommandPermissions("worldedit.extinguish")
    @Logging(PLACEMENT)
    public void extinguish(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        int defaultRadius = config.maxRadius != -1 ? Math.min(40, config.maxRadius) : 40;
        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0))
                : defaultRadius;
        we.checkMaxRadius(size);

        int affected = editSession.removeNear(session.getPlacementPosition(player), BlockTypes.FIRE, size);
        player.print(affected + " block(s) have been removed.");
    }

    @Command(
        aliases = { "butcher" },
        usage = "[radius]",
        flags = "plangbtfr",
        desc = "Kill all or nearby mobs",
        help =
            "Kills nearby mobs, based on radius, if none is given uses default in configuration.\n" +
            "Flags:\n" +
            "  -p also kills pets.\n" +
            "  -n also kills NPCs.\n" +
            "  -g also kills Golems.\n" +
            "  -a also kills animals.\n" +
            "  -b also kills ambient mobs.\n" +
            "  -t also kills mobs with name tags.\n" +
            "  -f compounds all previous flags.\n" +
            "  -r also destroys armor stands.\n" +
            "  -l currently does nothing.",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.butcher")
    @Logging(PLACEMENT)
    public void butcher(Actor actor, CommandContext args) throws WorldEditException {
        LocalConfiguration config = we.getConfiguration();
        Player player = actor instanceof Player ? (Player) actor : null;

        // technically the default can be larger than the max, but that's not my problem
        int radius = config.butcherDefaultRadius;

        // there might be a better way to do this but my brain is fried right now
        if (args.argsLength() > 0) { // user inputted radius, override the default
            radius = args.getInteger(0);
            if (radius < -1) {
                actor.printError("Use -1 to remove all mobs in loaded chunks");
                return;
            }
            if (config.butcherMaxRadius != -1) { // clamp if there is a max
                if (radius == -1) {
                    radius = config.butcherMaxRadius;
                } else { // Math.min does not work if radius is -1 (actually highest possible value)
                    radius = Math.min(radius, config.butcherMaxRadius);
                }
            }
        }

        CreatureButcher flags = new CreatureButcher(actor);
        flags.fromCommand(args);

        List<EntityVisitor> visitors = new ArrayList<>();
        LocalSession session = null;
        EditSession editSession = null;

        if (player != null) {
            session = we.getSessionManager().get(player);
            Vector center = session.getPlacementPosition(player);
            editSession = session.createEditSession(player);
            List<? extends Entity> entities;
            if (radius >= 0) {
                CylinderRegion region = CylinderRegion.createRadius(editSession, center, radius);
                entities = editSession.getEntities(region);
            } else {
                entities = editSession.getEntities();
            }
            visitors.add(new EntityVisitor(entities.iterator(), flags.createFunction()));
        } else {
            Platform platform = we.getPlatformManager().queryCapability(Capability.WORLD_EDITING);
            for (World world : platform.getWorlds()) {
                List<? extends Entity> entities = world.getEntities();
                visitors.add(new EntityVisitor(entities.iterator(), flags.createFunction()));
            }
        }

        int killed = 0;
        for (EntityVisitor visitor : visitors) {
            Operations.completeLegacy(visitor);
            killed += visitor.getAffected();
        }

        actor.print("Killed " + killed + (killed != 1 ? " mobs" : " mob") + (radius < 0 ? "" : " in a radius of " + radius) + ".");

        if (editSession != null) {
            session.remember(editSession);
            editSession.flushQueue();
        }
    }

    @Command(
        aliases = { "remove", "rem", "rement" },
        usage = "<type> <radius>",
        desc = "Remove all entities of a type",
        min = 2,
        max = 2
    )
    @CommandPermissions("worldedit.remove")
    @Logging(PLACEMENT)
    public void remove(Actor actor, CommandContext args) throws WorldEditException, CommandException {
        String typeStr = args.getString(0);
        int radius = args.getInteger(1);
        Player player = actor instanceof Player ? (Player) actor : null;

        if (radius < -1) {
            actor.printError("Use -1 to remove all entities in loaded chunks");
            return;
        }

        EntityRemover remover = new EntityRemover();
        remover.fromString(typeStr);

        List<EntityVisitor> visitors = new ArrayList<>();
        LocalSession session = null;
        EditSession editSession = null;

        if (player != null) {
            session = we.getSessionManager().get(player);
            Vector center = session.getPlacementPosition(player);
            editSession = session.createEditSession(player);
            List<? extends Entity> entities;
            if (radius >= 0) {
                CylinderRegion region = CylinderRegion.createRadius(editSession, center, radius);
                entities = editSession.getEntities(region);
            } else {
                entities = editSession.getEntities();
            }
            visitors.add(new EntityVisitor(entities.iterator(), remover.createFunction()));
        } else {
            Platform platform = we.getPlatformManager().queryCapability(Capability.WORLD_EDITING);
            for (World world : platform.getWorlds()) {
                List<? extends Entity> entities = world.getEntities();
                visitors.add(new EntityVisitor(entities.iterator(), remover.createFunction()));
            }
        }

        int removed = 0;
        for (EntityVisitor visitor : visitors) {
            Operations.completeLegacy(visitor);
            removed += visitor.getAffected();
        }

        actor.print("Marked " + removed + (removed != 1 ? " entities" : " entity") + " for removal.");

        if (editSession != null) {
            session.remember(editSession);
            editSession.flushQueue();
        }
    }

    @Command(
        aliases = { "/calc", "/calculate", "/eval", "/evaluate", "/solve" },
        usage = "<expression>",
        desc = "Evaluate a mathematical expression"
    )
    @CommandPermissions("worldedit.calc")
    public void calc(Actor actor, @Text String input) throws CommandException {
        try {
            Expression expression = Expression.compile(input);
            actor.print("= " + expression.evaluate());
        } catch (EvaluationException e) {
            actor.printError(String.format(
                    "'%s' could not be parsed as a valid expression", input));
        } catch (ExpressionException e) {
            actor.printError(String.format(
                    "'%s' could not be evaluated (error: %s)", input, e.getMessage()));
        }
    }

    @Command(
        aliases = { "/help" },
        usage = "[<command>]",
        desc = "Displays help for WorldEdit commands",
        min = 0,
        max = -1
    )
    @CommandPermissions("worldedit.help")
    public void help(Actor actor, CommandContext args) throws WorldEditException {
        help(args, we, actor);
    }

    private static CommandMapping detectCommand(Dispatcher dispatcher, String command, boolean isRootLevel) {
        CommandMapping mapping;

        // First try the command as entered
        mapping = dispatcher.get(command);
        if (mapping != null) {
            return mapping;
        }

        // Then if we're looking at root commands and the user didn't use
        // any slashes, let's try double slashes and then single slashes.
        // However, be aware that there exists different single slash
        // and double slash commands in WorldEdit
        if (isRootLevel && !command.contains("/")) {
            mapping = dispatcher.get("//" + command);
            if (mapping != null) {
                return mapping;
            }

            mapping = dispatcher.get("/" + command);
            if (mapping != null) {
                return mapping;
            }
        }

        return null;
    }

    public static void help(CommandContext args, WorldEdit we, Actor actor) {
        CommandCallable callable = we.getPlatformManager().getCommandManager().getDispatcher();

        int page = 0;
        final int perPage = actor instanceof Player ? 8 : 20; // More pages for console
        int effectiveLength = args.argsLength();

        // Detect page from args
        try {
            if (args.argsLength() > 0) {
                page = args.getInteger(args.argsLength() - 1);
                if (page <= 0) {
                    page = 1;
                } else {
                    page--;
                }

                effectiveLength--;
            }
        } catch (NumberFormatException ignored) {
        }

        boolean isRootLevel = true;
        List<String> visited = new ArrayList<>();

        // Drill down to the command
        for (int i = 0; i < effectiveLength; i++) {
            String command = args.getString(i);

            if (callable instanceof Dispatcher) {
                // Chop off the beginning / if we're are the root level
                if (isRootLevel && command.length() > 1 && command.charAt(0) == '/') {
                    command = command.substring(1);
                }

                CommandMapping mapping = detectCommand((Dispatcher) callable, command, isRootLevel);
                if (mapping != null) {
                    callable = mapping.getCallable();
                } else {
                    if (isRootLevel) {
                        actor.printError(String.format("The command '%s' could not be found.", args.getString(i)));
                        return;
                    } else {
                        actor.printError(String.format("The sub-command '%s' under '%s' could not be found.",
                                command, Joiner.on(" ").join(visited)));
                        return;
                    }
                }

                visited.add(args.getString(i));
                isRootLevel = false;
            } else {
                actor.printError(String.format("'%s' has no sub-commands. (Maybe '%s' is for a parameter?)",
                        Joiner.on(" ").join(visited), command));
                return;
            }
        }

        // Create the message
        if (callable instanceof Dispatcher) {
            Dispatcher dispatcher = (Dispatcher) callable;

            // Get a list of aliases
            List<CommandMapping> aliases = new ArrayList<>(dispatcher.getCommands());
            aliases.sort(new PrimaryAliasComparator(CommandManager.COMMAND_CLEAN_PATTERN));

            // Calculate pagination
            int offset = perPage * page;
            int pageTotal = (int) Math.ceil(aliases.size() / (double) perPage);

            // Box
            CommandListBox box = new CommandListBox(String.format("Help: page %d/%d ", page + 1, pageTotal));
            StyledFragment contents = box.getContents();
            StyledFragment tip = contents.createFragment(Style.GRAY);

            if (offset >= aliases.size()) {
                tip.createFragment(Style.RED).append(String.format("There is no page %d (total number of pages is %d).", page + 1, pageTotal)).newLine();
            } else {
                List<CommandMapping> list = aliases.subList(offset, Math.min(offset + perPage, aliases.size()));

                tip.append("Type ");
                tip.append(new Code().append("//help ").append("<command> [<page>]"));
                tip.append(" for more information.").newLine();

                // Add each command
                for (CommandMapping mapping : list) {
                    StringBuilder builder = new StringBuilder();
                    if (isRootLevel) {
                        builder.append("/");
                    }
                    if (!visited.isEmpty()) {
                        builder.append(Joiner.on(" ").join(visited));
                        builder.append(" ");
                    }
                    builder.append(mapping.getPrimaryAlias());
                    box.appendCommand(builder.toString(), mapping.getDescription().getDescription());
                }
            }

            actor.printRaw(ColorCodeBuilder.asColorCodes(box));
        } else {
            CommandUsageBox box = new CommandUsageBox(callable, Joiner.on(" ").join(visited));
            actor.printRaw(ColorCodeBuilder.asColorCodes(box));
        }
    }

}
