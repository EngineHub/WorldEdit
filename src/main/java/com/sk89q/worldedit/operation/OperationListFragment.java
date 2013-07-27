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

import java.util.List;

import com.sk89q.rebar.formatting.Style;
import com.sk89q.rebar.formatting.StyledFragment;

/**
 * Shows a list of operations in a message.
 */
public class OperationListFragment extends StyledFragment {
    
    /**
     * Create a new fragment for the given list of operations.
     * 
     * @param operations the list of operations
     * @param beginNewLine true to begin on a new line if there are operations to list
     */
    public OperationListFragment(List<QueuedOperation> operations, boolean beginNewLine) {
        int i = 0;
        for (QueuedOperation queued : operations) {
            StyledFragment name = new StyledFragment(Style.YELLOW);
            StyledFragment owner = new StyledFragment();
            StyledFragment status = new StyledFragment(Style.CYAN);
            status.append(queued.getState().name());
            
            // Get the object that may contain extra information
            PlayerIssuedOperation info = queued.getMetadata(PlayerIssuedOperation.class);
            if (info != null) {
                name.append(info.getLabel());

                owner.append(", owner=");
                owner.createFragment(Style.GREEN).append(info.getOwner().getName());
            } else {
                name.append(queued.getOperation().toString());
            }
            
            if (i > 0 || beginNewLine) {
                newLine();
            }
            
            append("#").append(i + 1).append(". ");
            append(name);
            append(owner);
            append(", status=");
            append(status);

            i++;
        }
    }

}
