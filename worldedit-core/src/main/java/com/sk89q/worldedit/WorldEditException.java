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

package com.sk89q.worldedit;

import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import java.util.Locale;

/**
 * Parent for all WorldEdit exceptions.
 */
public abstract class WorldEditException extends Exception {

    private final Component message;

    /**
     * Create a new exception.
     */
    protected WorldEditException() {
        this.message = null;
    }

    /**
     * Create a new exception with a message.
     *
     * @param message the message
     * @deprecated Use component version
     */
    @Deprecated
    protected WorldEditException(String message) {
        super(message);

        this.message = TextComponent.of(message);
    }

    /**
     * Create a new exception with a message.
     *
     * @param message the message
     */
    protected WorldEditException(Component message) {
        super(WorldEditText.reduceToText(message, Locale.getDefault()));

        this.message = message;
    }

    /**
     * Create a new exception with a message and a cause.
     *
     * @param message the message
     * @param cause the cause
     * @deprecated Use component version
     */
    @Deprecated
    protected WorldEditException(String message, Throwable cause) {
        super(message, cause);

        this.message = TextComponent.of(message);
    }

    /**
     * Create a new exception with a message and a cause.
     *
     * @param message the message
     * @param cause the cause
     */
    protected WorldEditException(Component message, Throwable cause) {
        super(WorldEditText.reduceToText(message, Locale.getDefault()), cause);

        this.message = message;
    }

    /**
     * Create a new exception with a cause.
     *
     * @param cause the cause
     */
    protected WorldEditException(Throwable cause) {
        super(cause);

        this.message = null;
    }

    /**
     * Get the message of this exception as a rich text component.
     *
     * @return The rich message
     */
    public Component getRichMessage() {
        return this.message;
    }
}
