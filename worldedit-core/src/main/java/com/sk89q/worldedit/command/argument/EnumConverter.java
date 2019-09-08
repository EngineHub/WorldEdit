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

package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.util.TreeGenerator;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.MultiKeyConverter;
import org.enginehub.piston.inject.Key;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public final class EnumConverter {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(SelectorChoice.class),
            basic(SelectorChoice.class));
        commandManager.registerConverter(Key.of(TreeGenerator.TreeType.class),
            full(TreeGenerator.TreeType.class,
                t -> ImmutableSet.copyOf(t.lookupKeys),
                null));
        commandManager.registerConverter(Key.of(EditSession.ReorderMode.class),
            full(EditSession.ReorderMode.class,
                r -> ImmutableSet.of(r.getDisplayName()),
                null));
    }

    private static <E extends Enum<E>> ArgumentConverter<E> basic(Class<E> enumClass) {
        return full(enumClass, e -> ImmutableSet.of(e.name().toLowerCase(Locale.ROOT)), null);
    }

    private static <E extends Enum<E>> ArgumentConverter<E> basic(Class<E> enumClass, @Nullable E unknownValue) {
        return full(enumClass, e -> ImmutableSet.of(e.name().toLowerCase(Locale.ROOT)), unknownValue);
    }

    private static <E extends Enum<E>> ArgumentConverter<E> full(Class<E> enumClass,
                                                                 Function<E, Set<String>> lookupKeys,
                                                                 @Nullable E unknownValue) {
        return MultiKeyConverter.from(
            EnumSet.allOf(enumClass),
            lookupKeys,
            unknownValue
        );
    }

    private EnumConverter() {
    }

}
