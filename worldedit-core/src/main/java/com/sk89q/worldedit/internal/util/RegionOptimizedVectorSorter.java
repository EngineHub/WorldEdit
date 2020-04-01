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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.sk89q.worldedit.math.BitMath.mask;

/**
 * Uses a radix sort to order vectors by region, then chunk, then Y value (max -> min).
 */
public class RegionOptimizedVectorSorter {

    // We need to sort by region file, chunk, and Y (x/z don't really matter)
    // Due to MC having x/z axes of only 60,000,000 blocks, the max value is <=26 bits.
    // We can store the chunk in 4 bits less, 22 bits; and region in 5 bits less than that, 17 bits
    // If we share the region + chunk bits, we can make a radix key that is really 5 parts:
    // [region X (17)][region Z (17)][chunk X (5)][chunk Z (5)][block Y (20)] = 64 bits
    // Even though we only normally need 8 bits for Y, we might as well use it for cubic chunks
    // compatibility in the future, since we have the room in the long value
    private static final int CHUNK_Z_SHIFT = 20;
    private static final int CHUNK_X_SHIFT = 5 + CHUNK_Z_SHIFT;
    private static final int REGION_Z_SHIFT = 5 + CHUNK_X_SHIFT;
    private static final int REGION_X_SHIFT = 17 + REGION_Z_SHIFT;
    private static final long REGION_X_MASK = ((long) mask(17)) << REGION_X_SHIFT;
    private static final long REGION_Z_MASK = ((long) mask(17)) << REGION_Z_SHIFT;
    private static final long CHUNK_X_MASK = ((long) mask(5)) << CHUNK_X_SHIFT;
    private static final long CHUNK_Z_MASK = ((long) mask(5)) << CHUNK_Z_SHIFT;
    private static final int Y_MAX = mask(20);
    // We flip the region x/z sign to turn signed numbers into unsigned ones
    // this allows us to sort on the raw bits, and not care about signs
    // Essentially it transforms [negative values][positive values]
    // to [positive value][even more positive values], i.e. a shift upwards
    private static final long FLIP_REGION_X_SIGN = 0x1_00_00L << REGION_X_SHIFT;
    private static final long FLIP_REGION_Z_SIGN = 0x1_00_00L << REGION_Z_SHIFT;

    private static long key(BlockVector3 elem) {
        long x = elem.getX();
        long z = elem.getZ();
        return (((x << (REGION_X_SHIFT - 9)) & REGION_X_MASK) ^ FLIP_REGION_X_SIGN)
            | (((z << (REGION_Z_SHIFT - 9)) & REGION_Z_MASK) ^ FLIP_REGION_Z_SIGN)
            | ((x << (CHUNK_X_SHIFT - 4)) & CHUNK_X_MASK)
            | ((z << (CHUNK_Z_SHIFT - 4)) & CHUNK_Z_MASK)
            | (Y_MAX - elem.getY());
    }

    private static final int NUMBER_OF_BITS = 64;
    private static final int BITS_PER_SORT = 16;
    private static final int MAX_FOR_BPS = 1 << BITS_PER_SORT;
    private static final int MASK_FOR_BPS = (1 << BITS_PER_SORT) - 1;
    private static final int NUMBER_OF_SORTS = NUMBER_OF_BITS / BITS_PER_SORT;
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    static int PARALLELISM_THRESHOLD;

    static {
        if (NUMBER_OF_CORES == 1) {
            // don't even bother
            PARALLELISM_THRESHOLD = Integer.MAX_VALUE;
        } else {
            // Determined via benchmarking serial vs. parallel.
            // Didn't try anything more fine-grained that increments of 100,000.
            PARALLELISM_THRESHOLD = 200000;
        }
    }

