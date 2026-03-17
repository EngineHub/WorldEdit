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

package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.DataException;
import org.enginehub.linbus.tree.LinCompoundTag;

/**
 * Strategy for creating a {@link Chunk} from NBT data.
 * Used to replace version-based conditionals with polymorphism.
 */
public interface ChunkFromTagLoader {

    /**
     * Whether this loader supports the given data version and tag structure.
     *
     * @param dataVersion the chunk data version
     * @param rootTag the root NBT tag
     * @return true if this loader can create a Chunk from the tag
     */
    boolean supports(int dataVersion, LinCompoundTag rootTag);

    /**
     * Create a Chunk from the given root tag.
     *
     * @param rootTag the root NBT tag (may be the full chunk or contain a Level tag)
     * @return the chunk
     * @throws DataException if the tag is not valid for this format
     */
    Chunk load(LinCompoundTag rootTag) throws DataException;
}
