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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.zone.ZoneRulesException;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
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
import com.sk89q.worldedit.extent.TracingExtent;
import com.sk89q.worldedit.util.formatting.component.MessageBox;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldedit.util.report.ConfigReport;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

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
        actor.printInfo(TranslatableComponent.of("worldedit.version.version", TextComponent.of(WorldEdit.getVersion())));
        actor.printInfo(TextComponent.of("https://github.com/EngineHub/WorldEdit/"));

        PlatformManager pm = we.getPlatformManager();

        TextComponentProducer producer = new TextComponentProducer();
        for (Platform platform : pm.getPlatforms()) {
            producer.append(
                    TextComponent.of("* ", TextColor.GRAY)
                    .append(TextComponent.of(platform.getPlatformName()))
                    .append(TextComponent.of("(" + platform.getPlatformVersion() + ")"))
            ).newline();
        }
        actor.print(new MessageBox("Platforms", producer, TextColor.GRAY).create());

        producer.reset();
        for (Capability capability : Capability.values()) {
            Platform platform = pm.queryCapability(capability);
            producer.append(
                    TextComponent.of(capability.name(), TextColor.GRAY)
                    .append(TextComponent.of(": ")
                    .append(TextComponent.of(platform != null ? platform.getPlatformName() : "NONE")))
            ).newline();
        }
        actor.print(new MessageBox("Capabilities", producer, TextColor.GRAY).create());
    }

    @Command(
        name = "reload",
        desc = "Reload configuration"
    )
    @CommandPermissions("worldedit.reload")
    public void reload(Actor actor) {
        we.getPlatformManager().queryCapability(Capability.CONFIGURATION).reload();
        we.getEventBus().post(new ConfigurationLoadEvent(we.getPlatformManager().queryCapability(Capability.CONFIGURATION).getConfiguration()));
        actor.printInfo(TranslatableComponent.of("worldedit.reload.config"));
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
            Files.write(result, dest, StandardCharsets.UTF_8);
            actor.printInfo(TranslatableComponent.of("worldedit.report.written", TextComponent.of(dest.getAbsolutePath())));
        } catch (IOException e) {
            actor.printError(TranslatableComponent.of("worldedit.report.error", TextComponent.of(e.getMessage())));
        }

        if (pastebin) {
            actor.checkPermission("worldedit.report.pastebin");
            ActorCallbackPaste.pastebin(we.getSupervisor(), actor, result, TranslatableComponent.builder("worldedit.report.callback"));
        }
    }

    @Command(
        name = "trace",
        desc = "Trace edit actions to see what's blocking them",
        descFooter = "This will run the given action at your placement position, then provide a single" +
            " extent which was the first to return a failure for that action"
    )
    void trace(Actor actor, LocalSession localSession,
               @Arg(desc = "The action to trace")
                   TracingExtent.Action action,
               @Switch(name = 'a', desc = "Print all active extents") boolean all) throws IncompleteRegionException {
        EditSession session = localSession.createEditSession(actor, true);
        try (EditSession ignored = session) {
            if (action.test.test(session, localSession.getPlacementPosition(actor))) {
                actor.printInfo(TranslatableComponent.builder("worldedit.trace.success")
                    .args(TextComponent.of(action.toString()))
                    .build());
                return;
            }
        }
        List<TracingExtent> tracingExtents = session.getTracingExtents();
        assert tracingExtents != null;
        if (tracingExtents.isEmpty()) {
            actor.printError(TranslatableComponent.of("worldedit.trace.no-tracing-extents"));
            return;
        }
        if (!all) {
            // make it only print the last one (which is the failure)
            tracingExtents = ImmutableList.of(Iterables.getLast(tracingExtents));
        }
        for (TracingExtent tracingExtent : tracingExtents) {
            actor.printInfo(TranslatableComponent.builder("worldedit.trace.extent")
                .args(
                    TextComponent.of(tracingExtent.getFailedActions().contains(action)),
                    TextComponent.of(tracingExtent.getExtent().getClass().getName())
                )
                .build());
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
    public void tz(Actor actor, LocalSession session,
                   @Arg(desc = "The timezone to set")
                       String timezone) {
        try {
            ZoneId tz = ZoneId.of(timezone);
            session.setTimezone(tz);
            actor.printInfo(TranslatableComponent.of("worldedit.timezone.set", TextComponent.of(tz.getDisplayName(
                    TextStyle.FULL, actor.getLocale()
            ))));
            actor.printInfo(TranslatableComponent.of("worldedit.timezone.current",
                    TextComponent.of(dateFormat.withLocale(actor.getLocale()).format(ZonedDateTime.now(tz)))));
        } catch (ZoneRulesException e) {
            actor.printError(TranslatableComponent.of("worldedit.timezone.invalid"));
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
