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

package com.sk89q.worldedit.extension.factory.parser.mask;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MatchMask;
import com.sk89q.worldedit.internal.registry.SimpleInputParser;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import java.util.List;

public class ClipboardMaskParser extends SimpleInputParser<Mask> {

    private static final List<String> aliases = List.of("#clipboard", "#copy");

    public ClipboardMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public List<String> getMatchedAliases() {
        return aliases;
    }

    @Override
    public Mask parseFromSimpleInput(String input, ParserContext context) throws InputParseException {
        try {
            return new MatchMask(context.requireExtent(), context.requireSession().getClipboard().getClipboard());
        } catch (EmptyClipboardException e) {
            throw new InputParseException(TranslatableComponent.of("worldedit.error.empty-clipboard"));
        }
    }
}
