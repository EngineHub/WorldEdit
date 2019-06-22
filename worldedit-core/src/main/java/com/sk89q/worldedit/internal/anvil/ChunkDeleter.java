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

package com.sk89q.worldedit.internal.anvil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.storage.McRegionChunkStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public final class ChunkDeleter {

    public static final String DELCHUNKS_FILE_NAME = "delete_chunks.json";
    private static final Logger logger = LoggerFactory.getLogger(ChunkDeleter.class);

    private static final Comparator<BlockVector2> chunkSorter = Comparator.comparing(
            pos -> (pos.getBlockX() & 31) + (pos.getBlockZ() & 31) * 32);

    private static Gson chunkDeleterGson = new GsonBuilder()
            .registerTypeAdapter(BlockVector2.class, new BlockVector2Adapter())
            .setPrettyPrinting()
            .create();

    public static ChunkDeletionInfo readInfo(Path chunkFile) throws IOException, JsonSyntaxException {
        String json = new String(Files.readAllBytes(chunkFile), StandardCharsets.UTF_8);
        return chunkDeleterGson.fromJson(json, ChunkDeletionInfo.class);
    }

    public static void writeInfo(ChunkDeletionInfo info, Path chunkFile) throws IOException, JsonIOException {
        String json = chunkDeleterGson.toJson(info, new TypeToken<ChunkDeletionInfo>() {}.getType());
        try (BufferedWriter writer = Files.newBufferedWriter(chunkFile, StandardOpenOption.CREATE)) {
            writer.write(json);
        }
    }

    public static void runFromFile(Path chunkFile, boolean deleteOnSuccess) {
        ChunkDeleter chunkDeleter;
        try {
            chunkDeleter = createFromFile(chunkFile);
        } catch (JsonSyntaxException | IOException e) {
            logger.error("Could not parse chunk deletion file. Invalid file?", e);
            return;
        }
        if (chunkDeleter.runDeleter()) {
            logger.info("Successfully deleted chunks.");
            if (deleteOnSuccess) {
                boolean deletedFile = false;
                try {
                    deletedFile = Files.deleteIfExists(chunkFile);
                } catch (IOException ignored) {
                }
                if (!deletedFile) {
                    logger.warn("Chunk deletion file could not be cleaned up. This may have unintended consequences" +
                            " on next startup, or if /delchunks is used again.");
                }
            }
        } else {
            logger.error("Error occurred while deleting chunks. " +
                    "If world errors occur, stop the server and restore the *.bak backup files.");
        }
    }

    private ChunkDeleter(ChunkDeletionInfo chunkDeletionInfo) {
        this.chunkDeletionInfo = chunkDeletionInfo;
    }

    private static ChunkDeleter createFromFile(Path chunkFile) throws IOException {
        ChunkDeletionInfo info = readInfo(chunkFile);
        if (info == null) {
            throw new IOException("Read null json. Empty file?");
        }
        return new ChunkDeleter(info);
    }

    private final ChunkDeletionInfo chunkDeletionInfo;

    private boolean runDeleter() {
        return chunkDeletionInfo.batches.stream().allMatch(this::runBatch);
    }

    private boolean runBatch(ChunkDeletionInfo.ChunkBatch chunkBatch) {
        final Map<Path, List<BlockVector2>> regionToChunkList = groupChunks(chunkBatch);
        BiPredicate<RegionAccess, BlockVector2> predicate = createPredicates(chunkBatch.deletionPredicates);
        return regionToChunkList.entrySet().stream().allMatch(entry -> {
            Path regionPath = entry.getKey();
            if (chunkBatch.backup) {
                try {
                    backupRegion(regionPath);
                } catch (IOException e) {
                    logger.warn("Error backing up region file: " + regionPath + ". Aborting the process.", e);
                    return false;
                }
            }
            return deleteChunks(regionPath, entry.getValue(), predicate);
        });
    }

    private Map<Path, List<BlockVector2>> groupChunks(ChunkDeletionInfo.ChunkBatch chunkBatch) {
        Path worldPath = Paths.get(chunkBatch.worldPath);
        return chunkBatch.chunks.stream()
                .collect(Collectors.groupingBy(chunk-> worldPath.resolve("region").resolve(McRegionChunkStore.getFilename(chunk))));
    }

    private BiPredicate<RegionAccess, BlockVector2> createPredicates(List<ChunkDeletionInfo.DeletionPredicate> deletionPredicates) {
        if (deletionPredicates == null) return (r, p) -> true;
        return deletionPredicates.stream()
                .map(this::createPredicate)
                .reduce(BiPredicate::and)
                .orElse((r, p) -> true);
    }

    private BiPredicate<RegionAccess, BlockVector2> createPredicate(ChunkDeletionInfo.DeletionPredicate deletionPredicate) {
        if ("modification".equals(deletionPredicate.property)) {
            int time;
            try {
                time = Integer.parseInt(deletionPredicate.value);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Modification time predicate specified invalid time: " + deletionPredicate.value);
            }
            switch (deletionPredicate.comparison) {
                case "<":
                    return (r, p) -> {
                        try {
                            return r.getModificationTime(p) < time;
                        } catch (IOException e) {
                            return false;
                        }
                    };
                case ">":
                    return (r, p) -> {
                        try {
                            return r.getModificationTime(p) > time;
                        } catch (IOException e) {
                            return false;
                        }
                    };
                default:
                    throw new IllegalStateException("Unexpected comparison value: " + deletionPredicate.comparison);
            }
        }
        throw new IllegalStateException("Unexpected property value: " + deletionPredicate.property);
    }

    private void backupRegion(Path regionFile) throws IOException {
        Path backupFile = regionFile.resolveSibling(regionFile.getFileName() + ".bak");
        Files.copy(regionFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean deleteChunks(Path regionFile, List<BlockVector2> chunks,
                                 BiPredicate<RegionAccess, BlockVector2> deletionPredicate) {
        try (RegionAccess region = new RegionAccess(regionFile)) {
            for (BlockVector2 chunk : chunks.stream().sorted(chunkSorter).collect(Collectors.toList())) {
                if (deletionPredicate.test(region, chunk)) {
                    region.deleteChunk(chunk);
                } else {
                    logger.debug("Chunk did not match predicates: " + chunk);
                }
            }
            return true;
        } catch (IOException e) {
            logger.warn("Error deleting chunks from region: " + regionFile + ". Aborting the process.", e);
            return false;
        }
    }

    private static class BlockVector2Adapter extends TypeAdapter<BlockVector2> {
        @Override
        public void write(JsonWriter out, BlockVector2 value) throws IOException {
            out.beginArray();
            out.value(value.getBlockX());
            out.value(value.getBlockZ());
            out.endArray();
        }

        @Override
        public BlockVector2 read(JsonReader in) throws IOException {
            in.beginArray();
            int x = in.nextInt();
            int z = in.nextInt();
            in.endArray();
            return BlockVector2.at(x, z);
        }
    }

}
