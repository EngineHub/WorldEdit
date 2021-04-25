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

package com.sk89q.worldedit.extent.clipboard.io.share;

import com.sk89q.worldedit.WorldEdit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ClipboardShareDestinations {

    private static final Map<String, ClipboardShareDestination> aliasMap = new HashMap<>();
    private static final List<ClipboardShareDestination> registeredDestinations = new ArrayList<>();

    public static void registerClipboardShareDestination(ClipboardShareDestination destination) {
        checkNotNull(destination);
        checkState(destination.supportsFormat(destination.getDefaultFormat()), "Destination must accept its default format");

        for (String key : destination.getAliases()) {
            String lowKey = key.toLowerCase(Locale.ROOT);
            ClipboardShareDestination old = aliasMap.put(lowKey, destination);
            if (old != null) {
                aliasMap.put(lowKey, old);
                WorldEdit.logger.warn(destination.getClass().getName() + " cannot override existing alias '" + lowKey + "' used by " + old.getClass().getName());
            }
        }
        registeredDestinations.add(destination);
    }

    static {
        for (BuiltInClipboardShareDestinations destination : BuiltInClipboardShareDestinations.values()) {
            registerClipboardShareDestination(destination);
        }
    }

    /**
     * Find the clipboard format named by the given alias.
     *
     * @param alias the alias
     * @return the format, otherwise null if none is matched
     */
    @Nullable
    public static ClipboardShareDestination findByAlias(String alias) {
        checkNotNull(alias);
        return aliasMap.get(alias.toLowerCase(Locale.ROOT).trim());
    }

    public static Collection<ClipboardShareDestination> getAll() {
        return Collections.unmodifiableCollection(registeredDestinations);
    }

    private ClipboardShareDestinations() {
    }
}
