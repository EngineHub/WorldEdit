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

package com.sk89q.worldedit.util.command.argument;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

public final class ArgumentUtils {

    private ArgumentUtils() {
    }

    public static List<String> getMatchingSuggestions(Collection<String> items, String s) {
        if (s.isEmpty()) {
            return Lists.newArrayList(items);
        }
        List<String> suggestions = Lists.newArrayList();
        for (String item : items) {
            if (item.toLowerCase().startsWith(s)) {
                suggestions.add(item);
            }
        }
        return suggestions;
    }

}
