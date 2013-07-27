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

package com.sk89q.rebar.util;

/**
 * Indicates an object that can have additional metadata attached.
 */
public interface Describable {

    /**
     * Get metadata that has been assigned to this object.
     * 
     * <p>Metadata can be used to attach information. Metadata is referenced by
     * the given class and providing a parent class will not return a child class.</p>
     * 
     * @param metadataClass the metadata class
     * @return an instance of the given class, or null if none can be found
     */
    <T> T getMetadata(Class<T> metadataClass);
    
    /**
     * Set metadata on this object.
     * 
     * <p>Metadata can be used to attach information. Metadata is referenced by
     * the given class and providing a parent class will not return a child class.</p>
     * 
     * @param metadata the object to set
     */
    void setMetadata(Object metadata);

}
