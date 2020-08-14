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

package com.sk89q.worldedit.util;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Returns the best choice given a weighting function and a target weight.
 *
 * <p>A function must be supplied that returns a numeric score for each
 * choice. The function can return null to mean that the choice should
 * not be considered.</p>
 *
 * @param <T> the type of choice
 */
public class WeightedChoice<T> {

    private final Function<T, ? extends Number> function;
    private final double target;
    private double best;
    private T current;

    /**
     * Create a new instance.
     *
     * @param function a function that assigns a score for each choice
     * @param target the target score that the best choice should be closest to
     */
    public WeightedChoice(Function<T, ? extends Number> function, double target) {
        checkNotNull(function);
        this.function = function;
        this.target = target;
    }

    /**
     * Consider the given object.
     *
     * @param object the choice
     */
    public void consider(T object) {
        checkNotNull(object);
        Number value = checkNotNull(function.apply(object));
        double distance = Math.abs(target - value.doubleValue());
        if (current == null || distance <= best) {
            best = distance;
            current = object;
        }
    }

    /**
     * Get the best choice.
     *
     * @return the best choice
     */
    public Optional<Choice<T>> getChoice() {
        if (current != null) {
            return Optional.of(new Choice<>(current, best));
        } else {
            return Optional.empty();
        }
    }

    /**
     * A tuple of choice and score.
     *
     * @param <T> the choice type
     */
    public static class Choice<T> {
        private final T object;
        private final double value;

        private Choice(T object, double value) {
            this.object = object;
            this.value = value;
        }

        /**
         * Get the chosen value.
         *
         * @return the value
         */
        public T getValue() {
            return object;
        }

        /**
         * Get the score.
         *
         * @return the score
         */
        public double getScore() {
            return value;
        }
    }

}
