package com.sk89q.worldedit.arranger;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;

import java.util.stream.Collectors;

/**
 * A report of applied actions, grouped by type.
 */
public final class ActionReport {

    /**
     * Get an empty report.
     *
     * @return an empty report
     */
    public static ActionReport empty() {
        // could be optimized, not doing so now
        return builder().build();
    }

    /**
     * Create a report consisting of a single action with the given type.
     *
     * @param type the type of action
     * @return the new report
     */
    public static ActionReport single(ActionType type) {
        return of(type, 1);
    }

    /**
     * Create a report consisting of a single action type with the given count.
     *
     * @param type the type of action
     * @return the new report
     */
    public static ActionReport of(ActionType type, long count) {
        return builder().addAction(type, count).build();
    }

    /**
     * Get a builder for creating an action report.
     *
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for an action report.
     */
    public static final class Builder {
        private final Object2LongMap<ActionType> report = new Object2LongLinkedOpenHashMap<>();

        /**
         * Add a single action of the given type.
         *
         * @param type the type of the action
         * @return this builder, for chaining
         */
        public Builder addAction(ActionType type) {
            return addAction(type, 1);
        }

        /**
         * Add some actions of the given type.
         *
         * @param type the type of the actions
         * @param count the number of actions
         * @return this builder, for chaining
         */
        public Builder addAction(ActionType type, long count) {
            report.put(type, report.getLong(type) + count);
            return this;
        }

        /**
         * Merge another report into this builder.
         *
         * @param other the other report
         * @return this builder, for chaining
         */
        public Builder merge(ActionReport other) {
            Object2LongMaps.fastForEach(other.report, e ->
                addAction(e.getKey(), e.getLongValue())
            );
            return this;
        }

        /**
         * Build an action report from this builder's content.
         *
         * @return the action report
         */
        public ActionReport build() {
            return new ActionReport(this);
        }

    }

    private final Object2LongMap<ActionType> report;

    private ActionReport(Builder builder) {
        this.report = new Object2LongLinkedOpenHashMap<>(builder.report);
    }

    /**
     * Get the number of actions of a given type.
     *
     * @param type the type of action
     * @return the number of actions that occurred
     */
    public long getActionCount(ActionType type) {
        return report.getLong(type);
    }

    /**
     * Get the total number of actions.
     *
     * @return the total number of actions that occurred
     */
    public long getTotalActionCount() {
        long count = 0;
        LongIterator entries = report.values().iterator();
        while (entries.hasNext()) {
            count += entries.nextLong();
        }
        return count;
    }

    /**
     * Convert this report to a report builder.
     *
     * @return the builder
     */
    public Builder toBuilder() {
        return builder().merge(this);
    }

    /**
     * Merge this report and the other report, adding their counts together.
     *
     * @param other the other report
     * @return a new report containing the merged content
     */
    public ActionReport merge(ActionReport other) {
        return toBuilder().merge(other).build();
    }

    /**
     * Describe this report.
     *
     * @return a component describing this report
     */
    public Component describe() {
        return TextComponent.join(
            TextComponent.newline(),
            report.object2LongEntrySet().stream()
                .map(e -> e.getKey().describeBulk(e.getLongValue()))
                .collect(Collectors.toList())
        );
    }

}
