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

// $Id$

package com.sk89q.worldedit.command;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.InvalidSnapshotException;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import com.sk89q.worldedit.world.storage.MissingWorldException;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Snapshot commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SnapshotCommands {

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final WorldEdit we;

    public SnapshotCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        name = "list",
        desc = "List snapshots"
    )
    @CommandPermissions("worldedit.snapshots.list")
    public void list(Actor actor, World world,
                     @ArgFlag(name = 'p', desc = "Page of results to return", def = "1")
                         int page) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            actor.printError(TranslatableComponent.of("worldedit.restore.not-configured"));
            return;
        }

        try {
            List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, world.getName());

            if (!snapshots.isEmpty()) {
                actor.print(new SnapshotListBox(world.getName(), snapshots).create(page));
            } else {
                actor.printError(TranslatableComponent.of("worldedit.restore.none-found-console"));

                // Okay, let's toss some debugging information!
                File dir = config.snapshotRepo.getDirectory();

                try {
                    WorldEdit.logger.info("WorldEdit found no snapshots: looked in: "
                            + dir.getCanonicalPath());
                } catch (IOException e) {
                    WorldEdit.logger.info("WorldEdit found no snapshots: looked in "
                            + "(NON-RESOLVABLE PATH - does it exist?): "
                            + dir.getPath());
                }
            }
        } catch (MissingWorldException ex) {
            actor.printError(TranslatableComponent.of("worldedit.restore.none-for-world"));
        }
    }

    @Command(
        name = "use",
        desc = "Choose a snapshot to use"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void use(Actor actor, World world, LocalSession session,
                    @Arg(desc = "Snapshot to use")
                        String name) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            actor.printError(TranslatableComponent.of("worldedit.restore.not-configured"));
            return;
        }

        // Want the latest snapshot?
        if (name.equalsIgnoreCase("latest")) {
            try {
                Snapshot snapshot = config.snapshotRepo.getDefaultSnapshot(world.getName());

                if (snapshot != null) {
                    session.setSnapshot(null);
                    actor.printInfo(TranslatableComponent.of("worldedit.snapshot.use.newest"));
                } else {
                    actor.printError(TranslatableComponent.of("worldedit.restore.none-found"));
                }
            } catch (MissingWorldException ex) {
                actor.printError(TranslatableComponent.of("worldedit.restore.none-for-world"));
            }
        } else {
            try {
                session.setSnapshot(config.snapshotRepo.getSnapshot(name));
                actor.printInfo(TranslatableComponent.of("worldedit.snapshot.use", TextComponent.of(name)));
            } catch (InvalidSnapshotException e) {
                actor.printError(TranslatableComponent.of("worldedit.restore.not-available"));
            }
        }
    }

    @Command(
        name = "sel",
        desc = "Choose the snapshot based on the list id"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void sel(Actor actor, World world, LocalSession session,
                    @Arg(desc = "The list ID to select")
                        int index) throws WorldEditException {
        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            actor.printError(TranslatableComponent.of("worldedit.restore.not-configured"));
            return;
        }

        if (index < 1) {
            actor.printError(TranslatableComponent.of("worldedit.snapshot.index-above-0"));
            return;
        }

        try {
            List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, world.getName());
            if (snapshots.size() < index) {
                actor.printError(TranslatableComponent.of("worldedit.snapshot.index-oob", TextComponent.of(snapshots.size())));
                return;
            }
            Snapshot snapshot = snapshots.get(index - 1);
            if (snapshot == null) {
                actor.printError(TranslatableComponent.of("worldedit.restore.not-available"));
                return;
            }
            session.setSnapshot(snapshot);
            actor.printInfo(TranslatableComponent.of("worldedit.snapshot.use", TextComponent.of(snapshot.getName())));
        } catch (MissingWorldException e) {
            actor.printError(TranslatableComponent.of("worldedit.restore.none-for-world"));
        }
    }

    @Command(
        name = "before",
        desc = "Choose the nearest snapshot before a date"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void before(Actor actor, World world, LocalSession session,
                       @Arg(desc = "The soonest date that may be used")
                           ZonedDateTime date) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            actor.printError(TranslatableComponent.of("worldedit.restore.not-configured"));
            return;
        }

        try {
            Snapshot snapshot = config.snapshotRepo.getSnapshotBefore(date, world.getName());

            if (snapshot == null) {
                actor.printError(TranslatableComponent.of(
                        "worldedit.snapshot.none-before",
                        TextComponent.of(dateFormat.withZone(session.getTimeZone()).format(date)))
                );
            } else {
                session.setSnapshot(snapshot);
                actor.printInfo(TranslatableComponent.of("worldedit.snapshot.use", TextComponent.of(snapshot.getName())));
            }
        } catch (MissingWorldException ex) {
            actor.printError(TranslatableComponent.of("worldedit.restore.none-for-world"));
        }
    }

    @Command(
        name = "after",
        desc = "Choose the nearest snapshot after a date"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    public void after(Actor actor, World world, LocalSession session,
                      @Arg(desc = "The soonest date that may be used")
                          ZonedDateTime date) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            actor.printError(TranslatableComponent.of("worldedit.restore.not-configured"));
            return;
        }

        try {
            Snapshot snapshot = config.snapshotRepo.getSnapshotAfter(date, world.getName());
            if (snapshot == null) {
                actor.printError(TranslatableComponent.of(
                        "worldedit.snapshot.none-after",
                        TextComponent.of(dateFormat.withZone(session.getTimeZone()).format(date)))
                );
            } else {
                session.setSnapshot(snapshot);
                actor.printInfo(TranslatableComponent.of("worldedit.snapshot.use", TextComponent.of(snapshot.getName())));
            }
        } catch (MissingWorldException ex) {
            actor.printError(TranslatableComponent.of("worldedit.restore.none-for-world"));
        }
    }

    private static class SnapshotListBox extends PaginationBox {
        private final List<Snapshot> snapshots;

        SnapshotListBox(String world, List<Snapshot> snapshots) {
            super("Snapshots for: " + world, "/snap list -p %page%");
            this.snapshots = snapshots;
        }

        @Override
        public Component getComponent(int number) {
            final Snapshot snapshot = snapshots.get(number);
            return TextComponent.of(number + 1 + ". ", TextColor.GOLD)
                    .append(TextComponent.of(snapshot.getName(), TextColor.LIGHT_PURPLE)
                            .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to use")))
                            .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/snap use " + snapshot.getName())));
        }

        @Override
        public int getComponentsSize() {
            return snapshots.size();
        }
    }
}
