/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.command;

/**
 * Describes a parameter.
 * 
 * @see Description
 */
public interface Parameter {

    /**
     * The name of the parameter.
     * 
     * @return the name
     */
    String getName();

    /**
     * Get the flag associated with this parameter.
     * 
     * @return the flag, or null if there is no flag associated
     * @see #isValueFlag()
     */
    Character getFlag();
    
    /**
     * Return whether the flag is a value flag.
     * 
     * @return true if the flag is a value flag
     * @see #getFlag()
     */
    boolean isValueFlag();
    
    /**
     * Get whether this parameter is optional.
     * 
     * @return true if the parameter does not have to be specified
     */
    boolean isOptional();
    
    /**
     * Get the default value as a string to be parsed by the binding.
     * 
     * @return a default value, or null if none is set
     */
    String[] getDefaultValue();

}
