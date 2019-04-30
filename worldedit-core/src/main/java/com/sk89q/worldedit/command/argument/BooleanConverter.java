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

import com.google.common.collect.ImmutableSetMultimap;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.MultiKeyConverter;
import org.enginehub.piston.inject.Key;

public class BooleanConverter {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(Boolean.class),
            MultiKeyConverter.builder(
                ImmutableSetMultimap.<Boolean, String>builder()
                    .putAll(false, "off", "f", "false", "n", "no")
                    .putAll(true, "on", "t", "true", "y", "yes")
                    .build()
            )
                .errorMessage(arg -> "Not a boolean value: " + arg)
                .build()
        );
    }

    private BooleanConverter() {
    }

}
