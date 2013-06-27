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

package com.sk89q.worldedit.changelog;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.foundation.Extent;

/**
 * A change of a block from one copy to another.
 */
public class BlockChange implements ReversibleChange {
    
    private Vector location;
    private BaseBlock previous;
    private BaseBlock replacement;
    
    /**
     * Create a new instance.
     */
    public BlockChange() {
    }
    
    /**
     * Create a new instance.
     * 
     * @param location the location of the block
     * @param previous the previous block
     * @param replacement the replacement block
     */
    public BlockChange(Vector location, BaseBlock previous, BaseBlock replacement) {
        this.location = location;
        this.previous = previous;
        this.replacement = replacement;
    }

    /**
     * Get the location of the change.
     * 
     * @return the location
     */
    public Vector getLocation() {
        return location;
    }

    /**
     * Set the location of the change.
     * 
     * @param location the location
     */
    public void setLocation(Vector location) {
        this.location = location;
    }

    /**
     * Get the previous block.
     * 
     * @return the previous block
     */
    public BaseBlock getPrevious() {
        return previous;
    }

    /**
     * Set the previous block.
     * 
     * @param previous the previous block
     */
    public void setPrevious(BaseBlock previous) {
        this.previous = previous;
    }

    /**
     * Get the replacement block.
     * 
     * @return the replacement block
     */
    public BaseBlock getReplacement() {
        return replacement;
    }

    /**
     * Set the replacement block.
     * 
     * @param replacement the replacement block
     */
    public void setReplacement(BaseBlock replacement) {
        this.replacement = replacement;
    }

    @Override
    public void revert(Extent extent) {
        extent.setBlock(location, previous);
    }

    @Override
    public void apply(Extent extent) {
        extent.setBlock(location, replacement);
    }

    @Override
    public BlockChange clone() {
        return new BlockChange(getLocation(), getPrevious(), getReplacement());
    }
    
}
