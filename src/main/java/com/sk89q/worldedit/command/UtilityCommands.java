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
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld.KillFlags;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.command.functions.CommandFutureUtils;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.function.CommonOperationFactory;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.CountingOperation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.PrimaryAliasComparator;
import com.sk89q.worldedit.util.command.binding.Range;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.binding.Text;
import com.sk89q.worldedit.util.command.parametric.Optional;
import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.formatting.StyledFragment;
import com.sk89q.worldedit.util.formatting.component.Code;
import com.sk89q.worldedit.util.formatting.component.CommandListBox;
import com.sk89q.worldedit.util.formatting.component.CommandUsageBox;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.PLACEMENT;

/**
 * Utility commands.
 * 
 * @author sk89q
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
        help = "" +
                "Flags:\n" +
                " -r - operate recursively (//fillr)",
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.fill")
    @Logging(PLACEMENT)
    public void fill(Player player, LocalSession session, EditSession editSession,
                     Pattern pattern,
                     @Range(min = 1) double radius,
                     @Optional("1") @Range(min = 1) int depth,
                     @Switch('r') boolean recursive) throws WorldEditException {
        we.checkMaxRadius(radius);

        Vector pos = session.getPlacementPosition(player);
        CountingOperation operation = CommonOperationFactory.fillXZ(editSession, pos, pattern, radius, depth, recursive);
        CommandFutureUtils.withCountPrinters(player, Operations.completeSlowly(editSession, operation));
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
    public void fillr(Player player, LocalSession session, EditSession editSession,
                      Pattern pattern,
                      @Range(min = 1) double radius,
                      @Optional("1") @Range(min = 1) int depth) throws WorldEditException {
        fill(player, session, editSession, pattern, radius, depth, true);
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
    public void drain(Player player, LocalSession session, EditSession editSession,
                      @Range(min = 0) double radius) throws WorldEditException {
        we.checkMaxRadius(radius);

        CommandFutureUtils.withCountPrinters(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.drainCommand(editSession,
                                session.getPlacementPosition(player), radius)));
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
    public void fixLava(Player player, LocalSession session, EditSession editSession,
                        @Range(min = 0) double radius) throws WorldEditException {
        we.checkMaxRadius(radius);

        CommandFutureUtils.withCountPrinters(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.fixLiquidCommand(editSession,
                                session.getPlacementPosition(player), radius,
                                BlockID.LAVA, BlockID.STATIONARY_LAVA)));
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
    public void fixWater(Player player, LocalSession session, EditSession editSession,
                         @Range(min = 0) double radius) throws WorldEditException {
        we.checkMaxRadius(radius);

        CommandFutureUtils.withCountPrinters(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.fixLiquidCommand(editSession,
                                session.getPlacementPosition(player), radius,
                                BlockID.WATER, BlockID.STATIONARY_WATER)));
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
    public void removeAbove(Player player, LocalSession session, EditSession editSession,
                            @Optional("1") @Range(min = 1) int size,
                            @Optional Integer height) throws WorldEditException {
        we.checkMaxRadius(size);
        World world = player.getWorld();
        int h = world.getMaxY();
        if (height != null) {
            h = Math.min(h, height + 1);
        }
        if (h < 2) {
            h = 2;
        }

        // --

        Vector position = session.getPlacementPosition(player);
        Region region = new CuboidRegion(
                world, // Causes clamping of Y range
                position.add(-size + 1, 0, -size + 1),
                position.add(size - 1, h, size - 1));
        Pattern pattern = new BlockPattern(new BaseBlock(BlockID.AIR));

        CommandFutureUtils.withCountPrinters(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.setBlocks(editSession, region, pattern)));
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
    public void removeBelow(Player player, LocalSession session, EditSession editSession,
                            @Optional("1") @Range(min = 1) int size,
                            @Optional Integer height) throws WorldEditException {
        we.checkMaxRadius(size);
        World world = player.getWorld();
        int h = world.getMaxY();
        if (height != null) {
            h = Math.min(h, height + 1);
        }
        if (h < 2) {
            h = 2;
        }

        // --

        Vector position = session.getPlacementPosition(player);
        Region region = new CuboidRegion(
                world, // Causes clamping of Y range
                position.add(-size + 1, 0, -size + 1),
                position.add(size - 1, -h, size - 1));
        Pattern pattern = new BlockPattern(new BaseBlock(BlockID.AIR));

        CommandFutureUtils.withCountPrinters(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.setBlocks(editSession, region, pattern)));
    }

    @Command(
        aliases = { "/removenear", "removenear" },
        usage = "<block> [size]",
        desc = "Remove all of a certain block near you.",
        min = 1,
        max = 2
    )
    @CommandPermissions("worldedit.removenear")
    @Logging(PLACEMENT)
    public void removeNear(Player player, LocalSession session, EditSession editSession,
                           Mask blockMask,
                           @Optional("50") @Range(min = 1) int size) throws WorldEditException {
        we.checkMaxRadius(size);

        Vector position = session.getPlacementPosition(player);
        int adj = size - 1;
        Region region = new CuboidRegion(
                player.getWorld(), // Causes clamping of Y range
                position.subtract(adj, adj, adj),
                position.add(adj, adj, adj));
        Pattern pattern = new BlockPattern(new BaseBlock(BlockID.AIR));

        CommandFutureUtils.withCountPrinters(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.replaceBlocks(editSession, region, blockMask, pattern)));
    }

    @Command(
        aliases = { "/replacenear", "replacenear" },
        usage = "<size> <from> <to>",
        desc = "Replace nearby blocks",
        flags = "f",
        min = 3,
        max = 3
    )
    @CommandPermissions("worldedit.replacenear")
    @Logging(PLACEMENT)
    public void replaceNear(Player player, LocalSession session, EditSession editSession,
                            @Range(min = 1) int size,
                            Mask blockMask,
                            Pattern pattern,
                            @Switch('f') boolean ignored) throws WorldEditException {
        we.checkMaxRadius(size);

        Vector position = session.getPlacementPosition(player);
        int adj = size - 1;
        Region region = new CuboidRegion(
                player.getWorld(), // Causes clamping of Y range
                position.subtract(adj, adj, adj),
                position.add(adj, adj, adj));

        CommandFutureUtils.withCountPrinters(player,
                Operations.completeSlowly(editSession,
                        CommonOperationFactory.replaceBlocks(editSession, region, blockMask, pattern)));
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
    public void snow(Player player, LocalSession session, EditSession editSession,
                     @Optional("10") @Range(min = 1) double size) throws WorldEditException {

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
    public void thaw(Player player, LocalSession session, EditSession editSession,
                     @Optional("10") @Range(min = 1) double size) throws WorldEditException {

        int affected = editSession.thaw(session.getPlacementPosition(player), size);
        player.print(affected + " surfaces thawed.");
    }

    @Command(
        aliases = { "/green", "green" },
        usage = "[radius]",
        desc = "Greens the area. -f: only normal dirt",
        flags = "f",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.green")
    @Logging(PLACEMENT)
    public void green(Player player, LocalSession session, EditSession editSession,
                      @Optional("10") @Range(min = 1) double size,
                      @Switch('f') boolean onlyNormalDirt) throws WorldEditException {

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
    public void extinguish(Player player, LocalSession session, EditSession editSession,
                           @Optional("40") @Range(min = 1) int size) throws WorldEditException {
        we.checkMaxRadius(size);

        removeNear(player, session, editSession, new BlockMask(editSession, new BaseBlock(BlockID.FIRE)), size);
    }

    @Command(
        aliases = { "butcher" },
        usage = "[radius]",
        flags = "plangbtf",
        desc = "Kill all or nearby mobs",
        help =
            "Kills nearby mobs, based on radius, if none is given uses default in configuration.\n" +
            "Flags:" +
            "  -p also kills pets.\n" +
            "  -n also kills NPCs.\n" +
            "  -g also kills Golems.\n" +
            "  -a also kills animals.\n" +
            "  -b also kills ambient mobs.\n" +
            "  -t also kills mobs with name tags.\n" +
            "  -f compounds all previous flags.\n" +
            "  -l strikes lightning on each killed mob.",
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
            if (config.butcherMaxRadius != -1) { // clamp if there is a max
                if (radius == -1) {
                    radius = config.butcherMaxRadius;
                } else { // Math.min does not work if radius is -1 (actually highest possible value)
                    radius = Math.min(radius, config.butcherMaxRadius);
                }
            }
        }

        FlagContainer flags = new FlagContainer(actor);
        flags.or(KillFlags.FRIENDLY      , args.hasFlag('f')); // No permission check here. Flags will instead be filtered by the subsequent calls.
        flags.or(KillFlags.PETS          , args.hasFlag('p'), "worldedit.butcher.pets");
        flags.or(KillFlags.NPCS          , args.hasFlag('n'), "worldedit.butcher.npcs");
        flags.or(KillFlags.GOLEMS        , args.hasFlag('g'), "worldedit.butcher.golems");
        flags.or(KillFlags.ANIMALS       , args.hasFlag('a'), "worldedit.butcher.animals");
        flags.or(KillFlags.AMBIENT       , args.hasFlag('b'), "worldedit.butcher.ambient");
        flags.or(KillFlags.TAGGED        , args.hasFlag('t'), "worldedit.butcher.tagged");
        flags.or(KillFlags.WITH_LIGHTNING, args.hasFlag('l'), "worldedit.butcher.lightning");
        // If you add flags here, please add them to com.sk89q.worldedit.commands.BrushCommands.butcherBrush() as well

        int killed;
        if (player != null) {
            LocalSession session = we.getSessionManager().get(player);
            killed = player.getWorld().killMobs(session.getPlacementPosition(player), radius, flags.flags);
        } else {
            killed = 0;
            for (World world : we.getServer().getWorlds()) {
                killed += world.killMobs(new Vector(), radius, flags.flags);
            }
        }

        if (radius < 0) {
            actor.print("Killed " + killed + " mobs.");
        } else {
            actor.print("Killed " + killed + " mobs in a radius of " + radius + ".");
        }
    }

    public static class FlagContainer {
        private final Actor player;
        public int flags = 0;
        public FlagContainer(Actor player) {
            this.player = player;
        }

        public void or(int flag, boolean on) {
            if (on) flags |= flag;
        }

        public void or(int flag, boolean on, String permission) {
            or(flag, on);

            if ((flags & flag) != 0 && !player.hasPermission(permission)) {
                flags &= ~flag;
            }
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
    public void remove(Actor actor, @Optional Player player, @Optional LocalSession session, CommandContext args) throws WorldEditException {

        String typeStr = args.getString(0);
        int radius = args.getInteger(1);

        if (radius < -1) {
            actor.printError("Use -1 to remove all entities in loaded chunks");
            return;
        }

        EntityType type;

        if (typeStr.matches("all")) {
            type = EntityType.ALL;
        } else if (typeStr.matches("projectiles?|arrows?")) {
            type = EntityType.PROJECTILES;
        } else if (typeStr.matches("items?")
                || typeStr.matches("drops?")) {
            type = EntityType.ITEMS;
        } else if (typeStr.matches("falling(blocks?|sand|gravel)")) {
            type = EntityType.FALLING_BLOCKS;
        } else if (typeStr.matches("paintings?")
                || typeStr.matches("art")) {
            type = EntityType.PAINTINGS;
        } else if (typeStr.matches("(item)frames?")) {
            type = EntityType.ITEM_FRAMES;
        } else if (typeStr.matches("boats?")) {
            type = EntityType.BOATS;
        } else if (typeStr.matches("minecarts?")
                || typeStr.matches("carts?")) {
            type = EntityType.MINECARTS;
        } else if (typeStr.matches("tnt")) {
            type = EntityType.TNT;
        } else if (typeStr.matches("xp")) {
            type = EntityType.XP_ORBS;
        } else {
            actor.printError("Acceptable types: projectiles, items, paintings, itemframes, boats, minecarts, tnt, xp, or all");
            return;
        }

        int removed = 0;
        if (player != null) {
            Vector origin = session.getPlacementPosition(player);
            removed = player.getWorld().removeEntities(type, origin, radius);
        } else {
            for (World world : we.getServer().getWorlds()) {
                removed += world.removeEntities(type, new Vector(), radius);
            }
        }
        actor.print("Marked " + removed + " entit(ies) for removal.");
    }

    @Command(
        aliases = { "/calc", "/calculate", "/eval", "/evaluate", "/solve" },
        usage = "<expression>",
        desc = "Evaluate a mathematical expression"
    )
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
        List<String> visited = new ArrayList<String>();

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
            List<CommandMapping> aliases = new ArrayList<CommandMapping>(dispatcher.getCommands());
            Collections.sort(aliases, new PrimaryAliasComparator(CommandManager.COMMAND_CLEAN_PATTERN));

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
                    box.appendCommand(builder.toString(), mapping.getDescription().getShortDescription());
                }
            }

            actor.printRaw(ColorCodeBuilder.asColorCodes(box));
        } else {
            CommandUsageBox box = new CommandUsageBox(callable, Joiner.on(" ").join(visited));
            actor.printRaw(ColorCodeBuilder.asColorCodes(box));
        }
    }
}
