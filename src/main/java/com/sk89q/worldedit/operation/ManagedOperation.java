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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;

/**
 * Handles flushing {@link EditSession}s, storing changes to history, and 
 * other important things for an {@link Operation}.
 */
public class ManagedOperation extends OperationWrapper {
    
    private final EditSession editSession;
    private final WorldEdit worldEdit;
    private final LocalPlayer player;
    private boolean hasFlushed = false;
    
    /**
     * Create a new managed operation.
     * 
     * @param worldEdit WorldEdit instance
     * @param editSession the edit session
     * @param player the player
     * @param operation the operation to wrap
     */
    public ManagedOperation(WorldEdit worldEdit, EditSession editSession,
            LocalPlayer player, Operation operation) {
        super(operation);
        this.editSession = editSession;
        this.worldEdit = worldEdit;
        this.player = player;
    }

    /**
     * Flush changes after the operation is completed.
     */
    private void flush() {
        LocalSession session = worldEdit.getSessions().get(player);
        session.remember(editSession);
    }

    @Override
    protected void onResume(Operation current, Operation next, boolean success) {
    }

    @Override
    public void onFailure(Throwable t) {
        flush();
    }

    @Override
    public void onSuccess(Operation operation) {
        flush();
    }

    @Override
    protected Operation nextOperation(boolean success) {
        if (hasFlushed) {
            return null;
        }
        
        hasFlushed = true;
        return editSession.getFlushOperation();
    }

}
