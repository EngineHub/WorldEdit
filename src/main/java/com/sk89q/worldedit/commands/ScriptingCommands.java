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

import java.io.File;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;
import com.sk89q.worldedit.*;

/**
 * Scripting commands.
 * 
 * @author sk89q
 */
public class ScriptingCommands {
    @Command(
        aliases = { "cs" },
        usage = "<filename> [args...]",
        desc = "Execute a CraftScript",
        min = 1,
        max = -1
    )
    @CommandPermissions("worldedit.scripting.execute")
    @Logging(ALL)
    public static void execute(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        String[] scriptArgs = args.getSlice(1);
        String name = args.getString(0);
        
        if (!player.hasPermission("worldedit.scripting.execute." + name)) {
            player.printError("You don't have permission to use that script.");
            return;
        }

        session.setLastScript(name);

        File dir = we.getWorkingDirectoryFile(we.getConfiguration().scriptsDir);
        File f = we.getSafeOpenFile(player, dir, name, "js",
                new String[] {"js"});
        
        we.runScript(player, f, scriptArgs);
    }

    @Command(
        aliases = { ".s" },
        usage = "[args...]",
        desc = "Execute last CraftScript",
        min = 0,
        max = -1
    )
    @CommandPermissions("worldedit.scripting.execute")
    @Logging(ALL)
    public static void executeLast(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        String lastScript = session.getLastScript();
        
        if (!player.hasPermission("worldedit.scripting.execute." + lastScript)) {
            player.printError("You don't have permission to use that script.");
            return;
        }
        
        if (lastScript == null) {
            player.printError("Use /cs with a script name first.");
            return;
        }

        String[] scriptArgs = args.getSlice(0);

        File dir = we.getWorkingDirectoryFile(we.getConfiguration().scriptsDir);
        File f = we.getSafeOpenFile(player, dir, lastScript, "js",
                new String[] {"js"});
        
        we.runScript(player, f, scriptArgs);
    }
}
