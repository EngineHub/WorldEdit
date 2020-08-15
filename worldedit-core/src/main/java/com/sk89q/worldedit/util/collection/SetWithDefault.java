package com.sk89q.worldedit.util.collection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Represents the combination of a {@link Set} of values and a default value.
 *
 * <p>
 * If (and only if) the set is empty, there will be no default value. This also means that
 * no default value implies an empty set.
 * </p>
 */
public final class SetWithDefault<E> {

    private static final SetWithDefault<?> EMPTY = new SetWithDefault<>(ImmutableSet.of());

    public static <E> SetWithDefault<E> empty() {
        // there is no element to retrieve, so this is safe
        @SuppressWarnings("unchecked")
        SetWithDefault<E> empty = (SetWithDefault<E>) EMPTY;
        return empty;
    }

    public static <E> SetWithDefault<E> of(@Nullable E defaultValue) {
        return of(defaultValue, ImmutableSet.of());
    }

    public static <E> SetWithDefault<E> of(@Nullable E defaultValue, Iterable<? extends E> values) {
        if (defaultValue == null && Iterables.isEmpty(values)) {
            return empty();
        }
        Set<E> result = new LinkedHashSet<>();
        if (defaultValue != null) {
            result.add(defaultValue);
        }
        values.forEach(result::add);
        return new SetWithDefault<>(ImmutableSet.copyOf(result));
    }

    private final ImmutableSet<E> values;

    private SetWithDefault(ImmutableSet<E> values) {
        this.values = values;
    }

    /**
     * Get the default value, if present.
     *
     * @return the default value
     */
    @Nullable
    public E defaultValue() {
        return values.isEmpty() ? null : values.iterator().next();
    }

    /**
     * Get a mostly-unsorted set of the values.
     *
     * <p>
     * It is guaranteed that the {@linkplain #defaultValue() default value} is the first element
     * in iteration order, if present.
     * </p>
     *
     * @return the set of values
     */
    public ImmutableSet<E> values() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetWithDefault<?> that = (SetWithDefault<?>) o;
        return Objects.equals(values, that.values)
            && Objects.equals(defaultValue(), that.defaultValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, defaultValue());
    }

    @Override
    public String toString() {
        return "SetWithDefault{"
            + "defaultValue=" + defaultValue() + ","
            + "values=" + values
            + "}";
    }
}
