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

import com.google.common.io.Files;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.PrintCommandHelp;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldedit.util.report.ConfigReport;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.zone.ZoneRulesException;
import java.util.List;
import java.util.Locale;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class WorldEditCommands {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final WorldEdit we;

    public WorldEditCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        name = "version",
        aliases = { "ver" },
        desc = "Get WorldEdit version"
    )
    public void version(Actor actor) {
        actor.print("WorldEdit version " + WorldEdit.getVersion());
        actor.print("https://github.com/EngineHub/worldedit/");

        PlatformManager pm = we.getPlatformManager();

        actor.printDebug("----------- Platforms -----------");
        for (Platform platform : pm.getPlatforms()) {
            actor.printDebug(String.format("* %s (%s)", platform.getPlatformName(), platform.getPlatformVersion()));
        }

        actor.printDebug("----------- Capabilities -----------");
        for (Capability capability : Capability.values()) {
            Platform platform = pm.queryCapability(capability);
            actor.printDebug(String.format("%s: %s", capability.name(), platform != null ? platform.getPlatformName() : "NONE"));
        }
    }

    @Command(
        name = "reload",
        desc = "Reload configuration"
    )
    @CommandPermissions("worldedit.reload")
    public void reload(Actor actor) {
        we.getPlatformManager().queryCapability(Capability.CONFIGURATION).reload();
        we.getEventBus().post(new ConfigurationLoadEvent(we.getPlatformManager().queryCapability(Capability.CONFIGURATION).getConfiguration()));
        actor.print("Configuration reloaded!");
    }

    @Command(
        name = "report",
        desc = "Writes a report on WorldEdit"
    )
    @CommandPermissions("worldedit.report")
    public void report(Actor actor,
                       @Switch(name = 'p', desc = "Pastebins the report")
                           boolean pastebin) throws WorldEditException {
        ReportList report = new ReportList("Report");
        report.add(new SystemInfoReport());
        report.add(new ConfigReport());
        String result = report.toString();

        try {
            File dest = new File(we.getConfiguration().getWorkingDirectory(), "report.txt");
            Files.write(result, dest, Charset.forName("UTF-8"));
            actor.print("WorldEdit report written to " + dest.getAbsolutePath());
        } catch (IOException e) {
            actor.printError("Failed to write report: " + e.getMessage());
        }

        if (pastebin) {
            actor.checkPermission("worldedit.report.pastebin");
            ActorCallbackPaste.pastebin(we.getSupervisor(), actor, result, "WorldEdit report: %s.report");
        }
    }

    @Command(
        name = "cui",
        desc = "Complete CUI handshake (internal usage)"
    )
    public void cui(Player player, LocalSession session) {
        session.setCUISupport(true);
        session.dispatchCUISetup(player);
    }

    @Command(
        name = "tz",
        desc = "Set your timezone for snapshots"
    )
    public void tz(Player player, LocalSession session,
                   @Arg(desc = "The timezone to set")
                       String timezone) {
        try {
            ZoneId tz = ZoneId.of(timezone);
            session.setTimezone(tz);
            player.print("Timezone set for this session to: " + tz.getDisplayName(
                    TextStyle.FULL, Locale.ENGLISH
            ));
            player.print("The current time in that timezone is: " + dateFormat.format(ZonedDateTime.now(tz)));
        } catch (ZoneRulesException e) {
            player.printError("Invalid timezone");
        }
    }

    @Command(
        name = "help",
        desc = "Displays help for WorldEdit commands"
    )
    @CommandPermissions("worldedit.help")
    public void help(Actor actor,
                     @Switch(name = 's', desc = "List sub-commands of the given command, if applicable")
                         boolean listSubCommands,
                     @ArgFlag(name = 'p', desc = "The page to retrieve", def = "1")
                         int page,
                     @Arg(desc = "The command to retrieve help for", def = "", variable = true)
                         List<String> command) throws WorldEditException {
        PrintCommandHelp.help(command, page, listSubCommands,
                we.getPlatformManager().getPlatformCommandManager().getCommandManager(), actor, "/worldedit help");
    }
}
