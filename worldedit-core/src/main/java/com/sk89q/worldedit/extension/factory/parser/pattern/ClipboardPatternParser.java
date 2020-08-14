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

package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.pattern.ClipboardPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import java.util.Locale;
import java.util.stream.Stream;

public class ClipboardPatternParser extends InputParser<Pattern> {

    public ClipboardPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input) {
        if (input.isEmpty()) {
            return Stream.of("#clipboard");
        }
        String[] offsetParts = input.split("@", 2);
        String firstLower = offsetParts[0].toLowerCase(Locale.ROOT);
        final boolean isClip = "#clipboard".startsWith(firstLower);
        final boolean isCopy = "#copy".startsWith(firstLower);
        if (isClip || isCopy) {
            if (offsetParts.length == 2) {
                String coords = offsetParts[1];
                if (coords.isEmpty()) {
                    return Stream.of(input + "[x,y,z]");
                }
            } else {
                if (isClip) {
                    return Stream.of("#clipboard", "#clipboard@[x,y,z]");
                }
                return Stream.of("#copy", "#copy@[x,y,z]");
            }
        }
        return Stream.empty();
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        String[] offsetParts = input.split("@", 2);
        if (!offsetParts[0].equalsIgnoreCase("#clipboard") && !offsetParts[0].equalsIgnoreCase("#copy")) {
            return null;
        }
        LocalSession session = context.requireSession();

        BlockVector3 offset = BlockVector3.ZERO;
        if (offsetParts.length == 2) {
            String coords = offsetParts[1];
            if (coords.length() < 7  // min length of `[x,y,z]`
                || coords.charAt(0) != '[' || coords.charAt(coords.length() - 1) != ']') {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.clipboard.missing-offset"));
            }
            String[] offsetSplit = coords.substring(1, coords.length() - 1).split(",");
            if (offsetSplit.length != 3) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.clipboard.missing-coordinates"));
            }
            offset = BlockVector3.at(
                    Integer.parseInt(offsetSplit[0]),
                    Integer.parseInt(offsetSplit[1]),
                    Integer.parseInt(offsetSplit[2])
            );
        }

        if (session != null) {
            try {
                ClipboardHolder holder = session.getClipboard();
                Clipboard clipboard = holder.getClipboard();
                return new ClipboardPattern(clipboard, offset);
            } catch (EmptyClipboardException e) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.empty-clipboard"));
            }
        } else {
            throw new InputParseException(TranslatableComponent.of("worldedit.error.missing-session"));
        }
    }

}
