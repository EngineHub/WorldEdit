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

import java.util.ArrayList;
import java.util.List;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.formatting.MessageBox;
import com.sk89q.rebar.formatting.MessageBuilder;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.operation.OperationListFragment;
import com.sk89q.worldedit.operation.PlayerIssuedOperation;
import com.sk89q.worldedit.operation.QueuedOperation;

/**
 * Commands for managing running operations.
 */
public class OperationCommands {

    private final WorldEdit worldEdit;

    /**
     * Construct a new instance.
     * 
     * @param worldEdit an instance of WorldEdit
     */
    public OperationCommands(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /*
     * List all running or queued operations.
     */
    @Command(aliases = { "/queue", "/running", "/queued" },
             desc = "List all concurrently running or queued operations")
    @CommandPermissions("worldedit.operation.list")
    public void listRunning(LocalPlayer player) {
        List<QueuedOperation> queue = worldEdit.getExecutor().getQueue();

        MessageBuilder builder = worldEdit.createMessage();
        MessageBox box = new MessageBox();
        box.getContents()
                .append(queue.size()).append(" operation(s) queued or running.")
                .append(new OperationListFragment(queue, true));
        builder.append(box);
        player.print(builder);
    }

    /*
     * Cancel all running or queued operations.
     */
    @Command(aliases = "/cancelall",
             desc = "Cancel all concurrently running or queued operations")
    @CommandPermissions("worldedit.operation.cancel")
    public void cancelAll(LocalPlayer player) throws CommandException {
        List<QueuedOperation> cancelled = worldEdit.getExecutor().cancelAll();
        
        if (cancelled.size() == 0) {
            throw new CommandException("There were no queued or running operations to cancel.");
        }
        
        MessageBuilder builder = worldEdit.createMessage();
        MessageBox box = new MessageBox();
        box.getContents()
                .append(cancelled.size()).append(" operation(s) cancelled.")
                .append(new OperationListFragment(cancelled, true));
        builder.append(box);
        player.print(builder);
    }

    /*
     * Cancel all running or queued operations.
     */
    @Command(aliases = "/cancel",
             desc = "Cancel your own concurrently running or queued operations")
    @CommandPermissions("worldedit.operation.cancel.self")
    public void cancelOwn(LocalPlayer player) throws CommandException {
        List<QueuedOperation> queue = worldEdit.getExecutor().getQueue();
        List<QueuedOperation> cancelled = new ArrayList<QueuedOperation>();
        
        // Cancel operations owned by the player
        for (QueuedOperation queued : queue) {
            PlayerIssuedOperation info = queued.getMetadata(PlayerIssuedOperation.class);
            if (info != null && info.getOwner().equals(player)) {
                if (queued.cancel()) {
                    cancelled.add(queued);
                }
            }
        }
        
        if (cancelled.size() == 0) {
            throw new CommandException("You had no queued or running operations to cancel.");
        }
        
        MessageBuilder builder = worldEdit.createMessage();
        MessageBox box = new MessageBox();
        box.getContents()
                .append(cancelled.size()).append(" operation(s) of yours were cancelled.")
                .append(new OperationListFragment(cancelled, true));
        builder.append(box);
        player.print(builder);
    }

}
