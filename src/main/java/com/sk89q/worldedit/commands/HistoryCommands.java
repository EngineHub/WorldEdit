// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.changelog.ApplyChangeLog;
import com.sk89q.worldedit.operation.RejectedOperationException;

/**
 * History little commands.
 */
public class HistoryCommands {
    
    private final WorldEdit worldEdit;
    
    public HistoryCommands(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    @Command(
        aliases = { "/undo", "undo" },
        usage = "[times] [player]",
        desc = "Undoes the last action",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.history.undo")
    public void undo(CommandContext args, LocalSession session,
            LocalPlayer player, EditSession editSession)
            throws WorldEditException, CommandPermissionsException,
            RejectedOperationException {
        
        boolean found = false;
        int times = Math.max(1, args.getInteger(0, 1));
        for (int i =0 ; i < times; ++i) {
            ApplyChangeLog undone;
            if (args.argsLength() < 2) {
                undone = session.createUndo(session.getBlockBag(player), player);
            } else {
                player.checkPermission("worldedit.history.undo.other");
                LocalSession sess = worldEdit.getSessions().getIfExists(args.getString(1));
                if (sess == null) {
                    player.printError("Unable to find session for " + args.getString(1));
                    break;
                }
                undone = sess.createUndo(session.getBlockBag(player), player);
            }
            if (undone != null) {
                found = true;
                worldEdit.execute(player, undone, (EditSession) undone.getExtent(), "/" + args.getCommand());
            } else {
                if (found) {
                    player.printError("There are no more undo's to queue.");
                } else {
                    player.printError("Nothing left to undo.");
                }
                break;
            }
        }
    }

    @Command(
        aliases = { "/redo", "redo" },
        usage = "[times] [player]",
        desc = "Redoes the last action (from history)",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.history.redo")
    public void redo(CommandContext args, LocalSession session,
            LocalPlayer player, EditSession editSession)
            throws WorldEditException, CommandPermissionsException,
            RejectedOperationException {

        boolean found = false;
        int times = Math.max(1, args.getInteger(0, 1));
        for (int i = 0; i < times; ++i) {
            ApplyChangeLog redone;
            if (args.argsLength() < 2) {
                redone = session.createRedo(session.getBlockBag(player), player);
            } else {
                player.checkPermission("worldedit.history.redo.other");
                LocalSession sess = worldEdit.getSessions().getIfExists(args.getString(1));
                if (sess == null) {
                    player.printError("Unable to find session for " + args.getString(1));
                    break;
                }
                redone = sess.createRedo(session.getBlockBag(player), player);
            }   
            if (redone != null) {
                found = true;
                worldEdit.execute(player, redone, (EditSession) redone.getExtent(), "/" + args.getCommand());
            } else {
                if (found) {
                    player.printError("There are no more redo's to queue.");
                } else {
                    player.printError("Nothing left to redo.");
                }
                break;
            }
        }
    }

    @Command(
        aliases = { "/clearhistory", "clearhistory" },
        usage = "",
        desc = "Clear your history",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.history.clear")
    public void clearHistory(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.clearHistory();
        player.print("History cleared.");
    }
    
}
