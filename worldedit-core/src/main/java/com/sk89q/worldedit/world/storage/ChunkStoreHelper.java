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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.chunk.AnvilChunk;
import com.sk89q.worldedit.world.chunk.AnvilChunk13;
import com.sk89q.worldedit.world.chunk.AnvilChunk16;
import com.sk89q.worldedit.world.chunk.AnvilChunk18;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.chunk.OldChunk;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinTagType;

import java.io.IOException;
import java.io.InputStream;

public class ChunkStoreHelper {

    @FunctionalInterface
    public interface ChunkDataInputSupplier {

        InputStream openInputStream() throws DataException, IOException;

    }

    /**
     * Reads a chunk from the given input stream.
     *
     * @param input the input stream
     * @return the chunk
     * @throws DataException if an error occurs
     * @throws IOException if an I/O error occurs
     * @deprecated No replacement, just load the tag yourself
     */
    @Deprecated
    public static CompoundTag readCompoundTag(ChunkDataInputSupplier input) throws DataException, IOException {
        try (InputStream stream = input.openInputStream();
            NBTInputStream nbt = new NBTInputStream(stream)) {
            Tag<?, ?> tag = nbt.readNamedTag().getTag();
            if (!(tag instanceof CompoundTag)) {
                throw new ChunkStoreException("CompoundTag expected for chunk; got "
                    + tag.getClass().getName());
            }

            return (CompoundTag) tag;
        }
    }

    /**
     * Convert a chunk NBT tag into a {@link Chunk} implementation.
     *
     * @param rootTag the root tag of the chunk
     * @return a Chunk implementation
     * @throws DataException if the rootTag is not valid chunk data
     * @deprecated Use {@link #getChunk(LinCompoundTag)}
     */
    @Deprecated
    public static Chunk getChunk(CompoundTag rootTag) throws DataException {
        return getChunk(rootTag.toLinTag());
    }

    /**
     * Convert a chunk NBT tag into a {@link Chunk} implementation.
     *
     * @param rootTag the root tag of the chunk
     * @return a Chunk implementation
     * @throws DataException if the rootTag is not valid chunk data
     */
    public static Chunk getChunk(LinCompoundTag rootTag) throws DataException {
        int dataVersion = rootTag.value().get("DataVersion") instanceof LinNumberTag<?> t
            ? t.value().intValue() : -1;

        final Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
        final int currentDataVersion = platform.getDataVersion();
        if ((dataVersion > 0 || hasLevelSections(rootTag)) && dataVersion < currentDataVersion) { // only fix up MCA format, DFU doesn't support MCR chunks
            final DataFixer dataFixer = platform.getDataFixer();
            if (dataFixer != null) {
                rootTag = dataFixer.fixUp(DataFixer.FixTypes.CHUNK, rootTag, dataVersion);
                dataVersion = currentDataVersion;
            }
        }

        if (dataVersion >= Constants.DATA_VERSION_MC_1_18) {
            return new AnvilChunk18(rootTag);
        }

        LinCompoundTag tag = rootTag.findTag("Level", LinTagType.compoundTag());
        if (tag == null) {
            throw new ChunkStoreException("Missing root 'Level' tag");
        }

        if (dataVersion >= Constants.DATA_VERSION_MC_1_16) {
            return new AnvilChunk16(tag);
        }
        if (dataVersion >= Constants.DATA_VERSION_MC_1_13) {
            return new AnvilChunk13(tag);
        }

        if (tag.value().containsKey("Sections")) {
            return new AnvilChunk(tag);
        }

        return new OldChunk(tag);
    }

    private static boolean hasLevelSections(LinCompoundTag rootTag) {
        LinCompoundTag levelTag = rootTag.findTag("Level", LinTagType.compoundTag());
        return levelTag != null && levelTag.value().containsKey("Sections");
    }

    private ChunkStoreHelper() {
    }
}
