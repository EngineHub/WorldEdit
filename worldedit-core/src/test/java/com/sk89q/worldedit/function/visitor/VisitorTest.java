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

package com.sk89q.worldedit.function.visitor;

import com.sk89q.worldedit.function.LayerFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compares the visitors against a straightforward reference flood fill over
 * randomly generated masks (including negative Y).
 */
class VisitorTest {

    private static final BlockVector3 MIN = BlockVector3.at(-6, -5, -6);
    private static final BlockVector3 MAX = BlockVector3.at(6, 5, 6);

    private static final List<BlockVector3> AXES = List.of(
        BlockVector3.UNIT_X, BlockVector3.UNIT_MINUS_X,
        BlockVector3.UNIT_Y, BlockVector3.UNIT_MINUS_Y,
        BlockVector3.UNIT_Z, BlockVector3.UNIT_MINUS_Z
    );
    private static final List<BlockVector3> DIAGONALS = List.of(
        BlockVector3.at(1, 0, -1), BlockVector3.at(1, 0, 1),
        BlockVector3.at(-1, 0, 1), BlockVector3.at(-1, 0, -1)
    );

    private static final Comparator<BlockVector3> ORDER = Comparator
        .comparingInt(BlockVector3::x).thenComparingInt(BlockVector3::y).thenComparingInt(BlockVector3::z);

    private static Set<BlockVector3> randomSolid(Random random, double density) {
        Set<BlockVector3> solid = new HashSet<>();
        for (BlockVector3 pos : new CuboidRegion(MIN, MAX)) {
            if (random.nextDouble() < density) {
                solid.add(pos);
            }
        }
        return solid;
    }

    private static BlockVector3 pickSeed(Random random, Set<BlockVector3> solid) {
        List<BlockVector3> sorted = new ArrayList<>(solid);
        sorted.sort(ORDER);
        return sorted.get(random.nextInt(sorted.size()));
    }

    /**
     * Reference flood fill; maps every reached position to its depth.
     */
    private static Map<BlockVector3, Integer> referenceFill(BlockVector3 seed, List<BlockVector3> directions,
                                                             BiPredicate<BlockVector3, BlockVector3> canMove) {
        Map<BlockVector3, Integer> depth = new HashMap<>();
        Queue<BlockVector3> queue = new ArrayDeque<>();
        depth.put(seed, 0);
        queue.add(seed);
        while (!queue.isEmpty()) {
            BlockVector3 from = queue.remove();
            for (BlockVector3 dir : directions) {
                BlockVector3 to = from.add(dir);
                if (!depth.containsKey(to) && canMove.test(from, to)) {
                    depth.put(to, depth.get(from) + 1);
                    queue.add(to);
                }
            }
        }
        return depth;
    }

    private static class Recorder implements RegionFunction {
        final List<BlockVector3> order = new ArrayList<>();

        @Override
        public boolean apply(BlockVector3 position) {
            order.add(position);
            return true;
        }
    }

    private static void assertMatchesReference(List<BlockVector3> order, Map<BlockVector3, Integer> reference) {
        assertEquals(order.size(), new HashSet<>(order).size(), "a position was visited more than once");
        assertEquals(reference.keySet(), new HashSet<>(order));
        int lastDepth = 0;
        for (BlockVector3 pos : order) {
            int depth = reference.get(pos);
            assertTrue(depth >= lastDepth, "visited out of breadth-first order at " + pos);
            lastDepth = depth;
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8})
    void recursiveVisitorMatchesReferenceFill(long seed) throws Exception {
        Random random = new Random(seed);
        Set<BlockVector3> solid = randomSolid(random, 0.4 + 0.05 * seed);
        BlockVector3 start = pickSeed(random, solid);

        Recorder recorder = new Recorder();
        RecursiveVisitor visitor = new RecursiveVisitor(solid::contains, recorder);
        visitor.visit(start);
        Operations.complete(visitor);

        assertMatchesReference(recorder.order,
            referenceFill(start, AXES, (_, to) -> solid.contains(to)));
        assertEquals(recorder.order.size(), visitor.getAffected());
    }

