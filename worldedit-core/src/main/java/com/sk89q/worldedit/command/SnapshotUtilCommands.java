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
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.snapshot.InvalidSnapshotException;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import com.sk89q.worldedit.world.snapshot.SnapshotRestore;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.MissingWorldException;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

import java.io.File;
import java.io.IOException;

import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SnapshotUtilCommands {

    private final WorldEdit we;

    public SnapshotUtilCommands(WorldEdit we) {
        this.we = we;
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
                            String snapshotName) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            actor.printError(TranslatableComponent.of("worldedit.restore.not-configured"));
            return;
        }

        Region region = session.getSelection(world);
        Snapshot snapshot;

        if (snapshotName != null) {
            try {
                snapshot = config.snapshotRepo.getSnapshot(snapshotName);
            } catch (InvalidSnapshotException e) {
                actor.printError(TranslatableComponent.of("worldedit.restore.not-available"));
                return;
            }
        } else {
            snapshot = session.getSnapshot();
        }

        // No snapshot set?
        if (snapshot == null) {
            try {
                snapshot = config.snapshotRepo.getDefaultSnapshot(world.getName());

                if (snapshot == null) {
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

                    return;
                }
            } catch (MissingWorldException ex) {
                actor.printError(TranslatableComponent.of("worldedit.restore.none-for-world"));
                return;
            }
        }

        ChunkStore chunkStore;

        // Load chunk store
        try {
            chunkStore = snapshot.getChunkStore();
            actor.printInfo(TranslatableComponent.of("worldedit.restore.loaded", TextComponent.of(snapshot.getName())));
        } catch (DataException | IOException e) {
            actor.printError(TranslatableComponent.of("worldedit.restore.failed", TextComponent.of(e.getMessage())));
            return;
        }

        try {
            // Restore snapshot
            SnapshotRestore restore = new SnapshotRestore(chunkStore, editSession, region);
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
                chunkStore.close();
            } catch (IOException ignored) {
            }
        }
    }
}
