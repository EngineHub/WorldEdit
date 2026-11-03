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

package com.sk89q.worldedit.internal.command;

import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;

import java.util.Locale;

public class CommandArgParseException extends RuntimeException {
    private final Component richMessage;

    public CommandArgParseException(Component richMessage) {
        this.richMessage = richMessage;
    }

    public Component getRichMessage() {
        return richMessage;
    }

    @Override
    public String getMessage() {
        // Computed lazily, since this is thrown on many tab-completions where the message is never used
        return WorldEditText.reduceToText(richMessage, Locale.getDefault());
    }
}
