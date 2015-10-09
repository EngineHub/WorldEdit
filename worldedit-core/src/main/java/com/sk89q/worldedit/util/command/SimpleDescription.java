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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of {@link Description} which has setters.
 */
public class SimpleDescription implements Description {
    
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private List<String> permissions = new ArrayList<String>();
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
        this.parameters = ImmutableList.copyOf(parameters);
    }

    @Override
    public String getShortDescription() {
        return description;
    }
    
    /**
     * Set the description of the command.
     * 
     * @param description the description
     * @see #getShortDescription()
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
        this.permissions = ImmutableList.copyOf(permissions);
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
            builder.append(parameter);
            first = false;
        }
        
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return getUsage();
    }

}
