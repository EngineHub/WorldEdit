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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotRestore;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import static com.sk89q.worldedit.command.SnapshotCommands.checkSnapshotsConfigured;
import static com.sk89q.worldedit.command.SnapshotCommands.resolveSnapshotName;
import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SnapshotUtilCommands {

    private final WorldEdit we;
    private final LegacySnapshotUtilCommands legacy;

    public SnapshotUtilCommands(WorldEdit we) {
        this.we = we;
        this.legacy = new LegacySnapshotUtilCommands(we);
    }

    @Command(
        name = "restore",
        aliases = { "/restore" },
        desc = "Restore the selection from a snapshot"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.snapshots.restore")
    public void restore(Actor actor, World world, LocalSession session, EditSession editSession,
                        @Arg(name = "snapshot", desc = "The snapshot to restore", def = "")
                            String snapshotName) throws WorldEditException, IOException {
        LocalConfiguration config = we.getConfiguration();
        checkSnapshotsConfigured(config);

        if (config.snapshotRepo != null) {
            legacy.restore(actor, world, session, editSession, snapshotName);
            return;
        }

        Region region = session.getSelection(world);
        Snapshot snapshot;

        if (snapshotName != null) {
            URI uri = resolveSnapshotName(config, snapshotName);
            Optional<Snapshot> snapOpt = config.snapshotDatabase.getSnapshot(uri);
            if (!snapOpt.isPresent()) {
                actor.printError(TranslatableComponent.of("worldedit.restore.not-available"));
                return;
            }
            snapshot = snapOpt.get();
        } else {
            snapshot = session.getSnapshotExperimental();
        }

        // No snapshot set?
        if (snapshot == null) {
            try (Stream<Snapshot> snapshotStream =
                     config.snapshotDatabase.getSnapshotsNewestFirst(world.getName())) {
                snapshot = snapshotStream
                    .findFirst().orElse(null);
            }

            if (snapshot == null) {
                actor.printError(TranslatableComponent.of(
                    "worldedit.restore.none-for-specific-world",
                    TextComponent.of(world.getName())
                ));
                return;
            }
        }
        actor.printInfo(TranslatableComponent.of(
            "worldedit.restore.loaded",
            TextComponent.of(snapshot.getInfo().getDisplayName())
        ));

        try {
            // Restore snapshot
            SnapshotRestore restore = new SnapshotRestore(snapshot, editSession, region);
            //player.print(restore.getChunksAffected() + " chunk(s) will be loaded.");

            restore.restore();

            if (restore.hadTotalFailure()) {
                String error = restore.getLastErrorMessage();
                if (!restore.getMissingChunks().isEmpty()) {
                    actor.printError(TranslatableComponent.of("worldedit.restore.chunk-not-present"));
                } else if (error != null) {
                    actor.printError(TranslatableComponent.of("worldedit.restore.block-place-failed"));
                    actor.printError(TranslatableComponent.of("worldedit.restore.block-place-error", TextComponent.of(error)));
                } else {
                    actor.printError(TranslatableComponent.of("worldedit.restore.chunk-load-failed"));
                }
            } else {
                actor.printInfo(TranslatableComponent.of("worldedit.restore.restored",
                    TextComponent.of(restore.getMissingChunks().size()),
                    TextComponent.of(restore.getErrorChunks().size())));
            }
        } finally {
            try {
                snapshot.close();
            } catch (IOException ignored) {
            }
        }
    }
}
