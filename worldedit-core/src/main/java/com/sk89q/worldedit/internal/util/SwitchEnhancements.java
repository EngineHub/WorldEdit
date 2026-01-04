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

package com.sk89q.worldedit.internal.util;


/**
 * Utility methods relating to the {@code switch} statement.
 */
public final class SwitchEnhancements {
    public static final class ExhaustiveDummyValue {
        private static final ExhaustiveDummyValue INSTANCE = new ExhaustiveDummyValue();

        private ExhaustiveDummyValue() {
        }
    }

    /**
     * A no-op method to use as a dummy value for exhaustiveness checking in switch statements.
     *
     * @return an exhaustive dummy value
     */
    public static ExhaustiveDummyValue dummyValue() {
        return ExhaustiveDummyValue.INSTANCE;
    }

    /**
     * A no-op method to force exhaustiveness checking in switch statements by converting them to expressions.
     *
     * <p>
     * To use it, import it statically and wrap your switch with it, then yield {@link #dummyValue()} from all
     * branches.
     * </p>
     *
     * @param ignored an ignored parameter to allow passing the switch expression
     */
    public static void exhaustive(ExhaustiveDummyValue ignored) {
    }

    private SwitchEnhancements() {
    }
}
