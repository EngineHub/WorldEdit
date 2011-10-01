// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;

public class WorldEditCommands {
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    
    @Command(
        aliases = { "version", "ver" },
        usage = "",
        desc = "Get WorldEdit version",
        min = 0,
        max = 0
    )
    public static void version(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        player.print("WorldEdit version " + WorldEdit.getVersion());
        player.print("http://www.sk89q.com/projects/worldedit/");
    }

    @Command(
        aliases = { "reload" },
        usage = "",
        desc = "Reload WorldEdit",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.reload")
    public static void reload(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        we.getServer().reload();
        player.print("Configuration reloaded!");
    }

    @Command(
        aliases = { "cui" },
        usage = "",
        desc = "Complete CUI handshake",
        min = 0,
        max = 0
    )
    public static void cui(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        session.setCUISupport(true);
        session.dispatchCUISetup(player);
    }

    @Command(
        aliases = { "tz" },
        usage = "[timezone]",
        desc = "Set your timezone",
        min = 1,
        max = 1
    )
    public static void tz(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        TimeZone tz = TimeZone.getTimeZone(args.getString(0));
        session.setTimezone(tz);
        player.print("Timezone set for this session to: " + tz.getDisplayName());
        player.print("The current time in that timezone is: "
                + dateFormat.format(Calendar.getInstance(tz).getTime()));
    }
}
