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

import com.google.errorprone.annotations.InlineMe;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.chunk.Chunk;
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
            if (!(tag instanceof CompoundTag compoundTag)) {
                throw new ChunkStoreException("CompoundTag expected for chunk; got "
                    + tag.getClass().getName());
            }

            return compoundTag;
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
    @InlineMe(
        replacement = "ChunkStoreHelper.getChunk(rootTag.toLinTag())",
        imports = "com.sk89q.worldedit.world.storage.ChunkStoreHelper"
    )
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
        int dataVersion = extractDataVersion(rootTag);
        DataFixResult fixResult = applyDataFixerIfNeeded(rootTag, dataVersion);
        return ChunkFromTagLoaders.loadChunk(fixResult.effectiveDataVersion(), fixResult.rootTag());
    }

    /**
     * Extracts the DataVersion from the chunk root tag, or -1 if missing/invalid.
     */
    private static int extractDataVersion(LinCompoundTag rootTag) {
        return rootTag.value().get("DataVersion") instanceof LinNumberTag<?> numberTag
            ? numberTag.value().intValue() : -1;
    }

    private record DataFixResult(LinCompoundTag rootTag, int effectiveDataVersion) {}

    /**
     * Applies the platform data fixer when the chunk is in MCA format and behind current version.
     * Only fixes MCA format; DFU doesn't support MCR chunks.
     */
    private static DataFixResult applyDataFixerIfNeeded(LinCompoundTag rootTag, int dataVersion) {
        final Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
        final int currentDataVersion = platform.getDataVersion();
        boolean isMcAFormat = dataVersion > 0 || hasLevelSections(rootTag);
        boolean isBehindCurrentVersion = dataVersion < currentDataVersion;
        boolean shouldApplyFix = isMcAFormat && isBehindCurrentVersion;
        if (!shouldApplyFix) {
            return new DataFixResult(rootTag, dataVersion);
        }
        final DataFixer dataFixer = platform.getDataFixer();
        if (dataFixer == null) {
            return new DataFixResult(rootTag, dataVersion);
        }
        LinCompoundTag fixedTag = dataFixer.fixUp(DataFixer.FixTypes.CHUNK, rootTag, dataVersion);
        return new DataFixResult(fixedTag, currentDataVersion);
    }

    private static boolean hasLevelSections(LinCompoundTag rootTag) {
        LinCompoundTag levelTag = rootTag.findTag("Level", LinTagType.compoundTag());
        return levelTag != null && levelTag.value().containsKey("Sections");
    }

    private ChunkStoreHelper() {
    }
}
