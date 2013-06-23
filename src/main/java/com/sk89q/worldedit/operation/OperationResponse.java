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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.FutureCallback;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEdit;

/**
 * Informs a player on the status of an operation.
 */
public class OperationResponse implements FutureCallback<Operation> {

    private static final Logger logger = Logger.getLogger(
            OperationResponse.class.getCanonicalName());

    private final WorldEdit worldEdit;
    private final LocalPlayer player;

    /**
     * Create a new instance.
     *
     * @param worldEdit a copy of WorldEdit
     * @param player a player
     */
    public OperationResponse(WorldEdit worldEdit, LocalPlayer player) {
        this.worldEdit = worldEdit;
        this.player = player;
    }

    @Override
    public void onSuccess(Operation operation) {
        if (operation instanceof ChangeCountable) {
            int affected = ((ChangeCountable) operation).getChangeCount();
            player.print(operation.getClass().getSimpleName() + ": " +
                    affected + " blocks were changed.");
        } else {
            player.print(operation.getClass().getSimpleName() + ": completed.");
        }
    }

    @Override
    public void onFailure(Throwable thrown) {
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
    
}
