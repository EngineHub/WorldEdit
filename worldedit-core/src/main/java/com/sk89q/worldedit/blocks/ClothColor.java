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

package com.sk89q.worldedit.blocks;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

/**
 * The colors for wool.
 *
 * <p>This class may be removed in the future.</p>
 */
public enum ClothColor {

    WHITE("White", "white"),
    ORANGE("Orange", "orange"),
    MAGENTA("Magenta", "magenta"),
    LIGHT_BLUE("Light blue", "lightblue"),
    YELLOW("Yellow", "yellow"),
    LIGHT_GREEN("Light green", "lightgreen"),
    PINK("Pink", "pink", "lightred"),
    GRAY("Gray", "grey", "gray"),
    LIGHT_GRAY("Light gray", "lightgrey", "lightgray"),
    CYAN("Cyan", "cyan", "turquoise"),
    PURPLE("Purple", "purple", "violet"),
    BLUE("Blue", "blue"),
    BROWN("Brown", "brown", "cocoa", "coffee"),
    DARK_GREEN("Dark green", "green", "darkgreen", "cactusgreen", "cactigreen"),
    RED("Red", "red"),
    BLACK("Black", "black");
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String, ClothColor> lookup = new HashMap<>();

    private final String name;
    private final String[] lookupKeys;

    static {
        for (ClothColor type : EnumSet.allOf(ClothColor.class)) {
            for (String key : type.lookupKeys) {
                lookup.put(key, type);
            }
        }
    }


    /**
     * Construct the type.
     *
     * @param name the name of the color
     * @param lookupKeys a name to refer to the color by
     */
    ClothColor(String name, String ... lookupKeys) {
        this.name = name;
        this.lookupKeys = lookupKeys;
    }

    /**
     * Return type from name. May return null.
     *
     * @param name the name of the color
     * @return a color or null
     */
    @Nullable
    public static ClothColor lookup(String name) {
        return lookup.get(name.toLowerCase());
    }

    /**
     * Get user-friendly item name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

}
