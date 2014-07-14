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

package com.sk89q.worldedit.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Constants {

    private Constants() {
    }

    /**
     * List of top level NBT fields that should not be copied to a world,
     * such as UUIDLeast and UUIDMost.
     */
    public static final List<String> NO_COPY_ENTITY_NBT_FIELDS;

    static {
        NO_COPY_ENTITY_NBT_FIELDS = Collections.unmodifiableList(Arrays.asList(
                "UUIDLeast", "UUIDMost", // Bukkit and Vanilla
                "WorldUUIDLeast", "WorldUUIDMost", // Bukkit and Vanilla
                "PersistentIDMSB", "PersistentIDLSB" // Forge
        ));
    }

}
