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
 * Scripting commands.
 * 
 * @author sk89q
 */
public class ScriptingCommands {
    @Command(
        aliases = {"cs"},
        usage = "<filename> [args...]",
        desc = "Execute a CraftScript",
        min = 1,
        max = -1
    )
    @CommandPermissions({"worldedit.scripting.execute"})
    public static void execute(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        // @TODO: Check for worldedit.scripting.execute.<script> permission

        String[] scriptArgs = args.getSlice(1);
        
        session.setLastScript(args.getString(0));
        
        we.runScript(player, args.getString(0), scriptArgs);
    }

    @Command(
        aliases = {".s"},
        usage = "[args...]",
        desc = "Execute last CraftScript",
        min = 0,
        max = -1
    )
    @CommandPermissions({"worldedit.scripting.execute"})
    public static void executeLast(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        // @TODO: Check for worldedit.scripting.execute.<script> permission
        
        String lastScript = session.getLastScript();
        
        if (lastScript == null) {
            player.printError("Use /cs with a script name first.");
            return;
        }

        String[] scriptArgs = args.getSlice(0);
        
        we.runScript(player, lastScript, scriptArgs);
        
    }
}
