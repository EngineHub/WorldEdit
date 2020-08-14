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

package com.sk89q.worldedit.util.collection;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.test.VariedVectors;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class PositionListTest {

    static class Long extends PositionListTest {
        protected Long() {
            super(new LongPositionList());
        }
    }

    static class Vector extends PositionListTest {
        protected Vector() {
            super(new VectorPositionList());
        }
    }

    private final PositionList positionList;

    protected PositionListTest(PositionList positionList) {
        this.positionList = positionList;
    }

    @VariedVectors.Test(capToVanilla = true)
    @DisplayName("calling add(vec) increases size by 1")
    void addIncreasesSizeByOne(BlockVector3 vec) {
        positionList.add(vec);
        assertEquals(1, positionList.size());
    }

    @VariedVectors.Test(capToVanilla = true)
    @DisplayName("calling get(0) after add(vec) returns vec")
    void canGetVectorAfterAdd(BlockVector3 vec) {
        positionList.add(vec);
        assertEquals(vec, positionList.get(0));
    }

    @VariedVectors.Test(capToVanilla = true)
    @DisplayName("calling iterator().hasNext() after add(vec) returns true")
    void hasNextAfterAdd(BlockVector3 vec) {
        positionList.add(vec);
        assertTrue(positionList.iterator().hasNext());
    }

    @VariedVectors.Test(capToVanilla = true)
    @DisplayName("calling iterator().next() after add(vec) returns vec")
    void nextAfterAdd(BlockVector3 vec) {
        positionList.add(vec);
        assertEquals(vec, positionList.iterator().next());
    }

    @VariedVectors.Test(capToVanilla = true)
    @DisplayName("calling reverseIterator().hasNext() after add(vec) returns true")
    void reverseHasNextAfterAdd(BlockVector3 vec) {
        positionList.add(vec);
        assertTrue(positionList.reverseIterator().hasNext());
    }

    @VariedVectors.Test(capToVanilla = true)
    @DisplayName("calling reverseIterator().next() after add(vec) returns vec")
    void reverseNextAfterAdd(BlockVector3 vec) {
        positionList.add(vec);
        assertEquals(vec, positionList.reverseIterator().next());
    }

    @VariedVectors.Test(capToVanilla = true)
    @DisplayName("calling clear() after add(vec) makes the size() zero")
    void clearAfterAdd(BlockVector3 vec) {
        positionList.add(vec);
        positionList.clear();
        assertEquals(0, positionList.size());
    }
}
