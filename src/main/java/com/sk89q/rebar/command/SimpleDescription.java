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

package com.sk89q.rebar.command;

import java.util.Collections;
import java.util.List;

/**
 * A simple implementation of {@link Description} which has setters.
 */
public class SimpleDescription implements Description {
    
    private List<Parameter> parameters = Collections.emptyList();
    private List<String> permissions = Collections.emptyList();
    private String description;
    private String help;
    private String overrideUsage;
    
    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Set the list of parameters.
     * 
     * @param parameters the list of parameters
     * @see #getParameters()
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = Collections.unmodifiableList(parameters);
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description of the command.
     * 
     * @param description the description
     * @see #getDescription()
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String getHelp() {
        return help;
    }
    
    /**
     * Set the help text of the command.
     * 
     * @param help the help text
     * @see #getHelp()
     */
    public void setHelp(String help) {
        this.help = help;
    }

    @Override
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Set the permissions of this command.
     * 
     * @param permissions the permissions
     */
    public void setPermissions(List<String> permissions) {
        this.permissions = Collections.unmodifiableList(permissions);
    }
    
    /**
     * Override the usage string returned with a given one.
     * 
     * @param usage usage string, or null
     */
    public void overrideUsage(String usage) {
        this.overrideUsage = usage;
    }

    @Override
    public String getUsage() {
        if (overrideUsage != null) {
            return overrideUsage;
        }
        
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        
        for (Parameter parameter : parameters) {
            if (!first) {
                builder.append(" ");
            }
            builder.append(parameter.toString());
            first = false;
        }
        
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return getUsage();
    }

}
