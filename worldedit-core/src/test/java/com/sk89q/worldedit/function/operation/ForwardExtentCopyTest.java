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


package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.changeset.ArrayListHistory;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.util.test.TestWorld;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static com.sk89q.worldedit.util.test.ExtentTestSupport.block;
import static com.sk89q.worldedit.util.test.ExtentTestSupport.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ForwardExtentCopyTest extends BaseWorldEditTest {

    private static final List<String> BLOCK_IDS = List.of(
        "minecraft:stone", "minecraft:oak_planks", "minecraft:oak_stairs", "minecraft:chest", "minecraft:glass"
    );
    private static final EntityType ITEM = new EntityType("minecraft:item");

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    static IntStream seeds() {
        return IntStream.range(0, 25);
    }

    /**
     * Fills about two thirds of the region with random blocks, leaving the rest as air.
     */
    private static void fill(Extent extent, Region region, Random random) throws WorldEditException {
        for (BlockVector3 position : region) {
            if (random.nextInt(3) != 0) {
                String id = BLOCK_IDS.get(random.nextInt(BLOCK_IDS.size()));
                List<BlockState> states = BlockTypes.get(id).getAllStates();
                extent.setBlock(position, states.get(random.nextInt(states.size())));
            }
        }
    }

    private static CuboidRegion randomRegion(Random random) {
        BlockVector3 min = BlockVector3.at(random.nextInt(21) - 10, random.nextInt(21) - 15, random.nextInt(21) - 10);
        return new CuboidRegion(min, min.add(random.nextInt(5), random.nextInt(5), random.nextInt(5)));
    }

    private static BlockVector3 randomOffset(Random random) {
        return BlockVector3.at(random.nextInt(61) - 30, random.nextInt(61) - 30, random.nextInt(61) - 30);
    }

    private static void copy(Extent source, Region region, BlockVector3 from, Extent destination, BlockVector3 to,
                             Transform transform) throws WorldEditException {
        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, from, destination, to);
        copy.setTransform(transform);
        Operations.complete(copy);
    }

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void copyPlacesEveryBlockAtTheOffsetPosition(int seed) throws WorldEditException {
        Random random = new Random(seed);
        CuboidRegion region = randomRegion(random);
        TestWorld source = new TestWorld("source");
        fill(source, region, random);
        BlockVector3 to = randomOffset(random);
        // Not the region minimum, so negative and positive offsets from 'from' are covered
        BlockVector3 from = region.getMinimumPoint().add(random.nextInt(2), 0, random.nextInt(2));
        TestWorld destination = new TestWorld("destination");

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, from, destination, to);
        Operations.complete(copy);

        for (BlockVector3 position : region) {
            assertEquals(source.getFullBlock(position), destination.getFullBlock(position.subtract(from).add(to)),
                () -> "copied from " + position);
        }
        long nonAir = source.getBlocks().keySet().stream().filter(region::contains).count();
        assertEquals(nonAir, destination.getBlocks().size());
        assertEquals(nonAir, copy.getAffected(), "only blocks that changed are counted");
    }

    @Test
    void defaultFromPositionIsTheRegionMinimum() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(-3, -2, 5), BlockVector3.at(-2, -1, 6));
        TestWorld source = new TestWorld();
        source.setBlock(BlockVector3.at(-3, -2, 5), block("minecraft:stone"));
        source.setBlock(BlockVector3.at(-2, -1, 6), block("minecraft:glass"));
        TestWorld destination = new TestWorld();

        Operations.complete(new ForwardExtentCopy(source, region, destination, BlockVector3.at(10, 0, 0)));

        assertEquals(block("minecraft:stone"), destination.getBlock(BlockVector3.at(10, 0, 0)));
        assertEquals(block("minecraft:glass"), destination.getBlock(BlockVector3.at(11, 1, 1)));
        assertEquals(2, destination.getBlocks().size());
    }

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void rotationsAndFlipsMoveBlocksToTheTransformedPosition(int seed) throws WorldEditException {
        Random random = new Random(seed);
        CuboidRegion region = randomRegion(random);
        TestWorld source = new TestWorld("source");
        fill(source, region, random);
        BlockVector3 to = randomOffset(random);
        BlockVector3 from = region.getMinimumPoint().add(random.nextInt(2), 0, 0);
        List<Transform> transforms = List.of(
            new AffineTransform().rotateY(90), new AffineTransform().rotateY(180), new AffineTransform().rotateY(270),
            new AffineTransform().rotateX(90), new AffineTransform().rotateZ(270),
            new AffineTransform().scale(-1, 1, 1), new AffineTransform().scale(1, -1, 1)
        );

        for (Transform transform : transforms) {
            TestWorld destination = new TestWorld("destination");
            copy(source, region, from, destination, to, transform);

            for (BlockVector3 position : region) {
                BlockVector3 expected = transform.apply(position.subtract(from).toVector3()).add(0.5, 0.5, 0.5)
                    .toBlockPoint().add(to);
                // Rotating the corner of a block and rounding to the nearest corner must land on the same block
                assertEquals(source.getFullBlock(position), destination.getFullBlock(expected),
                    () -> "block " + position + " with " + transform + " should be at " + expected);
            }
            assertEquals(source.getBlocks().keySet().stream().filter(region::contains).count(),
                destination.getBlocks().size(), () -> "no blocks lost or overwritten with " + transform);
        }
    }

    @Test
    void quarterTurnPlacesBlocksAtHandComputedPositions() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, -1, 0), BlockVector3.at(2, -1, 0));
        TestWorld source = new TestWorld();
        source.setBlock(BlockVector3.at(0, -1, 0), block("minecraft:stone"));
        source.setBlock(BlockVector3.at(2, -1, 0), block("minecraft:glass"));
        TestWorld destination = new TestWorld();

        copy(source, region, BlockVector3.at(0, -1, 0), destination, BlockVector3.at(10, 5, 10),
            new AffineTransform().rotateY(90));

        // A row along +x is turned into a row along one z direction, keeping its length and the pivot
        assertEquals(block("minecraft:stone"), destination.getBlock(BlockVector3.at(10, 5, 10)));
        BlockVector3 far = destination.getBlocks().keySet().stream()
            .filter(position -> !position.equals(BlockVector3.at(10, 5, 10))).findFirst().orElseThrow();
        assertEquals(block("minecraft:glass"), destination.getBlock(far));
        assertEquals(BlockVector3.at(10, 5, 8), far, "rotateY(90) turns +x into -z");
    }

    @Test
    void flipsAndOtherAxisRotationsPlaceBlocksAtHandComputedPositions() throws WorldEditException {
        BlockVector3 origin = BlockVector3.at(0, -1, 0);
        CuboidRegion region = new CuboidRegion(origin, BlockVector3.at(2, 0, 0));
        TestWorld source = new TestWorld();
        source.setBlock(BlockVector3.at(2, -1, 0), block("minecraft:glass"));
        BlockVector3 to = BlockVector3.at(10, 5, 10);

        TestWorld flipped = new TestWorld();
        copy(source, region, origin, flipped, to, new AffineTransform().scale(-1, 1, 1));
        assertEquals(List.of(BlockVector3.at(8, 5, 10)), List.copyOf(flipped.getBlocks().keySet()));

        TestWorld lifted = new TestWorld();
        copy(source, region, origin, lifted, to, new AffineTransform().rotateZ(90));
        assertEquals(List.of(BlockVector3.at(10, 7, 10)), List.copyOf(lifted.getBlocks().keySet()), "rotateZ(90): +x to +y");

        TestWorld tipped = new TestWorld();
        copy(source, region, origin, tipped, to, new AffineTransform().rotateX(90));
        assertEquals(List.of(BlockVector3.at(12, 5, 10)), List.copyOf(tipped.getBlocks().keySet()), "rotateX keeps +x");
    }

    @Test
    void sourceMaskLimitsWhatIsCopied() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, -2, 0), BlockVector3.at(3, 1, 0));
        TestWorld source = new TestWorld();
        source.setBlock(BlockVector3.at(0, -2, 0), block("minecraft:stone"));
        source.setBlock(BlockVector3.at(1, -1, 0), block("minecraft:glass"));
        source.setBlock(BlockVector3.at(3, 1, 0), block("minecraft:stone"));
        TestWorld destination = new TestWorld();
        destination.setBlock(BlockVector3.at(11, -1, 0), block("minecraft:oak_planks"));

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, destination, BlockVector3.at(10, 0, 0));
        Mask onlyStone = position -> source.getBlock(position).getBlockType().equals(BlockTypes.STONE);
        copy.setSourceMask(onlyStone);
        Operations.complete(copy);

        assertEquals(block("minecraft:stone"), destination.getBlock(BlockVector3.at(10, 0, 0)));
        assertEquals(block("minecraft:stone"), destination.getBlock(BlockVector3.at(13, 3, 0)));
        assertEquals(block("minecraft:oak_planks"), destination.getBlock(BlockVector3.at(11, -1, 0)),
            "masked-out source blocks do not overwrite the destination");
        assertEquals(3, destination.getBlocks().size());
    }

    @Test
    void existingBlockMaskSkipsAirAndLeavesDestinationUntouched() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(2, 0, 0));
        TestWorld source = new TestWorld();
        source.setBlock(BlockVector3.at(1, 0, 0), block("minecraft:stone"));
        TestWorld destination = new TestWorld();
        destination.setBlock(BlockVector3.at(0, 5, 0), block("minecraft:glass"));
        destination.setBlock(BlockVector3.at(2, 5, 0), block("minecraft:glass"));

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, destination, BlockVector3.at(0, 5, 0));
        copy.setSourceMask(new ExistingBlockMask(source));
        Operations.complete(copy);

        assertEquals(block("minecraft:glass"), destination.getBlock(BlockVector3.at(0, 5, 0)));
        assertEquals(block("minecraft:stone"), destination.getBlock(BlockVector3.at(1, 5, 0)));
        assertEquals(block("minecraft:glass"), destination.getBlock(BlockVector3.at(2, 5, 0)));
    }

    @Test
    void sourceFunctionRunsAfterEachBlockHasBeenCopied() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, -3, 0), BlockVector3.at(2, -1, 2));
        Random random = new Random(7);
        TestWorld source = new TestWorld();
        fill(source, region, random);
        Map<BlockVector3, BaseBlock> original = snapshot(source, region);
        BlockVector3 to = BlockVector3.at(20, 10, -20);
        TestWorld destination = new TestWorld();

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, destination, to);
        List<String> problems = new ArrayList<>();
        RegionFunction clearSource = position -> {
            // The copy of this block must already be in place when the source is cleared
            BlockVector3 target = position.subtract(region.getMinimumPoint()).add(to);
            if (!destination.getFullBlock(target).equals(original.get(position))) {
                problems.add(position + " was cleared before it was copied");
            }
            return source.setBlock(position, block("minecraft:air"));
        };
        copy.setSourceFunction(clearSource);
        Operations.complete(copy);

        assertEquals(List.of(), problems);
        assertEquals(0, source.getBlocks().size(), "the source is cleared");
        for (BlockVector3 position : region) {
            assertEquals(original.get(position), destination.getFullBlock(position.subtract(region.getMinimumPoint()).add(to)));
        }
    }

    @Test
    void moveWithSourceFunctionAndMaskOnlyClearsCopiedBlocks() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(1, 0, 0));
        TestWorld world = new TestWorld();
        world.setBlock(BlockVector3.at(0, 0, 0), block("minecraft:stone"));
        world.setBlock(BlockVector3.at(1, 0, 0), block("minecraft:glass"));

        ForwardExtentCopy copy = new ForwardExtentCopy(world, region, world, BlockVector3.at(0, 10, 0));
        copy.setSourceMask(position -> world.getBlock(position).getBlockType().equals(BlockTypes.STONE));
        copy.setSourceFunction(position -> world.setBlock(position, block("minecraft:air")));
        Operations.complete(copy);

        assertEquals(block("minecraft:air"), world.getBlock(BlockVector3.at(0, 0, 0)));
        assertEquals(block("minecraft:glass"), world.getBlock(BlockVector3.at(1, 0, 0)), "masked block stays");
        assertEquals(block("minecraft:stone"), world.getBlock(BlockVector3.at(0, 10, 0)));
        assertEquals(block("minecraft:air"), world.getBlock(BlockVector3.at(1, 10, 0)));
    }

    @Test
    void repetitionsStackTheTransform() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, -5, 0), BlockVector3.at(0, -5, 0));
        TestWorld source = new TestWorld();
        source.setBlock(BlockVector3.at(0, -5, 0), block("minecraft:stone"));
        TestWorld destination = new TestWorld();

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, destination, BlockVector3.at(0, -5, 0));
        copy.setTransform(new AffineTransform().translate(0, 2, 0));
        copy.setRepetitions(3);
        Operations.complete(copy);

        assertEquals(0, copy.getRepetitions());
        assertEquals(List.of(BlockVector3.at(0, -3, 0), BlockVector3.at(0, -1, 0), BlockVector3.at(0, 1, 0)),
            destination.getBlocks().keySet().stream().sorted(BlockVector3.sortByCoordsYzx()).toList());
        assertEquals(3, copy.getAffected());
    }

    @Test
    void zeroRepetitionsCopyNothingAndNegativeIsRejected() throws WorldEditException {
        CuboidRegion region = new CuboidRegion(BlockVector3.ZERO, BlockVector3.ZERO);
        TestWorld source = new TestWorld();
        source.setBlock(BlockVector3.ZERO, block("minecraft:stone"));
        TestWorld destination = new TestWorld();

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, destination, BlockVector3.at(0, 1, 0));
        copy.setRepetitions(0);
        Operations.complete(copy);

        assertEquals(0, destination.getBlocks().size());
        assertEquals(0, copy.getAffected());
        assertThrows(IllegalArgumentException.class, () -> copy.setRepetitions(-1));
    }

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void undoingACopyRestoresTheDestination(int seed) throws WorldEditException {
        Random random = new Random(seed);
        CuboidRegion region = randomRegion(random);
        TestWorld source = new TestWorld("source");
        fill(source, region, random);
        BlockVector3 to = randomOffset(random);
        TestWorld destination = new TestWorld("destination");
        CuboidRegion affected = new CuboidRegion(to.subtract(8, 8, 8), to.add(16, 16, 16));
        fill(destination, affected, random);
        Map<BlockVector3, BaseBlock> before = snapshot(destination, affected);

        ArrayListHistory history = new ArrayListHistory();
        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, region.getMinimumPoint(),
            new ChangeSetExtent(destination, history), to);
        copy.setTransform(new AffineTransform().rotateY(90));
        Operations.complete(copy);
        Map<BlockVector3, BaseBlock> after = snapshot(destination, affected);

        UndoContext context = new UndoContext();
        context.setExtent(destination);
        Operations.complete(ChangeSetExecutor.createUndo(history, context));
        assertEquals(before, snapshot(destination, affected), "undo");
        Operations.complete(ChangeSetExecutor.createRedo(history, context));
        assertEquals(after, snapshot(destination, affected), "redo");
    }

    private static BlockArrayClipboard clipboardWithEntities(Vector3... positions) {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(BlockVector3.at(-5, -5, -5), BlockVector3.at(5, 5, 5)));
        for (Vector3 position : positions) {
            clipboard.createEntity(new Location(clipboard, position), new BaseEntity(ITEM));
        }
        return clipboard;
    }

    private static BlockArrayClipboard emptyClipboard() {
        return new BlockArrayClipboard(new CuboidRegion(BlockVector3.at(-25, -25, -25), BlockVector3.at(35, 35, 35)));
    }

    private static List<Vector3> positions(Extent extent) {
        return extent.getEntities().stream().map(entity -> entity.getLocation().toVector()).toList();
    }

    @Test
    void entitiesInsideTheRegionAreCopiedWithTheOffset() throws WorldEditException {
        BlockArrayClipboard source = clipboardWithEntities(Vector3.at(0.5, -2.5, 0.5), Vector3.at(4.5, 4.5, 4.5));
        BlockArrayClipboard destination = emptyClipboard();
        CuboidRegion region = new CuboidRegion(BlockVector3.at(-1, -3, -1), BlockVector3.at(1, 0, 1));

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, region.getMinimumPoint(), destination,
            BlockVector3.at(10, -20, 30));
        Operations.complete(copy);

        assertEquals(List.of(Vector3.at(11.5, -19.5, 31.5)), positions(destination));
        assertEquals(2, source.getEntities().size(), "entities are not removed by default");
        assertEquals(region.getVolume() + 1, copy.getAffected(), "the entity is counted next to the blocks");
    }

    @Test
    void removingEntitiesMovesThemOutOfTheSource() throws WorldEditException {
        BlockArrayClipboard source = clipboardWithEntities(Vector3.at(0.5, 0.5, 0.5), Vector3.at(4.5, 4.5, 4.5));
        BlockArrayClipboard destination = emptyClipboard();
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(1, 1, 1));

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, destination, BlockVector3.at(20, 0, 0));
        copy.setRemovingEntities(true);
        Operations.complete(copy);

        assertEquals(List.of(Vector3.at(4.5, 4.5, 4.5)), positions(source));
        assertEquals(List.of(Vector3.at(20.5, 0.5, 0.5)), positions(destination));
    }

    @Test
    void disablingEntityCopyLeavesEntitiesAlone() throws WorldEditException {
        BlockArrayClipboard source = clipboardWithEntities(Vector3.at(0.5, 0.5, 0.5));
        BlockArrayClipboard destination = emptyClipboard();
        CuboidRegion region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(1, 1, 1));

        ForwardExtentCopy copy = new ForwardExtentCopy(source, region, destination, BlockVector3.at(20, 0, 0));
        copy.setCopyingEntities(false);
        copy.setRemovingEntities(true);
        Operations.complete(copy);

        assertEquals(1, source.getEntities().size());
        assertEquals(0, destination.getEntities().size());
        assertEquals(region.getVolume(), copy.getAffected(), "only blocks are counted");
    }

    @Test
    void entitiesThatAreNotPasteableAreSkipped() throws WorldEditException {
        BlockArrayClipboard base = clipboardWithEntities(Vector3.at(0.5, 0.5, 0.5), Vector3.at(1.5, 0.5, 0.5));
        List<? extends Entity> entities = base.getEntities();
        Entity keep = entities.get(0);
        Entity skip = entities.get(1);
        EntityProperties pasteable = mock(EntityProperties.class);
        when(pasteable.isPasteable()).thenReturn(true);
        EntityProperties notPasteable = mock(EntityProperties.class);
        when(notPasteable.isPasteable()).thenReturn(false);
        Extent source = new AbstractDelegateExtent(base) {
            @Override
            public List<? extends Entity> getEntities(Region region) {
                return List.of(withFacet(keep, pasteable), withFacet(skip, notPasteable));
            }
        };
        BlockArrayClipboard destination = emptyClipboard();

        Operations.complete(new ForwardExtentCopy(source, new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(2, 2, 2)),
            destination, BlockVector3.at(10, 0, 0)));

        assertEquals(List.of(Vector3.at(10.5, 0.5, 0.5)), positions(destination));
    }

    private static Entity withFacet(Entity entity, EntityProperties properties) {
        return new Entity() {
            @Override
            public BaseEntity getState() {
                return entity.getState();
            }

            @Override
            public Location getLocation() {
                return entity.getLocation();
            }

            @Override
            public boolean setLocation(Location location) {
                return entity.setLocation(location);
            }

            @Override
            public Extent getExtent() {
                return entity.getExtent();
            }

            @Override
            public boolean remove() {
                return entity.remove();
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFacet(Class<? extends T> cls) {
                return cls == EntityProperties.class ? (T) properties : null;
            }
        };
    }
}
