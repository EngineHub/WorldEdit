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
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.InvalidSnapshotException;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import com.sk89q.worldedit.world.storage.MissingWorldException;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Legacy snapshot command implementations. Commands are still registered via
 * {@link SnapshotCommands}, but it delegates to this class when legacy snapshots are in use.
 */
class LegacySnapshotCommands {

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private final WorldEdit we;

    LegacySnapshotCommands(WorldEdit we) {
        this.we = we;
    }

    void list(Actor actor, World world, int page) throws WorldEditException {
        LocalConfiguration config = we.getConfiguration();

        try {
            List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, world.getName());

            if (!snapshots.isEmpty()) {
                actor.print(new SnapshotListBox(world.getName(), snapshots).create(page));
            } else {
                actor.printError("No snapshots are available. See console for details.");

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
            actor.printError("No snapshots were found for this world.");
        }
    }

    void use(Actor actor, World world, LocalSession session, String name) {
        LocalConfiguration config = we.getConfiguration();

        // Want the latest snapshot?
        if (name.equalsIgnoreCase("latest")) {
            try {
                Snapshot snapshot = config.snapshotRepo.getDefaultSnapshot(world.getName());

                if (snapshot != null) {
                    session.setSnapshot(null);
                    actor.print("Now using newest snapshot.");
                } else {
                    actor.printError("No snapshots were found.");
                }
            } catch (MissingWorldException ex) {
                actor.printError("No snapshots were found for this world.");
            }
        } else {
            try {
                session.setSnapshot(config.snapshotRepo.getSnapshot(name));
                actor.print("Snapshot set to: " + name);
            } catch (InvalidSnapshotException e) {
                actor.printError("That snapshot does not exist or is not available.");
            }
        }
    }

    void sel(Actor actor, World world, LocalSession session, int index) {
        LocalConfiguration config = we.getConfiguration();

        if (index < 1) {
            actor.printError("Invalid index, must be equal or higher then 1.");
            return;
        }

        try {
            List<Snapshot> snapshots = config.snapshotRepo.getSnapshots(true, world.getName());
            if (snapshots.size() < index) {
                actor.printError("Invalid index, must be between 1 and " + snapshots.size() + ".");
                return;
            }
            Snapshot snapshot = snapshots.get(index - 1);
            if (snapshot == null) {
                actor.printError("That snapshot does not exist or is not available.");
                return;
            }
            session.setSnapshot(snapshot);
            actor.print("Snapshot set to: " + snapshot.getName());
        } catch (MissingWorldException e) {
            actor.printError("No snapshots were found for this world.");
        }
    }

    void before(Actor actor, World world, LocalSession session, ZonedDateTime date) {
        LocalConfiguration config = we.getConfiguration();

        try {
            Snapshot snapshot = config.snapshotRepo.getSnapshotBefore(date, world.getName());

            if (snapshot == null) {
                actor.printError("Couldn't find a snapshot before "
                    + dateFormat.withZone(session.getTimeZone()).format(date) + ".");
            } else {
                session.setSnapshot(snapshot);
                actor.print("Snapshot set to: " + snapshot.getName());
            }
        } catch (MissingWorldException ex) {
            actor.printError("No snapshots were found for this world.");
        }
    }

    void after(Actor actor, World world, LocalSession session, ZonedDateTime date) {
        LocalConfiguration config = we.getConfiguration();

        try {
            Snapshot snapshot = config.snapshotRepo.getSnapshotAfter(date, world.getName());
            if (snapshot == null) {
                actor.printError("Couldn't find a snapshot after "
                    + dateFormat.withZone(session.getTimeZone()).format(date) + ".");
            } else {
                session.setSnapshot(snapshot);
                actor.print("Snapshot set to: " + snapshot.getName());
            }
        } catch (MissingWorldException ex) {
            actor.printError("No snapshots were found for this world.");
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
