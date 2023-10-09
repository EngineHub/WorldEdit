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

// $Id$

package com.sk89q.worldedit.command;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.adventure.PaginationBox;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.event.ClickEvent;
import com.sk89q.worldedit.util.adventure.text.event.HoverEvent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabase;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.exception.StopExecutionException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Snapshot commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SnapshotCommands {

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final WorldEdit we;
    private final LegacySnapshotCommands legacy;

    public SnapshotCommands(WorldEdit we) {
        this.we = we;
        this.legacy = new LegacySnapshotCommands(we);
    }

    static void checkSnapshotsConfigured(LocalConfiguration localConfiguration) {
        if (!localConfiguration.snapshotsConfigured) {
            throw new StopExecutionException(Component.translatable(
                "worldedit.restore.not-configured"
            ));
        }
    }

    static URI resolveSnapshotName(LocalConfiguration config, String name) {
        if (!name.contains("://")) {
            if (config.snapshotDatabase instanceof FileSystemSnapshotDatabase) {
                String newName = name;
                while (newName.startsWith("/")) {
                    newName = newName.substring(1);
                }
                return FileSystemSnapshotDatabase.createUri(newName);
            }
        }
        return URI.create(name);
    }


    @Command(
        name = "list",
        desc = "List snapshots"
    )
    @CommandPermissions("worldedit.snapshots.list")
    void list(Actor actor, World world,
                     @ArgFlag(name = 'p', desc = "Page of results to return", def = "1")
                         int page) throws WorldEditException, IOException {
        LocalConfiguration config = we.getConfiguration();
        checkSnapshotsConfigured(config);

        if (config.snapshotRepo != null) {
            legacy.list(actor, world, page);
            return;
        }

        List<Snapshot> snapshots;
        try (Stream<Snapshot> snapshotStream =
                 config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshots = snapshotStream
                .collect(toList());
        }

        if (!snapshots.isEmpty()) {
            actor.print(new SnapshotListBox(world.getName(), snapshots).build(page));
        } else {
            actor.printError(Component.translatable(
                "worldedit.restore.none-for-specific-world",
                Component.text(world.getName())
            ));

            if (config.snapshotDatabase instanceof FileSystemSnapshotDatabase db) {
                Path root = db.getRoot();
                if (Files.isDirectory(root)) {
                    WorldEdit.logger.info("No snapshots were found for world '"
                        + world.getName() + "'; looked in " + root.toRealPath());
                } else {
                    WorldEdit.logger.info("No snapshots were found for world '"
                        + world.getName() + "'; " + root.toRealPath() + " is not a directory");
                }
            }
        }
    }

    @Command(
        name = "use",
        desc = "Choose a snapshot to use"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    void use(Actor actor, World world, LocalSession session,
                    @Arg(desc = "Snapshot to use")
                        String name) throws IOException {
        LocalConfiguration config = we.getConfiguration();
        checkSnapshotsConfigured(config);

        if (config.snapshotRepo != null) {
            legacy.use(actor, world, session, name);
            return;
        }

        // Want the latest snapshot?
        if (name.equalsIgnoreCase("latest")) {
            Snapshot snapshot;
            try (Stream<Snapshot> snapshotStream =
                     config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
                snapshot = snapshotStream
                    .findFirst().orElse(null);
            }

            if (snapshot != null) {
                if (session.getSnapshotExperimental() != null) {
                    session.getSnapshotExperimental().close();
                }
                session.setSnapshot(null);
                actor.printInfo(Component.translatable("worldedit.snapshot.use.newest"));
            } else {
                actor.printError(Component.translatable("worldedit.restore.none-for-world"));
            }
        } else {
            URI uri = resolveSnapshotName(config, name);
            Optional<Snapshot> snapshot = config.snapshotDatabase.getSnapshot(uri);
            if (snapshot.isPresent()) {
                if (session.getSnapshotExperimental() != null) {
                    session.getSnapshotExperimental().close();
                }
                session.setSnapshotExperimental(snapshot.get());
                actor.printInfo(Component.translatable(
                    "worldedit.snapshot.use", Component.text(name)
                ));
            } else {
                actor.printError(Component.translatable("worldedit.restore.not-available"));
            }
        }
    }

    @Command(
        name = "sel",
        desc = "Choose the snapshot based on the list id"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    void sel(Actor actor, World world, LocalSession session,
                    @Arg(desc = "The list ID to select")
                        int index) throws IOException {
        LocalConfiguration config = we.getConfiguration();
        checkSnapshotsConfigured(config);

        if (config.snapshotRepo != null) {
            legacy.sel(actor, world, session, index);
            return;
        }

        if (index < 1) {
            actor.printError(Component.translatable("worldedit.snapshot.index-above-0"));
            return;
        }

        List<Snapshot> snapshots;
        try (Stream<Snapshot> snapshotStream =
                 config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshots = snapshotStream
                .collect(toList());
        }
        if (snapshots.size() < index) {
            actor.printError(Component.translatable(
                "worldedit.snapshot.index-oob",
                Component.text(snapshots.size())
            ));
            return;
        }
        Snapshot snapshot = snapshots.get(index - 1);
        if (snapshot == null) {
            actor.printError(Component.translatable("worldedit.restore.not-available"));
            return;
        }
        if (session.getSnapshotExperimental() != null) {
            session.getSnapshotExperimental().close();
        }
        session.setSnapshotExperimental(snapshot);
        actor.printInfo(Component.translatable(
            "worldedit.snapshot.use",
            Component.text(snapshot.getInfo().getDisplayName())
        ));
    }

    @Command(
        name = "before",
        desc = "Choose the nearest snapshot before a date"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    void before(Actor actor, World world, LocalSession session,
                       @Arg(desc = "The soonest date that may be used")
                           ZonedDateTime date) throws IOException {
        LocalConfiguration config = we.getConfiguration();
        checkSnapshotsConfigured(config);

        if (config.snapshotRepo != null) {
            legacy.before(actor, world, session, date);
            return;
        }

        Snapshot snapshot;
        try (Stream<Snapshot> snapshotStream =
                 config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshot = snapshotStream
                .findFirst().orElse(null);
        }

        if (snapshot == null) {
            actor.printError(Component.translatable(
                "worldedit.snapshot.none-before",
                Component.text(dateFormat.withZone(session.getTimeZone()).format(date)))
            );
        } else {
            if (session.getSnapshotExperimental() != null) {
                session.getSnapshotExperimental().close();
            }
            session.setSnapshotExperimental(snapshot);
            actor.printInfo(Component.translatable(
                "worldedit.snapshot.use",
                Component.text(snapshot.getInfo().getDisplayName())
            ));
        }
    }

    @Command(
        name = "after",
        desc = "Choose the nearest snapshot after a date"
    )
    @CommandPermissions("worldedit.snapshots.restore")
    void after(Actor actor, World world, LocalSession session,
                      @Arg(desc = "The soonest date that may be used")
                          ZonedDateTime date) throws IOException {
        LocalConfiguration config = we.getConfiguration();
        checkSnapshotsConfigured(config);

        if (config.snapshotRepo != null) {
            legacy.after(actor, world, session, date);
            return;
        }

        Snapshot snapshot;
        try (Stream<Snapshot> snapshotStream =
                 config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
            snapshot = snapshotStream
                .findFirst().orElse(null);
        }
        if (snapshot == null) {
            actor.printError(Component.translatable(
                "worldedit.snapshot.none-after",
                Component.text(dateFormat.withZone(session.getTimeZone()).format(date)))
            );
        } else {
            if (session.getSnapshotExperimental() != null) {
                session.getSnapshotExperimental().close();
            }
            session.setSnapshotExperimental(snapshot);
            actor.printInfo(Component.translatable(
                "worldedit.snapshot.use",
                Component.text(snapshot.getInfo().getDisplayName())
            ));
        }
    }

    private static class SnapshotListBox extends PaginationBox {
        private final List<Snapshot> snapshots;

        SnapshotListBox(String world, List<Snapshot> snapshots) {
            super("Snapshots for: " + world, "/snap list -p %page%");
            this.snapshots = snapshots;
        }

        @Override
        public Component component(int number) {
            final Snapshot snapshot = snapshots.get(number);
            String displayName = snapshot.getInfo().getDisplayName();
            return Component.text(number + 1 + ". ", NamedTextColor.GOLD)
                .append(Component.text(displayName, NamedTextColor.LIGHT_PURPLE)
                    .hoverEvent(HoverEvent.showText(Component.text("Click to use")))
                    .clickEvent(ClickEvent.runCommand("/snap use " + displayName)));
        }

        @Override
        public int getComponentsSize() {
            return snapshots.size();
        }
    }
}
