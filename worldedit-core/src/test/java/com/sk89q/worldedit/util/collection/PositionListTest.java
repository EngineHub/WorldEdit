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

import com.sk89q.worldedit.util.test.VariedVectorGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class PositionListTest {

    static class Long extends PositionListTest {
        @Override
        protected PositionList createPositionList() {
            return new LongPositionList();
        }
    }

    static class Vector extends PositionListTest {
        @Override
        protected PositionList createPositionList() {
            return new VectorPositionList();
        }
    }

    private final VariedVectorGenerator generator = new VariedVectorGenerator(true);

    protected abstract PositionList createPositionList();

    @Test
    @DisplayName("calling add(vec) increases size by 1")
    void addIncreasesSizeByOne() {
        generator.makeVectorsStream().forEach(vec -> {
            PositionList positionList = createPositionList();
            positionList.add(vec);
            assertEquals(1, positionList.size());
        });
    }

    @Test
    @DisplayName("calling get(0) after add(vec) returns vec")
    void canGetVectorAfterAdd() {
        generator.makeVectorsStream().forEach(vec -> {
            PositionList positionList = createPositionList();
            positionList.add(vec);
            assertEquals(vec, positionList.get(0));
        });
    }

    @Test
    @DisplayName("calling iterator().hasNext() after add(vec) returns true")
    void hasNextAfterAdd() {
        generator.makeVectorsStream().forEach(vec -> {
            PositionList positionList = createPositionList();
            positionList.add(vec);
            assertTrue(positionList.iterator().hasNext());
        });
    }

    @Test
    @DisplayName("calling iterator().next() after add(vec) returns vec")
    void nextAfterAdd() {
        generator.makeVectorsStream().forEach(vec -> {
            PositionList positionList = createPositionList();
            positionList.add(vec);
            assertEquals(vec, positionList.iterator().next());
        });
    }

    @Test
    @DisplayName("calling reverseIterator().hasNext() after add(vec) returns true")
    void reverseHasNextAfterAdd() {
        generator.makeVectorsStream().forEach(vec -> {
            PositionList positionList = createPositionList();
            positionList.add(vec);
            assertTrue(positionList.reverseIterator().hasNext());
        });
    }

    @Test
    @DisplayName("calling reverseIterator().next() after add(vec) returns vec")
    void reverseNextAfterAdd() {
        generator.makeVectorsStream().forEach(vec -> {
            PositionList positionList = createPositionList();
            positionList.add(vec);
            assertEquals(vec, positionList.reverseIterator().next());
        });
    }

    @Test
    @DisplayName("calling clear() after add(vec) makes the size() zero")
    void clearAfterAdd() {
        generator.makeVectorsStream().forEach(vec -> {
            PositionList positionList = createPositionList();
            positionList.add(vec);
            positionList.clear();
            assertEquals(0, positionList.size());
        });
    }
}
