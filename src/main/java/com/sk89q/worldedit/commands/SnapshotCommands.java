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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.snapshots.InvalidSnapshotException;
import com.sk89q.worldedit.snapshots.Snapshot;
import com.sk89q.worldedit.snapshots.SnapshotRepository;

/**
 * Snapshot commands.
 * 
 * @author sk89q
 */
public class SnapshotCommands {
    private static Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    
    @Command(
        aliases = {"list"},
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
        
        String worldName = player.getWorld().getName();
        SnapshotRepository repo = config.snapshotRepositories.get(worldName);
        
        if (repo == null) {
            player.printError("Snapshot/backup restore is not configured for this world.");
            return;
        }
        
        List<Snapshot> snapshots = repo.getSnapshots(true);

        if (snapshots.size() > 0) {
            for (byte i = 0; i < Math.min(num, snapshots.size()); i++) {
                player.print((i + 1) + ". " + snapshots.get(i).getName());
            }

            player.print("Use /snap use [snapshot] or /snap use latest.");
        } else {
            player.printError("No snapshots are available. See console for details.");

            // Okay, let's toss some debugging information!
            File dir = repo.getDirectory();

            try {
                logger.info("WorldEdit found no snapshots: looked in: " +
                        dir.getCanonicalPath());
            } catch (IOException e) {
                logger.info("WorldEdit found no snapshots: looked in "
                        + "(NON-RESOLVABLE PATH - does it exist?): " +
                        dir.getPath());
            }
        }
    }
    
    @Command(
        aliases = {"use"},
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
        String worldName = player.getWorld().getName();
        SnapshotRepository repo = config.snapshotRepositories.get(worldName);
        
        if (repo == null) {
            player.printError("Snapshot/backup restore is not configured for this world.");
            return;
        }

        String name = args.getString(0);

        // Want the latest snapshot?
        if (name.equalsIgnoreCase("latest")) {
            Snapshot snapshot = repo.getDefaultSnapshot();

            if (snapshot != null) {
                session.setSnapshot(player.getWorld(), null);
                player.print("Now using newest snapshot.");
            } else {
                player.printError("No snapshots were found.");
            }
        } else {
            try {
                session.setSnapshot(player.getWorld(), repo.getSnapshot(name));
                player.print("Snapshot set to: " + name);
            } catch (InvalidSnapshotException e) {
                player.printError("That snapshot does not exist or is not available.");
            }
        }
    }
    
    @Command(
        aliases = {"before"},
        usage = "<date>",
        desc = "Choose the nearest snapshot before a date",
        min = 1,
        max = -1
    )
    @CommandPermissions({"worldedit.snapshots.restore"})
    public static void before(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();
        String worldName = player.getWorld().getName();
        SnapshotRepository repo = config.snapshotRepositories.get(worldName);
        
        if (repo == null) {
            player.printError("Snapshot/backup restore is not configured for this world.");
            return;
        }
        
        Calendar date = session.detectDate(args.getJoinedStrings(0));
        
        if (date == null) {
            player.printError("Could not detect the date inputted.");
        } else {
            dateFormat.setTimeZone(session.getTimeZone());
            
            Snapshot snapshot = repo.getSnapshotBefore(date);
            if (snapshot == null) {
                player.printError("Couldn't find a snapshot before "
                        + dateFormat.format(date.getTime()) + ".");
            } else {
                session.setSnapshot(player.getWorld(), snapshot);
                player.print("Snapshot set to: " + snapshot.getName());
            }
        }
    }
    
    @Command(
        aliases = {"after"},
        usage = "<date>",
        desc = "Choose the nearest snapshot after a date",
        min = 1,
        max = -1
    )
    @CommandPermissions({"worldedit.snapshots.restore"})
    public static void after(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();
        String worldName = player.getWorld().getName();
        SnapshotRepository repo = config.snapshotRepositories.get(worldName);
        
        if (repo == null) {
            player.printError("Snapshot/backup restore is not configured for this world.");
            return;
        }
        
        Calendar date = session.detectDate(args.getJoinedStrings(0));
        
        if (date == null) {
            player.printError("Could not detect the date inputted.");
        } else {
            dateFormat.setTimeZone(session.getTimeZone());
            
            Snapshot snapshot = repo.getSnapshotAfter(date);
            if (snapshot == null) {
                player.printError("Couldn't find a snapshot after "
                        + dateFormat.format(date.getTime()) + ".");
            } else {
                session.setSnapshot(player.getWorld(), snapshot);
                player.print("Snapshot set to: " + snapshot.getName());
            }
        }
    }
}
