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

package com.sk89q.worldedit.util.formatting.component;

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

/**
 * Represents a fragment representing an error.
 */
public class ErrorFormat extends TextComponentProducer {

    /**
     * Create a new instance.
     */
    private ErrorFormat() {
        getBuilder().content("").color(TextColor.RED);
    }

    /**
     * Creates an ErrorFormat with the given message.
     *
     * @param texts The text
     * @return The Component
     */
    public static TextComponent wrap(String... texts) {
        ErrorFormat error = new ErrorFormat();
        for (String component : texts) {
            error.append(component);
        }

        return error.create();
    }
}
