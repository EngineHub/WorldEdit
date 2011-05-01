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

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

/**
 * Cloth colors.
 *
 * @author sk89q
 */
public enum ClothColor {
    WHITE(0, "White", "white"),
    ORANGE(1, "Orange", "orange"),
    MAGENTA(2, "Magenta", "magenta"),
    LIGHT_BLUE(3, "Light blue", "lightblue"),
    YELLOW(4, "Yellow", "yellow"),
    LIGHT_GREEN(5, "Light green", "lightgreen"),
    PINK(6, "Pink", new String[] {"pink", "lightred"}),
    GRAY(7, "Gray", new String[] {"grey", "gray"}),
    LIGHT_GRAY(8, "Light gray", new String[] {"lightgrey", "lightgray"}),
    CYAN(9, "Cyan", new String[] {"cyan", "turquoise"}),
    PURPLE(10, "Purple", new String[] {"purple", "violet"}),
    BLUE(11, "Blue", "blue"),
    BROWN(12, "Brown", new String[] {"brown", "cocoa", "coffee"}),
    DARK_GREEN(13, "Dark green", new String[] {"green", "darkgreen", "cactusgreen", "cactigreen"}),
    RED(14, "Red", "red"),
    BLACK(15, "Black", "black");

    /**
     * Stores a map of the IDs for fast access.
     */
    private static final Map<Integer,ClothColor> ids = new HashMap<Integer,ClothColor>();
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String,ClothColor> lookup = new HashMap<String,ClothColor>();

    private final int id;
    private final String name;
    private final String[] lookupKeys;

    static {
        for (ClothColor type : EnumSet.allOf(ClothColor.class)) {
            ids.put(type.id, type);
            for (String key : type.lookupKeys) {
                lookup.put(key, type);
            }
        }
    }


    /**
     * Construct the type.
     *
     * @param id
     * @param name
     */
    ClothColor(int id, String name, String lookupKey) {
        this.id = id;
        this.name = name;
        this.lookupKeys = new String[]{lookupKey};
    }

    /**
     * Construct the type.
     *
     * @param id
     * @param name
     */
    ClothColor(int id, String name, String[] lookupKeys) {
        this.id = id;
        this.name = name;
        this.lookupKeys = lookupKeys;
    }

    /**
     * Return type from ID. May return null.
     *
     * @param id
     * @return
     */
    public static ClothColor fromID(int id) {
        return ids.get(id);
    }

    /**
     * Return type from name. May return null.
     *
     * @param name
     * @return
     */
    public static ClothColor lookup(String name) {
        return lookup.get(name.toLowerCase());
    }

    /**
     * Get item numeric ID.
     *
     * @return
     */
    public int getID() {
        return id;
    }

    /**
     * Get user-friendly item name.
     *
     * @return
     */
    public String getName() {
        return name;
    }
}