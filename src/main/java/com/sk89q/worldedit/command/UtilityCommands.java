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

import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.LocalWorld.KillFlags;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
        min = 2,
        max = 3
    )
    @CommandPermissions("worldedit.fill")
    @Logging(PLACEMENT)
    public void fill(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        double radius = Math.max(1, args.getDouble(1));
        we.checkMaxRadius(radius);
        int depth = args.argsLength() > 2 ? Math.max(1, args.getInteger(2)) : 1;

        Vector pos = session.getPlacementPosition(player);
        int affected = 0;
        if (pattern instanceof SingleBlockPattern) {
            affected = editSession.fillXZ(pos,
                    ((SingleBlockPattern) pattern).getBlock(),
                    radius, depth, false);
        } else {
            affected = editSession.fillXZ(pos, pattern, radius, depth, false);
        }
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
    public void fillr(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        double radius = Math.max(1, args.getDouble(1));
        we.checkMaxRadius(radius);
        int depth = args.argsLength() > 2 ? Math.max(1, args.getInteger(2)) : Integer.MAX_VALUE;

        Vector pos = session.getPlacementPosition(player);
        int affected = 0;
        if (pattern instanceof SingleBlockPattern) {
            affected = editSession.fillXZ(pos,
                    ((SingleBlockPattern) pattern).getBlock(),
                    radius, depth, true);
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
    public void drain(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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
    public void fixLava(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        double radius = Math.max(0, args.getDouble(0));
        we.checkMaxRadius(radius);
        int affected = editSession.fixLiquid(
                session.getPlacementPosition(player), radius, 10, 11);
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
    public void fixWater(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        double radius = Math.max(0, args.getDouble(0));
        we.checkMaxRadius(radius);
        int affected = editSession.fixLiquid(
                session.getPlacementPosition(player), radius, 8, 9);
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
    public void removeAbove(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
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
    public void removeBelow(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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
    public void removeNear(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0), true);
        int size = Math.max(1, args.getInteger(1, 50));
        we.checkMaxRadius(size);

        int affected = editSession.removeNear(session.getPlacementPosition(player), block.getType(), size);
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
    public void replaceNear(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
        int size = Math.max(1, args.getInteger(0));
        int affected;
        Set<BaseBlock> from;
        Pattern to;
        if (args.argsLength() == 2) {
            from = null;
            to = we.getBlockPattern(player, args.getString(1));
        } else {
            from = we.getBlocks(player, args.getString(1), true, !args.hasFlag('f'));
            to = we.getBlockPattern(player, args.getString(2));
        }

        Vector base = session.getPlacementPosition(player);
        Vector min = base.subtract(size, size, size);
        Vector max = base.add(size, size, size);
        Region region = new CuboidRegion(player.getWorld(), min, max);

        if (to instanceof SingleBlockPattern) {
            affected = editSession.replaceBlocks(region, from, ((SingleBlockPattern) to).getBlock());
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
    public void snow(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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
    public void thaw(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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
    public void green(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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
    public void extinguish(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        int defaultRadius = config.maxRadius != -1 ? Math.min(40, config.maxRadius) : 40;
        int size = args.argsLength() > 0 ? Math.max(1, args.getInteger(0))
                : defaultRadius;
        we.checkMaxRadius(size);

        int affected = editSession.removeNear(session.getPlacementPosition(player), 51, size);
        player.print(affected + " block(s) have been removed.");
    }

    @Command(
        aliases = { "butcher" },
        usage = "[radius]",
        flags = "plangbf",
        desc = "Kill all or nearby mobs",
        help =
            "Kills nearby mobs, based on radius, if none is given uses default in configuration.\n" +
            "Flags:" +
            "  -p also kills pets.\n" +
            "  -n also kills NPCs.\n" +
            "  -g also kills Golems.\n" +
            "  -a also kills animals.\n" +
            "  -b also kills ambient mobs.\n" +
            "  -f compounds all previous flags.\n" +
            "  -l strikes lightning on each killed mob.",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.butcher")
    @Logging(PLACEMENT)
    @Console
    public void butcher(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

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

        FlagContainer flags = new FlagContainer(player);
        flags.or(KillFlags.FRIENDLY      , args.hasFlag('f')); // No permission check here. Flags will instead be filtered by the subsequent calls.
        flags.or(KillFlags.PETS          , args.hasFlag('p'), "worldedit.butcher.pets");
        flags.or(KillFlags.NPCS          , args.hasFlag('n'), "worldedit.butcher.npcs");
        flags.or(KillFlags.GOLEMS        , args.hasFlag('g'), "worldedit.butcher.golems");
        flags.or(KillFlags.ANIMALS       , args.hasFlag('a'), "worldedit.butcher.animals");
        flags.or(KillFlags.AMBIENT       , args.hasFlag('b'), "worldedit.butcher.ambient");
        flags.or(KillFlags.WITH_LIGHTNING, args.hasFlag('l'), "worldedit.butcher.lightning");
        // If you add flags here, please add them to com.sk89q.worldedit.commands.BrushCommands.butcherBrush() as well

        int killed;
        if (player.isPlayer()) {
            killed = player.getWorld().killMobs(session.getPlacementPosition(player), radius, flags.flags);
        } else {
            killed = 0;
            for (World world : we.getServer().getWorlds()) {
                killed += world.killMobs(new Vector(), radius, flags.flags);
            }
        }

        if (radius < 0) {
            player.print("Killed " + killed + " mobs.");
        } else {
            player.print("Killed " + killed + " mobs in a radius of " + radius + ".");
        }
    }

    public static class FlagContainer {
        private final LocalPlayer player;
        public int flags = 0;
        public FlagContainer(LocalPlayer player) {
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
    @Console
    public void remove(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        String typeStr = args.getString(0);
        int radius = args.getInteger(1);

        if (radius < -1) {
            player.printError("Use -1 to remove all entities in loaded chunks");
            return;
        }

        EntityType type = null;

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
            player.printError("Acceptable types: projectiles, items, paintings, itemframes, boats, minecarts, tnt, xp, or all");
            return;
        }

        int removed = 0;
        if (player.isPlayer()) {
            Vector origin = session.getPlacementPosition(player);
            removed = player.getWorld().removeEntities(type, origin, radius);
        } else {
            for (World world : we.getServer().getWorlds()) {
                removed += world.removeEntities(type, new Vector(), radius);
            }
        }
        player.print("Marked " + removed + " entit(ies) for removal.");
    }

    @Command(
        aliases = { "/help" },
        usage = "[<command>]",
        desc = "Displays help for the given command or lists all commands.",
        min = 0,
        max = -1
    )
    @Console
    @CommandPermissions("worldedit.help")
    public void help(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        help(args, we, session, player, editSession);
    }

    public static void help(CommandContext args, WorldEdit we, LocalSession session, LocalPlayer player, EditSession editSession) {
        final CommandsManager<LocalPlayer> commandsManager = we.getCommandsManager();

        if (args.argsLength() == 0) {
            SortedSet<String> commands = new TreeSet<String>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    final int ret = o1.replaceAll("/", "").compareToIgnoreCase(o2.replaceAll("/", ""));
                    if (ret == 0) {
                        return o1.compareToIgnoreCase(o2);
                    }
                    return ret;
                }
            });
            commands.addAll(commandsManager.getCommands().keySet());

            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String command : commands) {
                if (!first) {
                    sb.append(", ");
                }

                sb.append('/');
                sb.append(command);
                first = false;
            }

            player.print(sb.toString());

            return;
        }

        String command = args.getJoinedStrings(0).toLowerCase().replaceAll("/", "");

        String helpMessage = commandsManager.getHelpMessages().get(command);
        if (helpMessage == null) {
            player.printError("Unknown command '" + command + "'.");
            return;
        }

        player.print(helpMessage);
    }
}