    @ParameterizedTest
    @ValueSource(longs = {11, 12, 13, 14})
    void diagonalDirectionsAreFollowed(long seed) throws Exception {
        Random random = new Random(seed);
        Set<BlockVector3> solid = randomSolid(random, 0.3);
        BlockVector3 start = pickSeed(random, solid);

        Recorder recorder = new Recorder();
        BreadthFirstSearch visitor = new BreadthFirstSearch(recorder) {
            {
                addDiagonal();
            }

            @Override
            protected boolean isVisitable(BlockVector3 from, BlockVector3 to) {
                return solid.contains(to);
            }
        };
        visitor.visit(start);
        Operations.complete(visitor);

        List<BlockVector3> directions = new ArrayList<>(AXES);
        directions.addAll(DIAGONALS);
        assertMatchesReference(recorder.order,
            referenceFill(start, directions, (_, to) -> solid.contains(to)));
    }

    @Test
    void clearingDirectionsRestrictsMovement() throws Exception {
        Recorder recorder = new Recorder();
        BreadthFirstSearch visitor = new BreadthFirstSearch(recorder) {
            {
                getDirections().clear();
                getDirections().add(BlockVector3.UNIT_X);
            }

            @Override
            protected boolean isVisitable(BlockVector3 from, BlockVector3 to) {
                return to.x() < 5;
            }
        };
        visitor.visit(BlockVector3.at(0, -3, 0));
        Operations.complete(visitor);

        assertEquals(List.of(
            BlockVector3.at(0, -3, 0), BlockVector3.at(1, -3, 0), BlockVector3.at(2, -3, 0),
            BlockVector3.at(3, -3, 0), BlockVector3.at(4, -3, 0)
        ), recorder.order);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 5})
    void searchIsBoundedByIsVisitableAndBreadthFirst(int maxDepth) throws Exception {
        BlockVector3 origin = BlockVector3.at(0, -2, 0);
        Recorder recorder = new Recorder();
        BreadthFirstSearch visitor = new BreadthFirstSearch(recorder) {
            @Override
            protected boolean isVisitable(BlockVector3 from, BlockVector3 to) {
                return to.subtract(origin).abs().x() + to.subtract(origin).abs().y()
                    + to.subtract(origin).abs().z() <= maxDepth;
            }
        };
        visitor.visit(origin);
        Operations.complete(visitor);

        Map<BlockVector3, Integer> reference = referenceFill(origin, AXES, (_, to) -> {
            BlockVector3 d = to.subtract(origin).abs();
            return d.x() + d.y() + d.z() <= maxDepth;
        });
        assertMatchesReference(recorder.order, reference);
        // a manhattan ball of radius r contains (2r+1)(2r^2+2r+3)/3 points
        assertEquals((2 * maxDepth + 1) * (2 * maxDepth * maxDepth + 2 * maxDepth + 3) / 3, recorder.order.size());
    }

    @Test
    void orderIsDeterministic() throws Exception {
        Set<BlockVector3> solid = randomSolid(new Random(99), 0.6);
        BlockVector3 start = pickSeed(new Random(100), solid);

        List<List<BlockVector3>> runs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Recorder recorder = new Recorder();
            RecursiveVisitor visitor = new RecursiveVisitor(solid::contains, recorder);
            visitor.visit(start);
            Operations.complete(visitor);
            runs.add(recorder.order);
        }
        assertEquals(runs.get(0), runs.get(1));
        assertEquals(runs.get(0), runs.get(2));
    }

    @Test
    void seedsAreVisitedEvenIfMaskFailsAndOnlyOnce() throws Exception {
        BlockVector3 seed = BlockVector3.at(0, -1, 0);
        Recorder recorder = new Recorder();
        RecursiveVisitor visitor = new RecursiveVisitor(_ -> false, recorder);
        visitor.visit(seed);
        visitor.visit(seed);
        Operations.complete(visitor);

        assertEquals(List.of(seed), recorder.order);
        assertEquals(1, visitor.getAffected());
    }

    @Test
    void multipleSeedsAreAllVisitedWithoutDuplicates() throws Exception {
        Set<BlockVector3> solid = new HashSet<>();
        for (BlockVector3 pos : new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(4, 0, 0))) {
            solid.add(pos);
        }
        for (BlockVector3 pos : new CuboidRegion(BlockVector3.at(0, -4, 0), BlockVector3.at(0, 0, 0))) {
            solid.add(pos);
        }
        Recorder recorder = new Recorder();
        RecursiveVisitor visitor = new RecursiveVisitor(solid::contains, recorder);
        visitor.visit(BlockVector3.at(0, 0, 0));
        visitor.visit(BlockVector3.at(4, 0, 0));
        visitor.visit(BlockVector3.at(0, -4, 0));
        Operations.complete(visitor);

        assertEquals(solid, new HashSet<>(recorder.order));
        assertEquals(solid.size(), recorder.order.size());
    }

    @Test
    void affectedOnlyCountsFunctionsThatReturnTrue() throws Exception {
        List<BlockVector3> applied = new ArrayList<>();
        RecursiveVisitor visitor = new RecursiveVisitor(pos -> pos.x() >= 0 && pos.x() < 6 && pos.y() == 0 && pos.z() == 0,
            pos -> {
                applied.add(pos);
                return pos.x() % 2 == 0;
            });
        visitor.visit(BlockVector3.ZERO);
        Operations.complete(visitor);

        assertEquals(6, applied.size());
        assertEquals(3, visitor.getAffected());
    }

    @ParameterizedTest
    @ValueSource(longs = {21, 22, 23, 24, 25, 26})
    void nonRisingVisitorNeverGoesUp(long seed) throws Exception {
        Random random = new Random(seed);
        Set<BlockVector3> solid = randomSolid(random, 0.5);
        BlockVector3 start = pickSeed(random, solid);

        Recorder recorder = new Recorder();
        NonRisingVisitor visitor = new NonRisingVisitor(solid::contains, recorder);
        visitor.visit(start);
        Operations.complete(visitor);

        List<BlockVector3> directions = List.of(
            BlockVector3.UNIT_X, BlockVector3.UNIT_MINUS_X,
            BlockVector3.UNIT_Z, BlockVector3.UNIT_MINUS_Z, BlockVector3.UNIT_MINUS_Y);
        assertMatchesReference(recorder.order, referenceFill(start, directions, (_, to) -> solid.contains(to)));
        assertTrue(recorder.order.stream().allMatch(pos -> pos.y() <= start.y()));
    }

    @ParameterizedTest
    @ValueSource(longs = {31, 32, 33, 34, 35, 36})
    void downwardVisitorOnlyMovesSidewaysAtBaseY(long seed) throws Exception {
        Random random = new Random(seed);
        Set<BlockVector3> solid = randomSolid(random, 0.6);
        int baseY = (int) (seed % 3) - 1;
        List<BlockVector3> atBase = new ArrayList<>();
        for (BlockVector3 pos : solid) {
            if (pos.y() == baseY) {
                atBase.add(pos);
            }
        }
        atBase.sort(ORDER);
        BlockVector3 start = atBase.get(random.nextInt(atBase.size()));

        Recorder recorder = new Recorder();
        DownwardVisitor visitor = new DownwardVisitor(solid::contains, recorder, baseY);
        visitor.visit(start);
        Operations.complete(visitor);

        List<BlockVector3> directions = List.of(
            BlockVector3.UNIT_X, BlockVector3.UNIT_MINUS_X,
            BlockVector3.UNIT_Z, BlockVector3.UNIT_MINUS_Z, BlockVector3.UNIT_MINUS_Y);
        Map<BlockVector3, Integer> reference = referenceFill(start, directions, (from, to) ->
            solid.contains(to) && (to.y() < from.y() || from.y() == baseY));
        assertMatchesReference(recorder.order, reference);
        assertTrue(recorder.order.stream().allMatch(pos -> pos.y() <= baseY));
    }

    @Test
    void downwardVisitorFromAboveBaseOnlyDescendsUntilBase() throws Exception {
        Mask box = pos -> pos.x() >= 1 && pos.x() <= 3 && pos.z() >= 1 && pos.z() <= 3 && pos.y() >= -3;
        Recorder recorder = new Recorder();
        DownwardVisitor visitor = new DownwardVisitor(box, recorder, 0);
        visitor.visit(BlockVector3.at(2, 2, 2));
        Operations.complete(visitor);

        Set<BlockVector3> expected = new HashSet<>();
        expected.add(BlockVector3.at(2, 2, 2));
        expected.add(BlockVector3.at(2, 1, 2));
        new CuboidRegion(BlockVector3.at(1, -3, 1), BlockVector3.at(3, 0, 3)).forEach(expected::add);
        assertEquals(expected, new HashSet<>(recorder.order));
        assertEquals(expected.size(), recorder.order.size());
    }

    @Test
    void regionVisitorVisitsEveryBlockOnce() throws Exception {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(-3, -64, -2), BlockVector3.at(2, -60, 4));
        Recorder recorder = new Recorder();
        RegionVisitor visitor = new RegionVisitor(region, recorder);
        Operations.complete(visitor);

        assertEquals(region.getVolume(), recorder.order.size());
        assertEquals(region.getVolume(), visitor.getAffected());
        assertEquals(recorder.order.size(), new HashSet<>(recorder.order).size());
        assertTrue(recorder.order.stream().allMatch(region::contains));
    }

    @Test
    void regionVisitorCountsOnlyAcceptedBlocks() throws Exception {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, -2, 0), BlockVector3.at(3, 1, 3));
        RegionVisitor visitor = new RegionVisitor(region, pos -> pos.y() < 0);
        Operations.complete(visitor);

        assertEquals(2 * 4 * 4, visitor.getAffected());
    }

    @Test
    void flatRegionVisitorVisitsEveryColumnOnce() throws Exception {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(-3, -20, -2), BlockVector3.at(2, 30, 4));
        List<BlockVector2> columns = new ArrayList<>();
        FlatRegionVisitor visitor = new FlatRegionVisitor(region, column -> {
            columns.add(column);
            return column.x() < 0;
        });
        Operations.complete(visitor);

        assertEquals(6 * 7, columns.size());
        assertEquals(columns.size(), new HashSet<>(columns).size());
        assertEquals(3 * 7, visitor.getAffected());
    }

    /**
     * A heightmap-backed layer function: blocks at or below the height are ground.
     */
    private record Layers(Map<BlockVector2, Integer> heights, List<String> log, int stopAtDepth)
        implements LayerFunction {

        @Override
        public boolean isGround(BlockVector3 position) {
            return position.y() <= heights.get(position.toBlockVector2());
        }

        @Override
        public boolean apply(BlockVector3 position, int depth) {
            log.add(position + "@" + depth);
            return depth < stopAtDepth;
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MAX_VALUE, 0, 2})
    void layerVisitorWalksDownFromTheSurface(int stopAtDepth) throws Exception {
        int minY = -4;
        int maxY = 6;
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(3, 0, 3));
        Random random = new Random(5);
        Map<BlockVector2, Integer> heights = new HashMap<>();
        for (BlockVector2 column : region.asFlatRegion()) {
            heights.put(column, random.nextInt(14) - 6); // from below minY to above maxY
        }

        List<String> log = new ArrayList<>();
        LayerVisitor visitor = new LayerVisitor(region, minY, maxY, new Layers(heights, log, stopAtDepth));
        visitor.setMask(column -> column.x() != 1);
        Operations.complete(visitor);

        Set<String> expected = new HashSet<>();
        for (Map.Entry<BlockVector2, Integer> entry : heights.entrySet()) {
            BlockVector2 column = entry.getKey();
            int height = entry.getValue();
            if (column.x() == 1 || height > maxY) {
                continue;
            }
            for (int y = height; y >= minY; y--) {
                int depth = height - y;
                expected.add(column.toBlockVector3(y) + "@" + depth);
                if (depth >= stopAtDepth) {
                    break;
                }
            }
        }
        assertEquals(expected, new HashSet<>(log));
        assertEquals(log.size(), new HashSet<>(log).size());
    }

    @Test
    void layerVisitorRejectsInvertedRange() {
        CuboidRegion region = new CuboidRegion(BlockVector3.ZERO, BlockVector3.ZERO);
        assertThrows(IllegalArgumentException.class,
            () -> new LayerVisitor(region, 5, 4, new Layers(Map.of(), new ArrayList<>(), 0)));
    }

    @Test
    void layerVisitorWithEqualMinAndMaxVisitsOneLayer() throws Exception {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(1, 0, 0));
        Map<BlockVector2, Integer> heights = Map.of(BlockVector2.at(0, 0), -10, BlockVector2.at(1, 0), 3);
        List<String> log = new ArrayList<>();
        Operations.complete(new LayerVisitor(region, 3, 3, new Layers(heights, log, Integer.MAX_VALUE)));

        assertEquals(List.of(BlockVector3.at(1, 3, 0) + "@0"), log);
    }
}
