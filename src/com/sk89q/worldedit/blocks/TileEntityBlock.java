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

package com.sk89q.worldedit.blocks;

import com.sk89q.worldedit.SchematicException;
import java.util.Map;
import org.jnbt.Tag;

/**
 * A class implementing this interface has extra TileEntityBlock data to store.
 *
 * @author sk89q
 */
public interface TileEntityBlock {
    /**
     * Return the name of the title entity ID.
     * 
     * @return title entity ID
     */
    public String getTileEntityID();
    /**
     * Store additional tile entity data.
     *
     * @return map of values
     * @throws SchematicException
     */
    public Map<String,Tag> toTileEntityNBT()
            throws SchematicException ;
    /**
     * Get additional information from the title entity data.
     * 
     * @param values
     * @throws SchematicException
     */
    public void fromTileEntityNBT(Map<String,Tag> values)
            throws SchematicException ;
}
