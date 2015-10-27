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
 * A simple implementation of {@link Parameter} that has setters.
 */
public class SimpleParameter implements Parameter {
    
    private String name;
    private Character flag;
    private boolean isValue;
    private boolean isOptional;
    private String[] defaultValue;
    
    /**
     * Create a new parameter with no name defined yet.
     */
    public SimpleParameter() {
    }
    
    /**
     * Create a new parameter of the given name.
     * 
     * @param name the name
     */
    public SimpleParameter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name of the parameter.
     *
     * @param name the parameter name
     */
    public SimpleParameter setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Character getFlag() {
        return flag;
    }
    
    @Override
    public boolean isValueFlag() {
        return flag != null && isValue;
    }

    /**
     * Set the flag used by this parameter.
     *  @param flag the flag, or null if there is no flag
     * @param isValue true if the flag is a value flag
     */
    public SimpleParameter setFlag(Character flag, boolean isValue) {
        this.flag = flag;
        this.isValue = isValue;
        return this;
    }
    
    @Override
    public boolean isOptional() {
        return isOptional || getFlag() != null;
    }

    /**
     * Set whether this parameter is optional.
     *
     * @param isOptional true if this parameter is optional
     */
    public SimpleParameter setOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }
    
    @Override
    public String[] getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value.
     *
     * @param defaultValue a default value, or null if none
     */
    public SimpleParameter setDefaultValue(String[] defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (getFlag() != null) {
            if (isValueFlag()) {
                builder.append("[-")
                        .append(getFlag()).append(" <").append(getName()).append(">]");
            } else {
                builder.append("[-").append(getFlag()).append("]");
            }
        } else {
            if (isOptional()) {
                builder.append("[<").append(getName()).append(">]");
            } else {
                builder.append("<").append(getName()).append(">");
            }
        }
        return builder.toString();
    }

}
