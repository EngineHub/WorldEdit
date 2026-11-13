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

package com.sk89q.worldedit.function.mask;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.math.BlockVector3;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the boolean algebra of the mask combinators, over a grid that includes negative coordinates.
 */
class MaskAlgebraTest {

    private static final List<BlockVector3> GRID = grid();

    private static List<BlockVector3> grid() {
        List<BlockVector3> points = new ArrayList<>();
        for (int x = -6; x <= 6; x++) {
            for (int y = -6; y <= 6; y++) {
                for (int z = -6; z <= 6; z++) {
                    points.add(BlockVector3.at(x, y, z));
                }
            }
        }
        return points;
    }

    private static final Mask A = v -> v.x() % 2 == 0;
    private static final Mask B = v -> v.y() % 3 == 0;
    private static final Mask C = v -> v.z() >= 0;

    private static void assertSameMask(Mask expected, Mask actual, String message) {
        for (BlockVector3 point : GRID) {
            assertEquals(expected.test(point), actual.test(point), () -> message + " at " + point);
        }
    }

    @Test
    void intersectionIsFlatteningInvariant() {
        Mask flat = new MaskIntersection(A, B, C);
        assertSameMask(new MaskIntersection(A, new MaskIntersection(B, C)), flat, "A&(B&C)");
        assertSameMask(new MaskIntersection(new MaskIntersection(A, B), C), flat, "(A&B)&C");
        for (BlockVector3 point : GRID) {
            assertEquals(A.test(point) && B.test(point) && C.test(point), flat.test(point), point::toString);
        }
    }

    @Test
    void unionIsFlatteningInvariant() {
        Mask flat = new MaskUnion(A, B, C);
        assertSameMask(new MaskUnion(A, new MaskUnion(B, C)), flat, "A|(B|C)");
        assertSameMask(new MaskUnion(new MaskUnion(A, B), C), flat, "(A|B)|C");
        for (BlockVector3 point : GRID) {
            assertEquals(A.test(point) || B.test(point) || C.test(point), flat.test(point), point::toString);
        }
    }

    @Test
    void doubleNegationIsIdentity() {
        assertSameMask(A, Masks.negate(Masks.negate(A)), "!!A");
        assertSame(A, Masks.negate(Masks.negate(A)), "double negation should unwrap");
    }

    @Test
    void negationIsComplement() {
        Mask notA = Masks.negate(A);
        for (BlockVector3 point : GRID) {
            assertEquals(!A.test(point), notA.test(point), point::toString);
        }
    }

    @Test
    void negatingConstants() {
        BlockVector3 any = BlockVector3.at(-3, -64, 17);
        assertTrue(Masks.alwaysTrue().test(any));
        assertFalse(Masks.negate(Masks.alwaysTrue()).test(any));
        assertTrue(Masks.negate(Masks.negate(Masks.alwaysTrue())).test(any));
        assertSame(Masks.alwaysTrue(), Masks.negate(Masks.negate(Masks.alwaysTrue())));
    }

    @Test
    void deMorganLaws() {
        assertSameMask(new MaskUnion(Masks.negate(A), Masks.negate(B)), Masks.negate(new MaskIntersection(A, B)),
            "!(A&B) == !A|!B");
        assertSameMask(new MaskIntersection(Masks.negate(A), Masks.negate(B)), Masks.negate(new MaskUnion(A, B)),
            "!(A|B) == !A&!B");
    }

    @Test
    void distributivity() {
        assertSameMask(new MaskUnion(new MaskIntersection(A, B), new MaskIntersection(A, C)),
            new MaskIntersection(A, new MaskUnion(B, C)), "A&(B|C) == (A&B)|(A&C)");
    }

    @Test
    void intersectionAndUnionIgnoreDuplicates() {
        assertSameMask(A, new MaskIntersection(A, A, A), "A&A&A");
        assertSameMask(A, new MaskUnion(A, A), "A|A");
    }

    @Test
    void addingMasksAfterConstruction() {
        MaskIntersection intersection = new MaskIntersection(A);
        intersection.add(B, C);
        assertSameMask(new MaskIntersection(A, B, C), intersection, "added");
        assertEquals(3, intersection.getMasks().size());
    }

