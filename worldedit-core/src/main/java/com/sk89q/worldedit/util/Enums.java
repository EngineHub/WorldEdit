/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper methods for enums.
 */
public final class Enums {

    private Enums() {
    }

    /**
     * Search the given enum for a value that is equal to the one of the
     * given values, searching in an ascending manner.
     *
     * @param enumType the enum type
     * @param values the list of values
     * @param <T> the type of enum
     * @return the found value or null
     */
    @Nullable
    public static <T extends Enum<T>> T findByValue(Class<T> enumType, String... values) {
        checkNotNull(enumType);
        checkNotNull(values);
        for (String val : values) {
            try {
                return Enum.valueOf(enumType, val);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
