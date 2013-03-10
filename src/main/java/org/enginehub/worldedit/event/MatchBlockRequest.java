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

package org.enginehub.worldedit.event;

import org.enginehub.event.ExecutorList;
import org.enginehub.event.Request;

import com.sk89q.worldedit.foundation.Block;

/**
 * A request to match a block string.
 */
public class MatchBlockRequest extends Request<Block> {
    
    private static final ExecutorList<MatchBlockRequest> executors = 
            new ExecutorList<MatchBlockRequest>();
    
    private String name;

    protected MatchBlockRequest() {
    }

    /**
     * Create a new request.
     * 
     * @param name the name of the block
     */
    public MatchBlockRequest(String name) {
        this.name = name;
    }

    /**
     * Get the name of the block being requested.
     * 
     * <p>A name might be "cobblestone" or "woodplanks".</p>
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    protected ExecutorList<MatchBlockRequest> getExecutors() {
        return executors;
    }

}
