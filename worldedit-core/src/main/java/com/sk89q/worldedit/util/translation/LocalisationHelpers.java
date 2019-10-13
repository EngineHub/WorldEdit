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

package com.sk89q.worldedit.util.translation;

public class LocalisationHelpers {

    private LocalisationHelpers() {
    }

    /**
     * Turn a translation key into a ".singular" or ".plural"
     * depending on what the given number is.
     *
     * @param translationKey The base translation key
     * @param number The number
     * @return The key with .plural or .singular appended
     */
    public static String pluraliseI18n(String translationKey, float number) {
        if (number == 1) {
            return translationKey + ".singular";
        } else {
            return translationKey + ".plural";
        }
    }
}
