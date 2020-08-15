package com.sk89q.worldedit.util.collection;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nullable;

public final class MoreSets {

    /**
     * If {@code first} is non-{@code null}, ensure it comes first in a set including it and
     * {@code all}.
     *
     * @param first the first element
     * @param all the rest or all other elements (may include first)
     * @param <T> the type of the elements
     * @return a set whose iteration order returns {@code first} first, then other items in
     *      {@code all}
     */
    public static <T> Set<T> ensureFirst(@Nullable T first, Iterable<T> all) {
        Set<T> result = new LinkedHashSet<>();
        if (first != null) {
            result.add(first);
        }
        all.forEach(result::add);
        return result;
    }

    private MoreSets() {
    }
}
