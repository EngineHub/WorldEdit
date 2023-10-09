/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.HookMode;
import com.sk89q.worldedit.command.util.PrintCommandHelp;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.util.adventure.text.TextComponent;
import com.sk89q.worldedit.util.formatting.component.MessageBox;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.event.HoverEvent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldedit.util.paste.PasteMetadata;
import com.sk89q.worldedit.util.report.ConfigReport;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.zone.ZoneRulesException;
import java.util.List;

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
        actor.printInfo(Component.translatable("worldedit.version.version", Component.text(WorldEdit.getVersion())));
        actor.printInfo(Component.text("https://github.com/EngineHub/WorldEdit/"));

        PlatformManager pm = we.getPlatformManager();

        TextComponent.Builder producer = Component.text();
        for (Platform platform : pm.getPlatforms()) {
            producer.append(
                    Component.text("* ", NamedTextColor.GRAY)
                    .append(Component.text(platform.getPlatformName())
                        .hoverEvent(HoverEvent.showText(Component.text(platform.id()))))
                    .append(Component.text("(" + platform.getPlatformVersion() + ")"))
            ).append(Component.newline());
        }
        actor.print(new MessageBox("Platforms", producer.build(), NamedTextColor.GRAY).build());

        producer = Component.text();
        for (Capability capability : Capability.values()) {
            Platform platform = pm.queryCapability(capability);
            producer.append(
                    Component.text(capability.name(), NamedTextColor.GRAY)
                    .append(Component.text(": ")
                    .append(Component.text(platform != null ? platform.getPlatformName() : "none")))
            ).append(Component.newline());
        }
        actor.print(new MessageBox("Capabilities", producer.build(), NamedTextColor.GRAY).build());
    }

    @Command(
        name = "reload",
        desc = "Reload configuration"
    )
    @CommandPermissions("worldedit.reload")
    public void reload(Actor actor) {
        we.getPlatformManager().queryCapability(Capability.CONFIGURATION).reload();
        we.getEventBus().post(new ConfigurationLoadEvent(we.getPlatformManager().queryCapability(Capability.CONFIGURATION).getConfiguration()));
        actor.printInfo(Component.translatable("worldedit.reload.config"));
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
            Path dest = we.getConfiguration().getWorkingDirectoryPath().resolve("report.txt");
            Files.writeString(dest, result, StandardCharsets.UTF_8);
            actor.printInfo(Component.translatable("worldedit.report.written", Component.text(dest.toAbsolutePath().toString())));
        } catch (IOException e) {
            actor.printError(Component.translatable("worldedit.report.error", Component.text(e.getMessage())));
        }

        if (pastebin) {
            actor.checkPermission("worldedit.report.pastebin");
            PasteMetadata metadata = new PasteMetadata();
            metadata.author = actor.getName();
            metadata.extension = "report";
            ActorCallbackPaste.pastebin(we.getSupervisor(), actor, result, metadata, Component.translatable("worldedit.report.callback"));
        }
    }

    @Command(
        name = "trace",
        desc = "Toggles trace hook"
    )
    void trace(Actor actor, LocalSession session,
               @Arg(desc = "The mode to set the trace hook to", def = "")
                   HookMode hookMode) {
        boolean previousMode = session.isTracingActions();
        boolean newMode;
        if (hookMode != null) {
            newMode = hookMode == HookMode.ACTIVE;
            if (newMode == previousMode) {
                actor.printError(Component.translatable(previousMode ? "worldedit.trace.active.already" : "worldedit.trace.inactive.already"));
                return;
            }
        } else {
            newMode = !previousMode;
        }
        session.setTracingActions(newMode);
        actor.printInfo(Component.translatable(newMode ? "worldedit.trace.active" : "worldedit.trace.inactive"));
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
    public void tz(Actor actor, LocalSession session,
                   @Arg(desc = "The timezone to set")
                       String timezone) {
        try {
            ZoneId tz = ZoneId.of(timezone);
            session.setTimezone(tz);
            actor.printInfo(Component.translatable("worldedit.timezone.set", Component.text(tz.getDisplayName(
                    TextStyle.FULL, actor.getLocale()
            ))));
            actor.printInfo(Component.translatable("worldedit.timezone.current",
                    Component.text(dateFormat.withLocale(actor.getLocale()).format(ZonedDateTime.now(tz)))));
        } catch (ZoneRulesException e) {
            actor.printError(Component.translatable("worldedit.timezone.invalid"));
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
