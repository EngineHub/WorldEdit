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

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * The colors for wool.
 *
 * <p>This class may be removed in the future.</p>
 */
public enum ClothColor {

    WHITE(ID.WHITE, "White", "white"),
    ORANGE(ID.ORANGE, "Orange", "orange"),
    MAGENTA(ID.MAGENTA, "Magenta", "magenta"),
    LIGHT_BLUE(ID.LIGHT_BLUE, "Light blue", "lightblue"),
    YELLOW(ID.YELLOW, "Yellow", "yellow"),
    LIGHT_GREEN(ID.LIGHT_GREEN, "Light green", "lightgreen"),
    PINK(ID.PINK, "Pink", new String[] { "pink", "lightred" }),
    GRAY(ID.GRAY, "Gray", new String[] { "grey", "gray" }),
    LIGHT_GRAY(ID.LIGHT_GRAY, "Light gray", new String[] { "lightgrey", "lightgray" }),
    CYAN(ID.CYAN, "Cyan", new String[] { "cyan", "turquoise" }),
    PURPLE(ID.PURPLE, "Purple", new String[] { "purple", "violet" }),
    BLUE(ID.BLUE, "Blue", "blue"),
    BROWN(ID.BROWN, "Brown", new String[] { "brown", "cocoa", "coffee" }),
    DARK_GREEN(ID.DARK_GREEN, "Dark green", new String[] { "green", "darkgreen", "cactusgreen", "cactigreen" }),
    RED(ID.RED, "Red", "red"),
    BLACK(ID.BLACK, "Black", "black");

    public static final class ID {
        public static final int WHITE = 0;
        public static final int ORANGE = 1;
        public static final int MAGENTA = 2;
        public static final int LIGHT_BLUE = 3;
        public static final int YELLOW = 4;
        public static final int LIGHT_GREEN = 5;
        public static final int PINK = 6;
        public static final int GRAY = 7;
        public static final int LIGHT_GRAY = 8;
        public static final int CYAN = 9;
        public static final int PURPLE = 10;
        public static final int BLUE = 11;
        public static final int BROWN = 12;
        public static final int DARK_GREEN = 13;
        public static final int RED = 14;
        public static final int BLACK = 15;

        private ID() {
        }
    }

    /**
     * Stores a map of the IDs for fast access.
     */
    private static final Map<Integer, ClothColor> ids;
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String, ClothColor> lookup;

    private final int id;
    private final String name;
    private final String[] lookupKeys;

    static {
        ImmutableMap.Builder<Integer, ClothColor> iBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, ClothColor> lBuilder = ImmutableMap.builder();
        for (ClothColor type : values()) {
            iBuilder.put(type.id, type);
            for (String key : type.lookupKeys) {
                lBuilder.put(key, type);
            }
        }
        ids = iBuilder.build();
        lookup = lBuilder.build();
    }


    /**
     * Construct the type.
     *
     * @param id the ID of the color
     * @param name the name of the color
     * @param lookupKey a name to refer to the color by
     */
    ClothColor(int id, String name, String lookupKey) {
        this.id = id;
        this.name = name;
        this.lookupKeys = new String[] { lookupKey };
    }

    /**
     * Construct the type.
     *
     * @param id the ID of the color
     * @param name the name of the color
     * @param lookupKeys an array of lookup keys
     */
    ClothColor(int id, String name, String[] lookupKeys) {
        this.id = id;
        this.name = name;
        this.lookupKeys = lookupKeys;
    }

    /**
     * Return type from ID. May return null.
     *
     * @param id the ID
     * @return a color or null
     */
    @Nullable
    public static ClothColor fromID(int id) {
        return ids.get(id);
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
     * Get item numeric ID.
     *
     * @return the ID
     */
    public int getID() {
        return id;
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
