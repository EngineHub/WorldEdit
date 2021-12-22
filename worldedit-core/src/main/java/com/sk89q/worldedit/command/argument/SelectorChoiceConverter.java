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

package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.MultiKeyConverter;
import org.enginehub.piston.inject.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SelectorChoiceConverter {
    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(SelectorChoice.class), MultiKeyConverter.from(getBasicItems()));
        commandManager.registerConverter(Key.of(SelectorChoiceOrList.class), orListConverter());
    }

    private static ArgumentConverter<SelectorChoiceOrList> orListConverter() {
        SetMultimap<SelectorChoiceOrList, String> items = LinkedHashMultimap.create(getBasicItems());

        items.put(SelectorChoiceList.INSTANCE, "list");

        return MultiKeyConverter.from(
            ImmutableSetMultimap.copyOf(items),
            SelectorChoiceList.INSTANCE
        );
    }

    @NotNull
    private static SetMultimap<SelectorChoice, String> getBasicItems() {
        SetMultimap<SelectorChoice, String> items = LinkedHashMultimap.create();
        for (var item : SelectorChoice.values()) {
            items.put(item, item.name().toLowerCase(Locale.ROOT));
        }
        items.put(SelectorChoice.CONVEX, "hull");
        items.put(SelectorChoice.CONVEX, "polyhedron");
        return items;
    }
}
