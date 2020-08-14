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

package com.sk89q.worldedit.util.task.progress;

import java.util.Arrays;
import java.util.Collection;

/**
 * A progress object describes the progress of an operation, specifying
 * either a percentage of completion or a status of indeterminacy.
 *
 * <p>Progress objects are immutable.</p>
 *
 * <p>To create a new instance, use one of the static constructors
 * on this class.</p>
 */
public abstract class Progress {

    /**
     * Create a new instance.
     */
    private Progress() {
    }

    /**
     * Return whether the current progress is indeterminate.
     *
     * @return true if indeterminate
     */
    public abstract boolean isIndeterminate();

    /**
     * Get the progress percentage.
     *
     * <p>If {@link #isIndeterminate()} returns {@code true}, the behavior
     * of this method is undefined.</p>
     *
     * @return a number in the range [0, 1]
     */
    public abstract double getProgress();

    /**
     * Get a static progress object that is indeterminate.
     *
     * @return a progress object
     */
    public static Progress indeterminate() {
        return INDETERMINATE;
    }

    /**
     * Get a static progress object that is complete.
     *
     * @return a progress object
     */
    public static Progress completed() {
        return COMPLETED;
    }

    /**
     * Create a new progress object with the given percentage.
     *
     * @param value the percentage, which will be clamped to [0, 1]
     * @return a progress object
     */
    public static Progress of(double value) {
        if (value < 0) {
            value = 0;
        } else if (value > 1) {
            value = 1;
        }

        final double finalValue = value;
        return new Progress() {
            @Override
            public boolean isIndeterminate() {
                return false;
            }

            @Override
            public double getProgress() {
                return finalValue;
            }
        };
    }

    /**
     * Create a new progress object with progress split equally between the
     * given progress objects.
     *
     * @param objects an array of progress objects
     * @return a new progress value
     */
    public static Progress split(Progress... objects) {
        return split(Arrays.asList(objects));
    }

    /**
     * Create a new progress object with progress split equally between the
     * given progress objects.
     *
     * @param progress a collection of progress objects
     * @return a new progress value
     */
    public static Progress split(Collection<Progress> progress) {
        int count = 0;
        double total = 0;

        for (Progress p : progress) {
            if (p.isIndeterminate()) {
                return indeterminate();
            }
            total += p.getProgress();
        }

        return of(total / count);
    }

    /**
     * Create a new progress object with progress split equally between the
     * given {@link ProgressObservable}s.
     *
     * @param observables an array of observables
     * @return a new progress value
     */
    public static Progress splitObservables(ProgressObservable... observables) {
        return splitObservables(Arrays.asList(observables));
    }

    /**
     * Create a new progress object with progress split equally between the
     * given {@link ProgressObservable}s.
     *
     * @param observables a collection of observables
     * @return a new progress value
     */
    public static Progress splitObservables(Collection<? extends ProgressObservable> observables) {
        int count = 0;
        double total = 0;

        for (ProgressObservable observable : observables) {
            Progress p = observable.getProgress();
            if (p.isIndeterminate()) {
                return indeterminate();
            }
            total += p.getProgress();
        }

        return of(total / count);
    }

    private static final Progress COMPLETED = of(1);

    private static final Progress INDETERMINATE = new Progress() {
        @Override
        public boolean isIndeterminate() {
            return true;
        }

        @Override
        public double getProgress() {
            return 0;
        }
    };

}
