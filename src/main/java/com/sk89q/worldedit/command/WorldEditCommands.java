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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class WorldEditCommands {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    
    private final WorldEdit we;
    
    public WorldEditCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "version", "ver" },
        usage = "",
        desc = "Get WorldEdit version",
        min = 0,
        max = 0
    )
    @Console
    public void version(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        player.print("WorldEdit version " + WorldEdit.getVersion());
        player.print("https://github.com/sk89q/worldedit/");

        PlatformManager pm = we.getPlatformManager();
        Platform primary = pm.getPrimaryPlatform();

        player.printDebug("");
        player.printDebug("Platforms:");
        for (Platform platform : pm.getPlatforms()) {
            String prefix = "";

            if (primary != null && primary.equals(platform)) {
                prefix = "[PRIMARY] ";
            }

            player.printDebug(String.format("- %s%s v%s (WE v%s)",
                    prefix, platform.getPlatformName(), platform.getPlatformVersion(), platform.getVersion()));
        }
    }

    @Command(
        aliases = { "reload" },
        usage = "",
        desc = "Reload WorldEdit",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.reload")
    @Console
    public void reload(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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
    public void cui(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
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
    @Console
    public void tz(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        TimeZone tz = TimeZone.getTimeZone(args.getString(0));
        session.setTimezone(tz);
        player.print("Timezone set for this session to: " + tz.getDisplayName());
        player.print("The current time in that timezone is: "
                + dateFormat.format(Calendar.getInstance(tz).getTime()));
    }

    @Command(
        aliases = { "help" },
        usage = "[<command>]",
        desc = "Displays help for the given command or lists all commands.",
        min = 0,
        max = -1
    )
    @CommandPermissions("worldedit.help")
    @Console
    public void help(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        UtilityCommands.help(args, we, session, player, editSession);
    }
}
