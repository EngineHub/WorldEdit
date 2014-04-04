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

package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods related to {@link Pattern}s.
 */
public final class Patterns {

    private Patterns() {
    }

    /**
     * Wrap an old-style pattern and return a new pattern.
     *
     * @param pattern the pattern
     * @return a new-style pattern
     */
    public static Pattern wrap(final com.sk89q.worldedit.patterns.Pattern pattern) {
        checkNotNull(pattern);
        return new Pattern() {
            @Override
            public BaseBlock apply(Vector position) {
                return pattern.next(position);
            }
        };
    }

    /**
     * Wrap a new-style pattern and return an old-style pattern.
     *
     * @param pattern the pattern
     * @return an old-style pattern
     */
    public static com.sk89q.worldedit.patterns.Pattern wrap(final Pattern pattern) {
        checkNotNull(pattern);
        return new com.sk89q.worldedit.patterns.Pattern() {
            @Override
            public BaseBlock next(Vector position) {
                return pattern.apply(position);
            }

            @Override
            public BaseBlock next(int x, int y, int z) {
                return next(new Vector(x, y, z));
            }
        };
    }

}
