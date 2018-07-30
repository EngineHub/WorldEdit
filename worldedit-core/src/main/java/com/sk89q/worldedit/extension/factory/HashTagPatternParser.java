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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.pattern.ClipboardPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.session.ClipboardHolder;

class HashTagPatternParser extends InputParser<Pattern> {

    HashTagPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        if (input.charAt(0) == '#') {
            if (!input.equals("#clipboard") && !input.equals("#copy")) {
                throw new InputParseException("#clipboard or #copy is acceptable for patterns starting with #");
            }

            LocalSession session = context.requireSession();

            if (session != null) {
                try {
                    ClipboardHolder holder = session.getClipboard();
                    Clipboard clipboard = holder.getClipboard();
                    return new ClipboardPattern(clipboard);
                } catch (EmptyClipboardException e) {
                    throw new InputParseException("To use #clipboard, please first copy something to your clipboard");
                }
            } else {
                throw new InputParseException("No session is available, so no clipboard is available");
            }
        } else {
            return null;
        }
    }

}
