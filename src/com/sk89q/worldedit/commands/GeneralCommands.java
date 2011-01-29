// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.*;

/**
 * General WorldEdit commands.
 * 
 * @author sk89q
 */
public class GeneralCommands {
    @Command(
        aliases = {"/limit"},
        usage = "<limit>",
        desc = "Modify block change limit",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.limit"})
    public static void limit(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        int limit = Math.max(-1, args.getInteger(0));
        if (!player.hasPermission("worldedit.limit.unrestricted")
                && config.maxChangeLimit > -1) {
            if (limit > config.maxChangeLimit) {
                player.printError("Your maximum allowable limit is "
                        + config.maxChangeLimit + ".");
                return;
            }
        }
        
        session.setBlockChangeLimit(limit);
        player.print("Block change limit set to " + limit + ".");
    }

    @Command(
        aliases = {"toggleplace"},
        usage = "",
        desc = "",
        min = 0,
        max = 0
    )
    public static void togglePlace(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        if (session.togglePlacementPosition()) {
            player.print("Now placing at pos #1.");
        } else {
            player.print("Now placing at the block you stand in.");
        }
    }
}
