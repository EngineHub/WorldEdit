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

package com.sk89q.worldedit.world;

import com.google.common.annotations.Beta;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;

/**
 * This entire class is subject to heavy changes. Do not use this as API.
 */
@Beta
public interface DataFixer {

    final class FixType<T> {
        private FixType() {
        }
    }

    final class FixTypes {
        private FixTypes() {
        }

        public static FixType<CompoundBinaryTag> CHUNK = new FixType<>();
        public static FixType<CompoundBinaryTag> BLOCK_ENTITY = new FixType<>();
        public static FixType<CompoundBinaryTag> ENTITY = new FixType<>();
        public static FixType<String> BLOCK_STATE = new FixType<>();
        public static FixType<String> BIOME = new FixType<>();
        public static FixType<String> ITEM_TYPE = new FixType<>();
    }

    default <T> T fixUp(FixType<T> type, T original) {
        return fixUp(type, original, -1);
    }

    <T> T fixUp(FixType<T> type, T original, int srcVer);
}
