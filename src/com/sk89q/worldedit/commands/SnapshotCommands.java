// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import java.io.IOException;
import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.data.ChunkStore;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.snapshots.InvalidSnapshotException;
import com.sk89q.worldedit.snapshots.Snapshot;
import com.sk89q.worldedit.snapshots.SnapshotRestore;

/**
 * Snapshot commands.
 * 
 * @author sk89q
 */
public class SnapshotCommands {
    @Command(
        aliases = {"listsnapshots"},
        usage = "[num]",
        desc = "List snapshots",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.snapshots.list"})
    public static void list(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();
        
        int num = args.argsLength() > 0 ?
            Math.min(40, Math.max(5, args.getInteger(0))) : 5;

        if (config.snapshotRepo != null) {
            Snapshot[] snapshots = config.snapshotRepo.getSnapshots();

            if (snapshots.length > 0) {
                for (byte i = 0; i < Math.min(num, snapshots.length); i++) {
                    player.print((i + 1) + ". " + snapshots[i].getName());
                }

                player.print("Use //use [snapshot] or //use latest to set the snapshot.");
            } else {
                player.printError("No snapshots are available.");
            }
        } else {
            player.printError("Snapshot/backup restore is not configured.");
        }
    }
    
    @Command(
        aliases = {"/use"},
        usage = "<snapshot>",
        desc = "Choose a snapshot to use",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.snapshots.restore"})
    public static void use(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();
        
        if (config.snapshotRepo == null) {
            player.printError("Snapshot/backup restore is not configured.");
            return;
        }

        String name = args.getString(0);

        // Want the latest snapshot?
        if (name.equalsIgnoreCase("latest")) {
            Snapshot snapshot = config.snapshotRepo.getDefaultSnapshot();

            if (snapshot != null) {
                session.setSnapshot(null);
                player.print("Now using newest snapshot.");
            } else {
                player.printError("No snapshots were found.");
            }
        } else {
            try {
                session.setSnapshot(config.snapshotRepo.getSnapshot(name));
                player.print("Snapshot set to: " + name);
            } catch (InvalidSnapshotException e) {
                player.printError("That snapshot does not exist or is not available.");
            }
        }
    }

    @Command(
        aliases = {"/restore"},
        usage = "[snapshot]",
        desc = "Restore the selection from a snapshot",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.snapshots.restore"})
    public static void restore(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Snapshot/backup restore is not configured.");
            return;
        }

        Region region = session.getRegion();
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
        
        ChunkStore chunkStore = null;

        // No snapshot set?
        if (snapshot == null) {
            snapshot = config.snapshotRepo.getDefaultSnapshot();

            if (snapshot == null) {
                player.printError("No snapshots were found.");
                return;
            }
        }

        // Load chunk store
        try {
            chunkStore = snapshot.getChunkStore();
            player.print("Snapshot '" + snapshot.getName() + "' loaded; now restoring...");
        } catch (DataException e) {
            player.printError("Failed to load snapshot: " + e.getMessage());
            return;
        } catch (IOException e) {
            player.printError("Failed to load snapshot: " + e.getMessage());
            return;
        }

        try {
            // Restore snapshot
            SnapshotRestore restore = new SnapshotRestore(chunkStore, region);
            //player.print(restore.getChunksAffected() + " chunk(s) will be loaded.");

            restore.restore(editSession);

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
            } catch (IOException e) {
            }
        }
    }
}
