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

package com.sk89q.worldedit.extension.input;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;

/**
 * Thrown when parsed input results in an error.
 */
public class InputParseException extends WorldEditException {

    /**
     * Throw with a message.
     *
     * @param message the message
     */
    public InputParseException(Component message) {
        super(message);
    }

    /**
     * Throw with a message.
     *
     * @param message the message
     */
    @Deprecated
    public InputParseException(String message) {
        super(message);
    }

    /**
     * Throw with a message and a cause.
     *
     * @param message the message
     * @param cause the cause
     */
    public InputParseException(Component message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Throw with a message and a cause.
     *
     * @param message the message
     * @param cause the cause
     */
    @Deprecated
    public InputParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