    @Test
    @Disabled("Documented as 'requires that all masks return true' (a logical AND), so an empty intersection "
        + "should be vacuously true, but MaskIntersection.test returns false for an empty mask list "
        + "(explicit isEmpty() guard). Expected true, actual false for new MaskIntersection(List.of()). "
        + "Possibly intentional, in which case the Javadoc is what is wrong")
    void emptyIntersectionIsTrue() {
        assertTrue(new MaskIntersection(ImmutableList.of()).test(BlockVector3.ZERO));
    }

    @Test
    void emptyUnionIsFalse() {
        assertFalse(new MaskUnion(ImmutableList.of()).test(BlockVector3.ZERO));
    }

    @Test
    void mask2dConversionRequiresEveryMemberToSupportIt() {
        Mask flat = Masks.alwaysTrue();
        assertNotNull(new MaskIntersection(flat, flat).toMask2D());
        assertNotNull(new MaskUnion(flat, flat).toMask2D());
        assertNull(new MaskIntersection(flat, A).toMask2D(), "A has no 2D form");
        assertNull(new MaskUnion(flat, A).toMask2D());
        assertNull(Masks.negate(A).toMask2D());
        assertNotNull(Masks.negate(flat).toMask2D());
    }

    @Test
    void offsetMaskTestsTheShiftedPosition() {
        BlockVector3 offset = BlockVector3.at(1, -2, 3);
        @SuppressWarnings("deprecation")
        OffsetMask mask = new OffsetMask(A, offset);
        for (BlockVector3 point : GRID) {
            assertEquals(A.test(point.add(offset)), mask.test(point), point::toString);
        }
    }

    @Test
    void offsetMaskSettersTakeEffect() {
        @SuppressWarnings("deprecation")
        OffsetMask mask = new OffsetMask(A, BlockVector3.ZERO);
        mask.setOffset(BlockVector3.at(1, 0, 0));
        assertEquals(A.test(BlockVector3.at(1, 0, 0)), mask.test(BlockVector3.ZERO));
        mask.setMask(Masks.alwaysTrue());
        assertTrue(mask.test(BlockVector3.at(1, 1, 1)));
    }

    @Test
    void singleOffsetsMaskMatchesOffsetMask() {
        BlockVector3 offset = BlockVector3.at(0, -1, 0);
        Mask single = OffsetsMask.single(A, offset);
        for (BlockVector3 point : GRID) {
            assertEquals(A.test(point.add(offset)), single.test(point), point::toString);
        }
    }

    @Test
    void adjacentMaskNeedsAtLeastOneNeighbour() {
        Mask adjacent = OffsetsMask.adjacent(A);
        BlockVector3[] neighbours = {
            BlockVector3.at(1, 0, 0), BlockVector3.at(-1, 0, 0), BlockVector3.at(0, 1, 0),
            BlockVector3.at(0, -1, 0), BlockVector3.at(0, 0, 1), BlockVector3.at(0, 0, -1)};
        for (BlockVector3 point : GRID) {
            boolean expected = false;
            for (BlockVector3 neighbour : neighbours) {
                expected |= A.test(point.add(neighbour));
            }
            assertEquals(expected, adjacent.test(point), point::toString);
        }
    }

    static Stream<Arguments> matchCounts() {
        return Stream.of(
            Arguments.of(0, 0), Arguments.of(1, 1), Arguments.of(2, 4), Arguments.of(0, 3), Arguments.of(3, 3),
            Arguments.of(4, 4), Arguments.of(2, 2), Arguments.of(5, 6), Arguments.of(0, Integer.MAX_VALUE)
        );
    }

