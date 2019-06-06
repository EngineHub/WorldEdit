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

package com.sk89q.worldedit.internal.util;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.storage.McRegionChunkStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
public class ChunkDeleter {
    public static final Logger logger = LoggerFactory.getLogger(ChunkDeleter.class);

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Chunk list must be specified.");
            System.exit(1);
            return;
        }
        boolean doBackup = args.length > 1 && args[1].equalsIgnoreCase("true") || promptDoBackup();
        String chunkFile = args[0];

        ChunkDeleter cd = new ChunkDeleter(doBackup);
        if (!cd.loadChunkFile(chunkFile)) {
            System.exit(1);
        }
        cd.readChunksFromFile();

        System.out.print("Press [enter] to begin deletion...");
        //noinspection ResultOfMethodCallIgnored
        System.in.read();

        cd.runDeleter();

        Files.deleteIfExists(Paths.get(chunkFile));
    }

    public static void runFromFile(File delChunks, boolean backup) {
        final ChunkDeleter chunkDeleter = new ChunkDeleter(backup);
        if (chunkDeleter.loadChunkFile(delChunks.getPath())) {
            try {
                if (chunkDeleter.runDeleter()) {
                    logger.info("Successfully deleted chunks.");
                    if (!delChunks.delete()) {
                        delChunks.deleteOnExit();
                    }
                } else {
                    logger.error("Error occurred while deleting chunks. " +
                            "If world errors occur, stop the server and restore the *.bak backup files.");
                }
            } catch (IOException e) {
                logger.warn("Exception occurred while backup up region files. Skipping chunk deletion for safety.", e);
            }
        } else {
            logger.warn("Couldn't load chunk deletion list. Skipping chunk deletion this time.");
        }
    }

    private static boolean promptDoBackup() {
        Scanner scanner = new Scanner(System.in);
        Boolean backup = null;
        System.out.print("Would you like to create backups of the region files before deleting chunks? [Y/n] ");
        while (backup == null) {
            String input = scanner.next();
            if (input.isEmpty() || input.charAt(0) == 'Y' || input.charAt(0) == 'y') {
                backup = true;
            } else if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
                backup = false;
            } else {
                System.out.print("Please enter 'y' or 'n': ");
            }
        }
        return backup;
    }

    public ChunkDeleter(boolean doBackup) {
        this.backup = doBackup;
    }

    private boolean backup;
    private List<String> chunks;
    private Map<String, Map<String, Set<BlockVector2>>> regionMap;

    public boolean loadChunkFile(String chunkFile) {
        try {
            chunks = Files.readAllLines(Paths.get(chunkFile));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        readChunksFromFile();
        return true;
    }

    public boolean runDeleter() throws IOException {
        if (backup) {
            backupRegions(regionMap);
        }
        return deleteChunks(regionMap);
    }

    private void readChunksFromFile() {
        regionMap = new HashMap<>();
        Set<String> goodWorlds = new HashSet<>();
        Set<String> badWorlds = new HashSet<>();
        int total = 0;
        for (String line : chunks) {
            String[] split = line.split(",");
            if (split.length != 3) {
                System.err.println("Invalid line, ignoring: " + line);
                continue;
            }
            final String worldName = split[0];
            if (badWorlds.contains(worldName)) {
                continue;
            }
            if (!goodWorlds.contains(worldName)) {
                if (Files.isDirectory(Paths.get(worldName)) && Files.isDirectory(Paths.get(worldName, "region"))) {
                    goodWorlds.add(worldName);
                } else {
                    badWorlds.add(worldName);
                    System.err.println(String.format(
                            "World/region folder not found for world '%s', skipping chunks for this world.", worldName));
                }
            }

            int x, z;
            try {
                x = Integer.parseInt(split[1]);
                z = Integer.parseInt(split[2]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid line, ignoring: " + line);
                continue;
            }

            final BlockVector2 chunkPos = BlockVector2.at(x, z);
            if (regionMap.computeIfAbsent(worldName, k -> new HashMap<>())
                    .computeIfAbsent(McRegionChunkStore.getFilename(chunkPos), k -> new HashSet<>())
                    .add(chunkPos)) {
                total++;
            }
        }

        System.out.println(String.format("Processed file with %d chunks. Summary of chunks to delete:", total));
        regionMap.forEach((world, regions) -> System.out.println(String.format("%s: %d chunk(s) across %d region(s)",
                world, regions.values().stream().mapToInt(Set::size).sum(), regions.size())));
    }

    private static void backupRegions(Map<String, Map<String, Set<BlockVector2>>> regionMap) throws IOException {
        for (Map.Entry<String, Map<String, Set<BlockVector2>>> entry : regionMap.entrySet()) {
            String world = entry.getKey();
            Map<String, Set<BlockVector2>> regions = entry.getValue();
            backupRegions(world, regions.keySet());
        }
    }

    private static void backupRegions(String world, Set<String> regions) throws IOException {
        Path worldDir = Paths.get(world, "region");
        for (String region : regions) {
            Path mca = worldDir.resolve(region);
            if (mca.toFile().exists()) {
                Files.copy(mca, mca.resolveSibling(region + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static boolean deleteChunks(Map<String, Map<String, Set<BlockVector2>>> regionMap) {
        try {
            regionMap.forEach(ChunkDeleter::deleteChunks);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void deleteChunks(String world, Map<String, Set<BlockVector2>> regions) {
        Path worldDir = Paths.get(world, "region");
        for (Map.Entry<String, Set<BlockVector2>> entry : regions.entrySet()) {
            String region = entry.getKey();
            Path mca = worldDir.resolve(region);
            if (mca.toFile().exists()) {
                try {
                    deleteChunks(mca, entry.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println(String.format("Region file %s doesn't exist. Skipping.", mca));
            }
        }
    }

    private static void deleteChunks(Path mca, Set<BlockVector2> chunks) throws IOException {
        File regionFile = mca.toFile();

        try (McRegionWriter region = new McRegionWriter(regionFile)) {
            for (BlockVector2 chunk : chunks.stream().sorted(
                    Comparator.comparing(BlockVector2::getBlockX).thenComparing(BlockVector2::getBlockZ))
                    .collect(Collectors.toList())) {
                region.deleteChunk(chunk);
            }
        }
    }

    private static class McRegionWriter implements AutoCloseable {

        private RandomAccessFile raf;
        McRegionWriter(File file) throws IOException {
            raf = new RandomAccessFile(file, "rw");
        }

        void deleteChunk(BlockVector2 pos) throws IOException {
            int x = pos.getBlockX() & 31;
            int z = pos.getBlockZ() & 31;
            raf.seek((x + z * 32) * 4);
            raf.writeInt(0);
        }

        @Override
        public void close() throws IOException {
            raf.close();
        }
    }
}
