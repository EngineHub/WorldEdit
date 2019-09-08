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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.command.util.Logging.LogMode.POSITION;

/**
 * Commands for moving the player around.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class NavigationCommands {

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
        name = "unstuck",
        aliases = { "!" },
        desc = "Escape from being stuck inside a block"
    )
    @CommandPermissions("worldedit.navigation.unstuck")
    public void unstuck(Player player) throws WorldEditException {
        player.findFreePosition();
        player.print("There you go!");
    }

    @Command(
        name = "ascend",
        aliases = { "asc" },
        desc = "Go up a floor"
    )
    @CommandPermissions("worldedit.navigation.ascend")
    public void ascend(Player player,
                       @Arg(desc = "# of levels to ascend", def = "1")
                           int levels) throws WorldEditException {
        int ascentLevels = 0;
        while (player.ascendLevel()) {
            ++ascentLevels;
            if (levels == ascentLevels) {
                break;
            }
        }
        if (ascentLevels == 0) {
            player.printError("No free spot above you found.");
        } else {
            player.print((ascentLevels != 1) ? "Ascended " + ascentLevels + " levels." : "Ascended a level.");
        }
    }

    @Command(
        name = "descend",
        aliases = { "desc" },
        desc = "Go down a floor"
    )
    @CommandPermissions("worldedit.navigation.descend")
    public void descend(Player player,
                        @Arg(desc = "# of levels to descend", def = "1")
                            int levels) throws WorldEditException {
        int descentLevels = 0;
        while (player.descendLevel()) {
            ++descentLevels;
            if (levels == descentLevels) {
                break;
            }
        }
        if (descentLevels == 0) {
            player.printError("No free spot below you found.");
        } else {
            player.print((descentLevels != 1) ? "Descended " + descentLevels + " levels." : "Descended a level.");
        }
    }

    @Command(
        name = "ceil",
        desc = "Go to the ceiling"
    )
    @CommandPermissions("worldedit.navigation.ceiling")
    @Logging(POSITION)
    public void ceiling(Player player,
                        @Arg(desc = "# of blocks to leave above you", def = "0")
                            int clearance,
                        @Switch(name = 'f', desc = "Force using flight to keep you still")
                            boolean forceFlight,
                        @Switch(name = 'g', desc = "Force using glass to keep you still")
                            boolean forceGlass) throws WorldEditException {
        clearance = Math.max(0, clearance);

        boolean alwaysGlass = getAlwaysGlass(forceFlight, forceGlass);
        if (player.ascendToCeiling(clearance, alwaysGlass)) {
            player.print("Whoosh!");
        } else {
            player.printError("No free spot above you found.");
        }
    }

    @Command(
        name = "thru",
        desc = "Pass through walls"
    )
    @CommandPermissions("worldedit.navigation.thru.command")
    public void thru(Player player) throws WorldEditException {
        if (player.passThroughForwardWall(6)) {
            player.print("Whoosh!");
        } else {
            player.printError("No free spot ahead of you found.");
        }
    }

    @Command(
        name = "jumpto",
        aliases = { "j" },
        desc = "Teleport to a location"
    )
    @CommandPermissions("worldedit.navigation.jumpto.command")
    public void jumpTo(Player player) throws WorldEditException {

        Location pos = player.getSolidBlockTrace(300);
        if (pos != null) {
            player.findFreePosition(pos);
            player.print("Poof!");
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        name = "up",
        desc = "Go upwards some distance"
    )
    @CommandPermissions("worldedit.navigation.up")
    @Logging(POSITION)
    public void up(Player player,
                   @Arg(desc = "Distance to go upwards")
                       int distance,
                   @Switch(name = 'f', desc = "Force using flight to keep you still")
                       boolean forceFlight,
                   @Switch(name = 'g', desc = "Force using glass to keep you still")
                       boolean forceGlass) throws WorldEditException {
        boolean alwaysGlass = getAlwaysGlass(forceFlight, forceGlass);
        if (player.ascendUpwards(distance, alwaysGlass)) {
            player.print("Whoosh!");
        } else {
            player.printError("You would hit something above you.");
        }
    }

    /**
     * Helper function for /up and /ceil.
     *
     * @param forceFlight if flight should be used, rather than the default config option
     * @param forceGlass if glass should always be placed, rather than the default config option
     * @return true, if glass should always be put under the player
     */
    private boolean getAlwaysGlass(boolean forceFlight, boolean forceGlass) {
        final LocalConfiguration config = worldEdit.getConfiguration();

        return forceGlass || (config.navigationUseGlass && !forceFlight);
    }
}
