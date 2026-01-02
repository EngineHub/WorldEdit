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

/**
 * Argument extracted from a command string.
 *
 * <p>
 * The original start and end may combine to form a length longer than the parsed text. This is because quotes and
 * escapes are removed from the parsed text. Suggestions replacing such arguments must be handled with care.
 * </p>
 *
 * @param parsedText the parsed text, with escapes removed and no quotes wrapping originally quoted text
 * @param originalStart the start index of the argument in the original string, which may be the index of a quote
 * @param originalEnd the end index of the argument in the original string, which may be the index of a quote
 */
public record ExtractedArg(
    String parsedText,
    int originalStart,
    int originalEnd
) {
}
