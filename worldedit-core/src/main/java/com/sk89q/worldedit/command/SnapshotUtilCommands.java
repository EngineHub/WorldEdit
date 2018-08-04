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

import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.snapshot.InvalidSnapshotException;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import com.sk89q.worldedit.world.snapshot.SnapshotRestore;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.MissingWorldException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class SnapshotUtilCommands {

    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");

    private final WorldEdit we;

    public SnapshotUtilCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
            aliases = { "restore", "/restore" },
            usage = "[snapshot]",
            desc = "Restore the selection from a snapshot",
            min = 0,
            max = 1
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.snapshots.restore")
    public void restore(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Snapshot/backup restore is not configured.");
            return;
        }

        Region region = session.getSelection(player.getWorld());
        Snapshot snapshot;

        if (args.argsLength() > 0) {
            try {
                snapshot = config.snapshotRepo.getSnapshot(args.getString(0));
            } catch (InvalidSnapshotException e) {
                player.printError("That snapshot does not exist or is not available.");
                return;
            }
        } else {
            snapshot = session.getSnapshot();
        }

        // No snapshot set?
        if (snapshot == null) {
            try {
                snapshot = config.snapshotRepo.getDefaultSnapshot(player.getWorld().getName());

                if (snapshot == null) {
                    player.printError("No snapshots were found. See console for details.");

                    // Okay, let's toss some debugging information!
                    File dir = config.snapshotRepo.getDirectory();

                    try {
                        logger.info("WorldEdit found no snapshots: looked in: "
                                + dir.getCanonicalPath());
                    } catch (IOException e) {
                        logger.info("WorldEdit found no snapshots: looked in "
                                + "(NON-RESOLVABLE PATH - does it exist?): "
                                + dir.getPath());
                    }

                    return;
                }
            } catch (MissingWorldException ex) {
                player.printError("No snapshots were found for this world.");
                return;
            }
        }

        ChunkStore chunkStore = null;

        // Load chunk store
        try {
            chunkStore = snapshot.getChunkStore();
            player.print("Snapshot '" + snapshot.getName() + "' loaded; now restoring...");
        } catch (DataException | IOException e) {
            player.printError("Failed to load snapshot: " + e.getMessage());
            return;
        }

        try {
            // Restore snapshot
            SnapshotRestore restore = new SnapshotRestore(chunkStore, editSession, region);
            //player.print(restore.getChunksAffected() + " chunk(s) will be loaded.");

            restore.restore();

            if (restore.hadTotalFailure()) {
                String error = restore.getLastErrorMessage();
                if (error != null) {
                    player.printError("Errors prevented any blocks from being restored.");
                    player.printError("Last error: " + error);
                } else {
                    player.printError("No chunks could be loaded. (Bad archive?)");
                }
            } else {
                player.print(String.format("Restored; %d "
                        + "missing chunks and %d other errors.",
                        restore.getMissingChunks().size(),
                        restore.getErrorChunks().size()));
            }
        } finally {
            try {
                chunkStore.close();
            } catch (IOException ignored) {
            }
        }
    }
}
