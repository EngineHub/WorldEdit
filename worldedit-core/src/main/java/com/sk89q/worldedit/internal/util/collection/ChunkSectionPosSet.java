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

package com.sk89q.worldedit.internal.util.collection;

import it.unimi.dsi.fastutil.shorts.AbstractShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;

import java.util.BitSet;

/**
 * A set of positions in a chunk section.
 *
 * <p>
 * This has a defined order based on the order of the bits in the internal encoding. It is not guaranteed to be stable
 * between Minecraft versions, but it is stable within a single one.
 * </p>
 */
public final class ChunkSectionPosSet {
    private static int index(int x, int y, int z) {
        // Each value is 0-15, so 4 bits
        // NOTE: This encoding specifically matches the encoding used by SectionPos in Minecraft, do not change unless they do
        return (x << 8) | (z << 4) | y;
    }

    @FunctionalInterface
    public interface PosConsumer {
        void apply(int x, int y, int z);
    }

    private final BitSet mask = new BitSet(4096);

    public void set(int x, int y, int z) {
        mask.set(index(x, y, z));
    }

    public void forEach(PosConsumer consumer) {
        for (int i = mask.nextSetBit(0); i >= 0; i = mask.nextSetBit(i + 1)) {
            consumer.apply((i >> 8) & 0xF, i & 0xF, (i >> 4) & 0xF);
        }
    }

    /**
     * {@return a view of this set as a short collection} These shorts match those used by {@code SectionPos}.
     */
    public ShortCollection asSectionPosEncodedShorts() {
        return new AbstractShortCollection() {
            @Override
            public ShortIterator iterator() {
                return new ShortIterator() {
                    private int next = mask.nextSetBit(0);

                    @Override
                    public short nextShort() {
                        if (!hasNext()) {
                            throw new IllegalStateException();
                        }
                        // Uses the fact that we share the encoding with SectionPos to efficiently map
                        short value = (short) next;
                        next = mask.nextSetBit(next + 1);
                        return value;
                    }

                    @Override
                    public boolean hasNext() {
                        return next >= 0;
                    }
                };
            }

            @Override
            public int size() {
                return mask.cardinality();
            }
        };
    }
}
