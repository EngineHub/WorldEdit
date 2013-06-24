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

package com.sk89q.worldedit.operation;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.FutureCallback;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.rebar.formatting.MessageBuilder;
import com.sk89q.rebar.formatting.Style;
import com.sk89q.rebar.formatting.StyledFragment;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEdit;

/**
 * Informs a player on the status of an operation.
 */
public class OperationResponse extends TimerTask implements FutureCallback<Operation> {

    private static final int QUEUE_MESSAGE_DELAY = 200;
    private static final Logger logger = Logger.getLogger(
            OperationResponse.class.getCanonicalName());
    private static final Timer timer = new Timer();

    private final WorldEdit worldEdit;
    private final LocalPlayer player;
    private final QueuedOperation queued;
    private boolean sentCompletion = false;

    /**
     * Create a new instance.
     *
     * @param worldEdit a copy of WorldEdit
     * @param player a player
     * @param queued the queued operation
     */
    public OperationResponse(WorldEdit worldEdit, LocalPlayer player, 
            QueuedOperation queued) {
        this.worldEdit = worldEdit;
        this.player = player;
        this.queued = queued;
    }
    
    /**
     * Get the label to describe the operation.
     * 
     * @return the operation label
     */
    private String getOperationLabel() {
        String name = queued.getOperation().getClass().getSimpleName();
        
        // Get the label from a PlayerIssuedOperation
        PlayerIssuedOperation info = queued.getMetadata(PlayerIssuedOperation.class);
        if (info != null) {
            name = info.getLabel();
        }
        
        return name;
    }

    /**
     * Schedule this response with the given server so that a "... has been queued"
     * message is sent after a delay, but only if the operation has not yet completed.
     */
    public void schedule() {
        timer.schedule(this, QUEUE_MESSAGE_DELAY);
    }

    @Override
    public void onSuccess(Operation operation) {
        this.sentCompletion = true;
        
        if (operation instanceof ChangeCountable) {
            int affected = ((ChangeCountable) operation).getChangeCount();

            MessageBuilder message = worldEdit.createMessage();
            message.append(getOperationLabel()).append(" completed with ")
                    .append(new StyledFragment(Style.CYAN).append(affected))
                    .append(" objects(s) changed.");
            player.print(message);
        } else {
            MessageBuilder message = worldEdit.createMessage();
            message.append(getOperationLabel()).append(" completed successfully.");
            player.print(message);
        }
    }

    @Override
    public void onFailure(Throwable thrown) {
        this.sentCompletion = true;
        
        if (thrown instanceof InterruptedException) {
            return; // Cancelled or so
        }
        
        try {
            worldEdit.getExceptionConverter().convert(thrown);
        } catch (WrappedCommandException e) {
            thrown = e;
        } catch (CommandException e) {
            player.printError(e.getMessage());
        }

        player.printError("An error occurred (see console for a full error message):\n"
                + thrown.getMessage());

        logger.log(Level.SEVERE,
                "An error occurred while executing an operation: "
                        + thrown.getMessage(), thrown);
    }

    @Override
    public void run() {
        if (!sentCompletion) {
            MessageBuilder message = new MessageBuilder(Style.GRAY);
            message
                    .append("(Your ").append(getOperationLabel())
                    .append(" is currently ")
                    .append(new StyledFragment(Style.CYAN).append(queued.getState().name()))
                    .append(".)");
            player.print(message);
        }
    }
    
}
