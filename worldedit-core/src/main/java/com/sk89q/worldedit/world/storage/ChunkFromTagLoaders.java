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

import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.chunk.AnvilChunk;
import com.sk89q.worldedit.world.chunk.AnvilChunk13;
import com.sk89q.worldedit.world.chunk.AnvilChunk16;
import com.sk89q.worldedit.world.chunk.AnvilChunk18;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.chunk.OldChunk;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.List;

/**
 * Registry of chunk format loaders. Order matters: first supporting loader is used.
 */
public final class ChunkFromTagLoaders {

    private static final List<ChunkFromTagLoader> LOADERS = List.of(
        new AnvilChunk18Loader(),
        new AnvilChunk16Loader(),
        new AnvilChunk13Loader(),
        new AnvilChunkLoader(),
        new OldChunkLoader()
    );

    /**
     * Find the first loader that supports the given data version and tag, then load the chunk.
     *
     * @param dataVersion the chunk data version
     * @param rootTag the root NBT tag
     * @return the chunk
     * @throws DataException if no loader supports the tag or loading fails
     */
    public static Chunk loadChunk(int dataVersion, LinCompoundTag rootTag) throws DataException {
        for (ChunkFromTagLoader loader : LOADERS) {
            if (loader.supports(dataVersion, rootTag)) {
                return loader.load(rootTag);
            }
        }
        throw new ChunkStoreException("Unsupported chunk format: dataVersion=" + dataVersion);
    }

    private ChunkFromTagLoaders() {
    }

    private static final class AnvilChunk18Loader implements ChunkFromTagLoader {
        @Override
        public boolean supports(int dataVersion, LinCompoundTag rootTag) {
            return dataVersion >= Constants.DATA_VERSION_MC_1_18;
        }

        @Override
        public Chunk load(LinCompoundTag rootTag) throws DataException {
            return new AnvilChunk18(rootTag);
        }
    }

    private static final class AnvilChunk16Loader implements ChunkFromTagLoader {
        @Override
        public boolean supports(int dataVersion, LinCompoundTag rootTag) {
            return dataVersion >= Constants.DATA_VERSION_MC_1_16;
        }

        @Override
        public Chunk load(LinCompoundTag rootTag) throws DataException {
            LinCompoundTag levelTag = rootTag.findTag("Level", LinTagType.compoundTag());
            if (levelTag == null) {
                throw new ChunkStoreException("Missing root 'Level' tag");
            }
            return new AnvilChunk16(levelTag);
        }
    }

    private static final class AnvilChunk13Loader implements ChunkFromTagLoader {
        @Override
        public boolean supports(int dataVersion, LinCompoundTag rootTag) {
            return dataVersion >= Constants.DATA_VERSION_MC_1_13;
        }

        @Override
        public Chunk load(LinCompoundTag rootTag) throws DataException {
            LinCompoundTag levelTag = rootTag.findTag("Level", LinTagType.compoundTag());
            if (levelTag == null) {
                throw new ChunkStoreException("Missing root 'Level' tag");
            }
            return new AnvilChunk13(levelTag);
        }
    }

    private static final class AnvilChunkLoader implements ChunkFromTagLoader {
        @Override
        public boolean supports(int dataVersion, LinCompoundTag rootTag) {
            LinCompoundTag levelTag = rootTag.findTag("Level", LinTagType.compoundTag());
            return levelTag != null && levelTag.value().containsKey("Sections");
        }

        @Override
        public Chunk load(LinCompoundTag rootTag) throws DataException {
            LinCompoundTag levelTag = rootTag.findTag("Level", LinTagType.compoundTag());
            if (levelTag == null) {
                throw new ChunkStoreException("Missing root 'Level' tag");
            }
            return new AnvilChunk(levelTag);
        }
    }

    private static final class OldChunkLoader implements ChunkFromTagLoader {
        @Override
        public boolean supports(int dataVersion, LinCompoundTag rootTag) {
            return rootTag.findTag("Level", LinTagType.compoundTag()) != null;
        }

        @Override
        public Chunk load(LinCompoundTag rootTag) throws DataException {
            LinCompoundTag levelTag = rootTag.findTag("Level", LinTagType.compoundTag());
            if (levelTag == null) {
                throw new ChunkStoreException("Missing root 'Level' tag");
            }
            return new OldChunk(levelTag);
        }
    }
}