    @ParameterizedTest(name = "min={0} max={1}")
    @MethodSource("matchCounts")
    void offsetsMaskCountsMatches(int min, int max) {
        // A point with even x has 4 matching neighbours (y and z), one with odd x has 2 (the x neighbours)
        OffsetsMask mask = OffsetsMask.builder(A).minMatches(min).maxMatches(max).build();
        for (BlockVector3 point : GRID) {
            int matches = 0;
            for (BlockVector3 offset : mask.getOffsets()) {
                if (A.test(point.add(offset))) {
                    matches++;
                }
            }
            assertEquals(min <= matches && matches <= max, mask.test(point), point::toString);
        }
    }

    @Test
    void excludeSelfRejectsPositionsThatMatchThemselves() {
        Mask positiveX = v -> v.x() > 0;
        Mask withSelf = OffsetsMask.builder(positiveX).build();
        Mask withoutSelf = OffsetsMask.builder(positiveX).excludeSelf(true).build();
        BlockVector3 insideNextToEdge = BlockVector3.at(1, 0, 0);
        BlockVector3 outsideNextToEdge = BlockVector3.at(0, 0, 0);
        assertTrue(withSelf.test(insideNextToEdge));
        assertFalse(withoutSelf.test(insideNextToEdge));
        assertTrue(withSelf.test(outsideNextToEdge));
        assertTrue(withoutSelf.test(outsideNextToEdge));
    }

    @Test
    void offsetsMaskValidatesItsArguments() {
        assertThrows(IllegalArgumentException.class, () -> OffsetsMask.builder(A).minMatches(3).maxMatches(2).build());
        assertThrows(IllegalArgumentException.class, () -> OffsetsMask.builder(A).minMatches(-1).build());
        assertThrows(IllegalArgumentException.class, () -> OffsetsMask.builder(A).minMatches(7).build());
        assertThrows(IllegalArgumentException.class,
            () -> OffsetsMask.builder(A).offsets(ImmutableList.of()).minMatches(0).build());
        assertThrows(IllegalArgumentException.class,
            () -> OffsetsMask.builder(A).offsets(ImmutableList.of(BlockVector3.UNIT_X)).minMatches(2).build());
    }

    @Test
    void offsetsMaskKeepsDuplicateOffsetsOnce() {
        OffsetsMask mask = OffsetsMask.builder(A)
            .offsets(ImmutableList.of(BlockVector3.UNIT_X, BlockVector3.UNIT_X, BlockVector3.UNIT_Y)).build();
        assertEquals(2, mask.getOffsets().size());
    }

    static Stream<Arguments> heightRanges() {
        return Stream.of(
            Arguments.of(0, 255), Arguments.of(-64, 319), Arguments.of(-64, -64), Arguments.of(-128, -1),
            Arguments.of(0, 0), Arguments.of(Integer.MIN_VALUE, Integer.MAX_VALUE), Arguments.of(5, 5)
        );
    }

    @ParameterizedTest(name = "[{0}, {1}]")
    @MethodSource("heightRanges")
    void boundedHeightMaskIsInclusiveOnBothEnds(int minY, int maxY) {
        BoundedHeightMask mask = new BoundedHeightMask(minY, maxY);
        assertTrue(mask.test(BlockVector3.at(0, minY, 0)), "minY is included");
        assertTrue(mask.test(BlockVector3.at(100, maxY, -100)), "maxY is included");
        if (minY > Integer.MIN_VALUE) {
            assertFalse(mask.test(BlockVector3.at(0, minY - 1, 0)), "below minY");
        }
        if (maxY < Integer.MAX_VALUE) {
            assertFalse(mask.test(BlockVector3.at(0, maxY + 1, 0)), "above maxY");
        }
    }

    @Test
    void boundedHeightMaskIgnoresHorizontalPosition() {
        BoundedHeightMask mask = new BoundedHeightMask(-10, 10);
        for (BlockVector3 point : GRID) {
            assertEquals(point.y() >= -10 && point.y() <= 10, mask.test(point), point::toString);
        }
        assertTrue(mask.test(BlockVector3.at(Integer.MAX_VALUE, 0, Integer.MIN_VALUE)));
    }

    @Test
    void boundedHeightMaskRejectsInvertedRange() {
        assertThrows(IllegalArgumentException.class, () -> new BoundedHeightMask(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new BoundedHeightMask(0, -64));
    }
}
