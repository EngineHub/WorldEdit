package com.sk89q.worldedit.util.collection;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Low-memory {@link List} operations.
 *
 * <p>
 * Internal list API. Do not use.
 * </p>
 */
public class LowMemoryLists {

    // A guess on what might be optimal.
    private static final int LIST_COPY_SIZE = 1 << 20;

    /**
     * Copies from the given lists, clearing them to keep the copy overhead low as data is moved.
     *
     * @param lists the lists to copy from
     * @param <E> the type of elements in the lists
     * @return the copied data
     */
    @SafeVarargs
    public static <E> List<E> copyWithLowOverhead(ArrayList<E>... lists) {
        ImmutableList.Builder<E> builder = ImmutableList.builder();

        for (ArrayList<E> list : lists) {
            int size;
            while ((size = list.size()) > 0) {
                List<E> sub = list.subList(0, Math.min(size, LIST_COPY_SIZE));
                builder.addAll(sub);
                sub.clear();
                list.trimToSize();
            }
        }

        return builder.build();
    }

    private LowMemoryLists() {
    }
}
