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

public class RegionOptimizedVectorSorter {

    // We need to sort by region file, chunk, and Y (x/z don't really matter)
    // Due to MC having x/z axes of only 60,000,000 blocks, the max value is <=26 bits.
    // We can store the chunk in 4 bits less, 22 bits; and region in 5 bits less than that, 17 bits
    // If we share the region + chunk bits, we can make a radix key that is really 5 parts:
    // [region X (17)][region Z (17)][chunk X (5)][chunk Z (5)][block Y (20)] = 64 bits
    // Even though we only normally need 8 bits for Y, we might as well use it for cubic chunks
    // compatibility in the future, since we have the room in the long value
    private static final long Y_MAX = mask(20);

    private static long key(BlockVector3 elem) {
        long cx = elem.getX() >>> 4;
        long cz = elem.getZ() >>> 4;
        long cy = Y_MAX - elem.getY();
        long rx = cx >>> 5;
        long rz = cz >>> 5;
        return (rx << (17 + 5 + 5 + 20))
            | ((rz << (5 + 5 + 20)))
            | ((cx << (5 + 20)))
            | ((cz << 20))
            | cy;
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
}
