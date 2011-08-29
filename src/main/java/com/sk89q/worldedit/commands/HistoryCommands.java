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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.*;

/**
 * History little commands.
 * 
 * @author sk89q
 */
public class HistoryCommands {
    @Command(
        aliases = {"/undo", "undo"},
        usage = "[times] [player]",
        desc = "Undoes the last action",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.history.undo"})
    public static void undo(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        int times = Math.max(1, args.getInteger(0, 1));
        for (int i = 0; i < times; ++i) {
            EditSession undone;
            if (args.argsLength() < 2) {
                undone = session.undo(session.getBlockBag(player));
            } else {
                player.checkPermission("worldedit.history.undo.other");
                LocalSession sess = we.getSession(args.getString(1));
                if (sess == null){
                    player.printError("Unable to find session for " + args.getString(1));
                    break;
                }
                undone = sess.undo(session.getBlockBag(player));
            }
            if (undone != null) {
                player.print("Undo successful.");
                we.flushBlockBag(player, undone);
            } else {
                player.printError("Nothing left to undo.");
                break;
            }
        }
    }
    
    @Command(
        aliases = {"/redo", "redo"},
        usage = "[times] [player]",
        desc = "Redoes the last action (from history)",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.history.redo"})
    public static void redo(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        int times = Math.max(1, args.getInteger(0, 1));
        
        for (int i = 0; i < times; ++i) {
            EditSession redone;
            if (args.argsLength() < 2) {
                redone = session.redo(session.getBlockBag(player));
            } else {
                player.checkPermission("worldedit.history.redo.other");
                LocalSession sess = we.getSession(args.getString(1));
                if (sess == null){
                    player.printError("Unable to find session for " + args.getString(1));
                    break;
                }
                redone = sess.redo(session.getBlockBag(player));
            }
            if (redone != null) {
                player.print("Redo successful.");
                we.flushBlockBag(player, redone);
            } else {
                player.printError("Nothing left to redo.");
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
    @CommandPermissions({"worldedit.history.clear"})
    public static void clearHistory(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.clearHistory();
        player.print("History cleared.");
    }
}
