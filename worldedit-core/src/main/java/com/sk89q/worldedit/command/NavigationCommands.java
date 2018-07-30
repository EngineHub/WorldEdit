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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.POSITION;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.command.parametric.Optional;

/**
 * Commands for moving the player around.
 */
public class NavigationCommands {

    @SuppressWarnings("unused")
    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public NavigationCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        aliases = { "unstuck", "!" },
        usage = "",
        desc = "Escape from being stuck inside a block",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.navigation.unstuck")
    public void unstuck(Player player) throws WorldEditException {
        player.print("There you go!");
        player.findFreePosition();
    }

    @Command(
        aliases = { "ascend", "asc" },
        usage = "[# of levels]",
        desc = "Go up a floor",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.navigation.ascend")
    public void ascend(Player player, @Optional("1") int levelsToAscend) throws WorldEditException {
        int ascentLevels = 0;
        while (player.ascendLevel()) {
            ++ascentLevels;
            if (levelsToAscend == ascentLevels) {
                break;
            }
        }
        if (ascentLevels == 0) {
            player.printError("No free spot above you found.");
        } else {
            player.print((ascentLevels != 1) ? "Ascended " + Integer.toString(ascentLevels) + " levels." : "Ascended a level.");
        }
    }

    @Command(
        aliases = { "descend", "desc" },
        usage = "[# of floors]",
        desc = "Go down a floor",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.navigation.descend")
    public void descend(Player player, @Optional("1") int levelsToDescend) throws WorldEditException {
        int descentLevels = 0;
        while (player.descendLevel()) {
            ++descentLevels;
            if (levelsToDescend == descentLevels) {
                break;
            }
        }
        if (descentLevels == 0) {
            player.printError("No free spot below you found.");
        } else {
            player.print((descentLevels != 1) ? "Descended " + Integer.toString(descentLevels) + " levels." : "Descended a level.");
        }
    }

    @Command(
        aliases = { "ceil" },
        usage = "[clearance]",
        desc = "Go to the celing",
        flags = "fg",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.navigation.ceiling")
    @Logging(POSITION)
    public void ceiling(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        final int clearance = args.argsLength() > 0 ?
            Math.max(0, args.getInteger(0)) : 0;

        final boolean alwaysGlass = getAlwaysGlass(args);
        if (player.ascendToCeiling(clearance, alwaysGlass)) {
            player.print("Whoosh!");
        } else {
            player.printError("No free spot above you found.");
        }
    }

    @Command(
        aliases = { "thru" },
        usage = "",
        desc = "Passthrough walls",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.navigation.thru.command")
    public void thru(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        if (player.passThroughForwardWall(6)) {
            player.print("Whoosh!");
        } else {
            player.printError("No free spot ahead of you found.");
        }
    }

    @Command(
        aliases = { "jumpto", "j" },
        usage = "",
        desc = "Teleport to a location",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.navigation.jumpto.command")
    public void jumpTo(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        Location pos = player.getSolidBlockTrace(300);
        if (pos != null) {
            player.findFreePosition(pos);
            player.print("Poof!");
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        aliases = { "up" },
        usage = "<block>",
        desc = "Go upwards some distance",
        flags = "fg",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.navigation.up")
    @Logging(POSITION)
    public void up(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        final int distance = args.getInteger(0);

        final boolean alwaysGlass = getAlwaysGlass(args);
        if (player.ascendUpwards(distance, alwaysGlass)) {
            player.print("Whoosh!");
        } else {
            player.printError("You would hit something above you.");
        }
    }

    /**
     * Helper function for /up and /ceil.
     * 
     * @param args The {@link CommandContext} to extract the flags from.
     * @return true, if glass should always be put under the player
     */
    private boolean getAlwaysGlass(CommandContext args) {
        final LocalConfiguration config = worldEdit.getConfiguration();

        final boolean forceFlight = args.hasFlag('f');
        final boolean forceGlass = args.hasFlag('g');

        return forceGlass || (config.navigationUseGlass && !forceFlight);
    }
}
