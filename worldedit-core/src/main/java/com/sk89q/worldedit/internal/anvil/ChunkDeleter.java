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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ChunkDeleter {

    public static final String DELCHUNKS_FILE_NAME = "delete_chunks.json";
    private static final Logger logger = LoggerFactory.getLogger(ChunkDeleter.class);

    private static final Comparator<BlockVector2> chunkSorter = Comparator.comparing(
        pos -> (pos.getBlockX() & 31) + (pos.getBlockZ() & 31) * 32
    );

    private static final Gson chunkDeleterGson = new GsonBuilder()
            .registerTypeAdapter(BlockVector2.class, new BlockVector2Adapter())
            .setPrettyPrinting()
            .create();

    public static ChunkDeletionInfo readInfo(Path chunkFile) throws IOException, JsonSyntaxException {
        String json = new String(Files.readAllBytes(chunkFile), StandardCharsets.UTF_8);
        return chunkDeleterGson.fromJson(json, ChunkDeletionInfo.class);
    }

    public static void writeInfo(ChunkDeletionInfo info, Path chunkFile) throws IOException, JsonIOException {
        String json = chunkDeleterGson.toJson(info, new TypeToken<ChunkDeletionInfo>() {}.getType());
        try (BufferedWriter writer = Files.newBufferedWriter(chunkFile)) {
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
        logger.info("Found chunk deletions. Proceeding with deletion...");
        long start = System.currentTimeMillis();
        if (chunkDeleter.runDeleter()) {
            logger.info("Successfully deleted {} matching chunks (out of {}, taking {} ms).",
                    chunkDeleter.getDeletedChunkCount(), chunkDeleter.getDeletionsRequested(),
                    System.currentTimeMillis() - start);
            if (deleteOnSuccess) {
                boolean deletedFile = false;
                try {
                    deletedFile = Files.deleteIfExists(chunkFile);
                } catch (IOException ignored) {
                }
                if (!deletedFile) {
                    logger.warn("Chunk deletion file could not be cleaned up. This may have unintended consequences"
                        + " on next startup, or if /delchunks is used again.");
                }
            }
        } else {
            logger.error("Error occurred while deleting chunks. "
                + "If world errors occur, stop the server and restore the *.bak backup files.");
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
    private final Set<Path> backedUpRegions = new HashSet<>();
    private boolean shouldPreload;
    private int debugRate = 100;
    private int totalChunksDeleted = 0;
    private int deletionsRequested = 0;

    private boolean runDeleter() {
        return chunkDeletionInfo.batches.stream().allMatch(this::runBatch);
    }

    private boolean runBatch(ChunkDeletionInfo.ChunkBatch chunkBatch) {
        int chunkCount = chunkBatch.getChunkCount();
        logger.debug("Processing deletion batch with {} chunks.", chunkCount);
        final Map<Path, Stream<BlockVector2>> regionToChunkList = groupChunks(chunkBatch);
        BiPredicate<RegionAccess, BlockVector2> predicate = createPredicates(chunkBatch.deletionPredicates);
        shouldPreload = chunkBatch.chunks == null;
        deletionsRequested += chunkCount;
        debugRate = chunkCount / 10;

        return regionToChunkList.entrySet().stream().allMatch(entry -> {
            Path regionPath = entry.getKey();
            if (!Files.exists(regionPath)) {
                return true;
            }
            if (chunkBatch.backup && !backedUpRegions.contains(regionPath)) {
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

    private Map<Path, Stream<BlockVector2>> groupChunks(ChunkDeletionInfo.ChunkBatch chunkBatch) {
        Path worldPath = Paths.get(chunkBatch.worldPath);
        if (chunkBatch.chunks != null) {
            return chunkBatch.chunks.stream()
                .collect(Collectors.groupingBy(RegionFilePos::new))
                .entrySet().stream().collect(Collectors.toMap(
                    e -> worldPath.resolve("region").resolve(e.getKey().getFileName()),
                    e -> e.getValue().stream().sorted(chunkSorter)));
        } else {
            final BlockVector2 minChunk = chunkBatch.minChunk;
            final BlockVector2 maxChunk = chunkBatch.maxChunk;
            final RegionFilePos minRegion = new RegionFilePos(minChunk);
            final RegionFilePos maxRegion = new RegionFilePos(maxChunk);
            Map<Path, Stream<BlockVector2>> groupedChunks = new HashMap<>();
            for (int regX = minRegion.getX(); regX <= maxRegion.getX(); regX++) {
                for (int regZ = minRegion.getZ(); regZ <= maxRegion.getZ(); regZ++) {
                    final Path regionPath = worldPath.resolve("region").resolve(new RegionFilePos(regX, regZ).getFileName());
                    if (!Files.exists(regionPath)) {
                        continue;
                    }
                    int startX = regX << 5;
                    int endX = (regX << 5) + 31;
                    int startZ = regZ << 5;
                    int endZ = (regZ << 5) + 31;

                    int minX = Math.max(Math.min(startX, endX), minChunk.getBlockX());
                    int minZ = Math.max(Math.min(startZ, endZ), minChunk.getBlockZ());
                    int maxX = Math.min(Math.max(startX, endX), maxChunk.getBlockX());
                    int maxZ = Math.min(Math.max(startZ, endZ), maxChunk.getBlockZ());
                    Stream<BlockVector2> stream = Stream.iterate(BlockVector2.at(minX, minZ),
                        bv2 -> {
                            int nextX = bv2.getBlockX();
                            int nextZ = bv2.getBlockZ();
                            if (++nextX > maxX) {
                                nextX = minX;
                                if (++nextZ > maxZ) {
                                    return null;
                                }
                            }
                            return BlockVector2.at(nextX, nextZ);
                        });
                    groupedChunks.put(regionPath, stream);
                }
            }
            return groupedChunks;
        }
    }

    private BiPredicate<RegionAccess, BlockVector2> createPredicates(List<ChunkDeletionInfo.DeletionPredicate> deletionPredicates) {
        if (deletionPredicates == null) {
            return (r, p) -> true;
        }
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
        backedUpRegions.add(backupFile);
    }

    private boolean deleteChunks(Path regionFile, Stream<BlockVector2> chunks,
                                 BiPredicate<RegionAccess, BlockVector2> deletionPredicate) {
        try (RegionAccess region = new RegionAccess(regionFile, shouldPreload)) {
            for (Iterator<BlockVector2> iterator = chunks.iterator(); iterator.hasNext();) {
                BlockVector2 chunk = iterator.next();
                if (chunk == null) {
                    break;
                }
                if (deletionPredicate.test(region, chunk)) {
                    region.deleteChunk(chunk);
                    totalChunksDeleted++;
                    if (debugRate != 0 && totalChunksDeleted % debugRate == 0) {
                        logger.debug("Deleted {} chunks so far.", totalChunksDeleted);
                    }
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

    public int getDeletedChunkCount() {
        return totalChunksDeleted;
    }

    public int getDeletionsRequested() {
        return deletionsRequested;
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

    private static class RegionFilePos {
        private final int x;
        private final int z;

        RegionFilePos(BlockVector2 chunk) {
            this.x = chunk.getBlockX() >> 5;
            this.z = chunk.getBlockZ() >> 5;
        }

        RegionFilePos(int regX, int regZ) {
            this.x = regX;
            this.z = regZ;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public String getFileName() {
            return "r." + x + "." + z + ".mca";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RegionFilePos that = (RegionFilePos) o;

            if (x != that.x) {
                return false;
            }
            return z == that.z;

        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + z;
            return result;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + z + ")";
        }
    }
}