    private static final ExecutorService SORT_SVC = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("worldedit-sort-svc-%d")
            .build()
    );

    public static void sort(List<BlockVector3> vectors) {
        sort(vectors.size() >= PARALLELISM_THRESHOLD, vectors);
    }

    /**
     * For test purposes, or if you want to finely control when parallelism occurs.
     *
     * <p>
     * {@code vectors} must be mutable, and will be sorted after this method returns.
     * </p>
     *
     * @param parallel {@code true} to sort in parallel
     * @param vectors the vectors to sort
     */
    public static void sort(boolean parallel, List<BlockVector3> vectors) {
        // Currently we don't do an in-place radix sort, but we could in the future.
        int size = vectors.size();
        // take care of some easy cases
        if (size == 0 || size == 1) {
            return;
        }
        BlockVector3[] source = vectors.toArray(new BlockVector3[0]);
        BlockVector3[] sorted = new BlockVector3[size];
        source = !parallel
            ? serialSort(source, size, sorted)
            : parallelSort(source, size, sorted);
        ListIterator<BlockVector3> it = vectors.listIterator();
        for (BlockVector3 blockVector3 : source) {
            it.next();
            it.set(blockVector3);
        }
    }

    private static BlockVector3[] parallelSort(BlockVector3[] source, int size, BlockVector3[] sorted) {
        int[][] counts = new int[NUMBER_OF_CORES][MAX_FOR_BPS];
        int[] finalCounts = new int[MAX_FOR_BPS];
        int[] keys = new int[size];
        List<Future<int[]>> tasks = new ArrayList<>(NUMBER_OF_CORES);
        int kStep = (size + NUMBER_OF_CORES - 1) / NUMBER_OF_CORES;
        for (int p = 0; p < NUMBER_OF_SORTS; p++) {
            BlockVector3[] currentSource = source;
            int shift = BITS_PER_SORT * p;
            for (int c = 0; c < NUMBER_OF_CORES; c++) {
                int[] localCounts = counts[c];
                int kStart = kStep * c;
                int kEnd = Math.min(kStart + kStep, size);
                tasks.add(SORT_SVC.submit(() -> {
                    for (int i = kStart; i < kEnd; i++) {
                        int k = ((int) (key(currentSource[i]) >>> shift) & MASK_FOR_BPS);
                        keys[i] = k;
                        localCounts[k]++;
                    }
                    return localCounts;
                }));
            }
            for (Future<int[]> task : tasks) {
                try {
                    task.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int c = 0; c < NUMBER_OF_CORES; c++) {
                int[] localCounts = counts[c];
                for (int i = 0; i < MAX_FOR_BPS; i++) {
                    finalCounts[i] += localCounts[i];
                    localCounts[i] = 0;
                }
            }
            tasks.clear();
            copyByCounts(size, source, sorted, keys, finalCounts);
            BlockVector3[] temp = source;
            source = sorted;
            sorted = temp;
        }
        // after the loop returns, source is now the final sorted array!
        return source;
    }

    private static BlockVector3[] serialSort(BlockVector3[] source, int size, BlockVector3[] sorted) {
        int[] counts = new int[MAX_FOR_BPS];
        int[] keys = new int[size];
        for (int p = 0; p < NUMBER_OF_SORTS; p++) {
            for (int i = 0; i < size; i++) {
                int k = ((int) (key(source[i]) >>> (BITS_PER_SORT * p)) & MASK_FOR_BPS);
                keys[i] = k;
                counts[k]++;
            }
            copyByCounts(size, source, sorted, keys, counts);
            BlockVector3[] temp = source;
            source = sorted;
            sorted = temp;
        }
        // after the loop returns, source is now the final sorted array!
        return source;
    }

    private static void copyByCounts(int size, BlockVector3[] source, BlockVector3[] sorted, int[] keys, int[] finalCounts) {
        int lastCount = finalCounts[0];
        for (int i = 1; i < MAX_FOR_BPS; i++) {
            lastCount = (finalCounts[i] += lastCount);
        }
        for (int i = size - 1; i >= 0; i--) {
            int key = keys[i];
            int count = --finalCounts[key];
            sorted[count] = source[i];
        }
        Arrays.fill(finalCounts, 0);
    }

    private RegionOptimizedVectorSorter() {
    }
}
