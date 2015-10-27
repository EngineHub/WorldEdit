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

import java.util.List;

/**
 * A description of a command.
 */
public interface Description {

    /**
     * Get the list of parameters for this command.
     * 
     * @return a list of parameters
     */
    List<Parameter> getParameters();

    /**
     * Get a short one-line description of this command.
     * 
     * @return a description, or null if no description is available
     */
    String getDescription();

    /**
     * Get a longer help text about this command.
     * 
     * @return a help text, or null if no help is available
     */
    String getHelp();

    /**
     * Get the usage string of this command.
     * 
     * <p>A usage string may look like 
     * {@code [-w &lt;world&gt;] &lt;var1&gt; &lt;var2&gt;}.</p>
     * 
     * @return a usage string
     */
    String getUsage();
    
    /**
     * Get a list of permissions that the player may have to have permission.
     * 
     * <p>Permission data may or may not be available. This is only useful as a
     * potential hint.</p>
     * 
     * @return the list of permissions
     */
    List<String> getPermissions();

}
