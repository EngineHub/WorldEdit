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
