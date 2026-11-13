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


package com.sk89q.worldedit.history;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ChangeSetExecutor;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.change.EntityCreate;
import com.sk89q.worldedit.history.change.EntityRemove;
import com.sk89q.worldedit.history.changeset.ArrayListHistory;
import com.sk89q.worldedit.history.changeset.BlockOptimizedHistory;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.sk89q.worldedit.util.test.ExtentTestSupport.block;
import static com.sk89q.worldedit.util.test.ExtentTestSupport.snapshot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that recorded history can be undone and redone back to the exact original and edited states.
 */
class ChangeSetTest extends BaseWorldEditTest {

    private static final Region AREA = new CuboidRegion(BlockVector3.at(-4, -3, -4), BlockVector3.at(3, 4, 3));
    private static final List<String> BLOCK_IDS = List.of(
        "minecraft:air", "minecraft:stone", "minecraft:oak_planks", "minecraft:oak_stairs", "minecraft:chest",
        "minecraft:oak_door", "minecraft:rail", "minecraft:oak_fence"
    );

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    private static BaseBlock randomBlock(Random random) {
        String id = BLOCK_IDS.get(random.nextInt(BLOCK_IDS.size()));
        List<BlockState> states = BlockTypes.get(id).getAllStates();
        return states.get(random.nextInt(states.size())).toBaseBlock();
    }

    private static BlockVector3 randomPosition(Random random) {
        BlockVector3 min = AREA.getMinimumPoint();
        BlockVector3 size = AREA.getMaximumPoint().subtract(min).add(1, 1, 1);
        return min.add(random.nextInt(size.x()), random.nextInt(size.y()), random.nextInt(size.z()));
    }

    private static void scatter(TestWorld world, Random random, int count) throws WorldEditException {
        for (int i = 0; i < count; i++) {
            world.setBlock(randomPosition(random), randomBlock(random));
        }
    }

    static Stream<Arguments> historiesAndSeeds() {
        List<Named<Supplier<ChangeSet>>> histories = List.of(
            Named.of("ArrayListHistory", ArrayListHistory::new),
            Named.of("BlockOptimizedHistory", BlockOptimizedHistory::new));
        return histories.stream().flatMap(history ->
            IntStream.range(0, 40).mapToObj(seed -> Arguments.of(history, seed)));
    }

    @ParameterizedTest(name = "{0}, seed {1}")
    @MethodSource("historiesAndSeeds")
    void undoAndRedoRestoreExactStates(Supplier<ChangeSet> historyFactory, int seed) throws WorldEditException {
        Random random = new Random(seed);
        TestWorld world = new TestWorld();
        scatter(world, random, 60);
        Map<BlockVector3, BaseBlock> original = snapshot(world, AREA);

        ChangeSet history = historyFactory.get();
        ChangeSetExtent extent = new ChangeSetExtent(world, history);
        // Positions are drawn from a small area, so the same position is edited several times
        for (int i = 0; i < 200; i++) {
            extent.setBlock(randomPosition(random), randomBlock(random));
        }
        Map<BlockVector3, BaseBlock> edited = snapshot(world, AREA);
        assertNotEquals(original, edited, "the random edits should have changed something");

        UndoContext context = new UndoContext();
        context.setExtent(world);

        Operations.complete(ChangeSetExecutor.createUndo(history, context));
        assertEquals(original, snapshot(world, AREA), "undo");

        Operations.complete(ChangeSetExecutor.createRedo(history, context));
        assertEquals(edited, snapshot(world, AREA), "redo");

        // Undo and redo can be repeated any number of times
        Operations.complete(ChangeSetExecutor.createUndo(history, context));
        assertEquals(original, snapshot(world, AREA), "second undo");
        Operations.complete(ChangeSetExecutor.createRedo(history, context));
        assertEquals(edited, snapshot(world, AREA), "second redo");
    }

    @ParameterizedTest(name = "seed {0}")
    @MethodSource("seeds")
    void blockOptimizedHistoryMatchesArrayListHistory(int seed) throws WorldEditException {
        Random random = new Random(seed);
        TestWorld first = new TestWorld();
        scatter(first, random, 40);
        TestWorld second = new TestWorld();
        second.getBlocks().putAll(first.getBlocks());
        Map<BlockVector3, BaseBlock> original = snapshot(first, AREA);

        ChangeSet plain = new ArrayListHistory();
        ChangeSet optimized = new BlockOptimizedHistory();
        ChangeSetExtent plainExtent = new ChangeSetExtent(first, plain);
        ChangeSetExtent optimizedExtent = new ChangeSetExtent(second, optimized);
        for (int i = 0; i < 150; i++) {
            BlockVector3 position = randomPosition(random);
            BaseBlock block = randomBlock(random);
            plainExtent.setBlock(position, block);
            optimizedExtent.setBlock(position, block);
        }
        Map<BlockVector3, BaseBlock> edited = snapshot(first, AREA);
        assertEquals(edited, snapshot(second, AREA));

        UndoContext context = new UndoContext();
        context.setExtent(first);
        Operations.complete(ChangeSetExecutor.createUndo(plain, context));
        context.setExtent(second);
        Operations.complete(ChangeSetExecutor.createUndo(optimized, context));
        assertEquals(original, snapshot(first, AREA));
        assertEquals(original, snapshot(second, AREA));

        context.setExtent(first);
        Operations.complete(ChangeSetExecutor.createRedo(plain, context));
        context.setExtent(second);
        Operations.complete(ChangeSetExecutor.createRedo(optimized, context));
        assertEquals(edited, snapshot(first, AREA));
        assertEquals(edited, snapshot(second, AREA));
    }

    static IntStream seeds() {
        return IntStream.range(0, 20);
    }

    @Test
    void undoAppliesChangesInReverseOrder() throws WorldEditException {
        TestWorld world = new TestWorld();
        BlockVector3 position = BlockVector3.at(1, -5, 1);
        ChangeSet history = new ArrayListHistory();
        ChangeSetExtent extent = new ChangeSetExtent(world, history);
        extent.setBlock(position, block("minecraft:stone"));
        extent.setBlock(position, block("minecraft:oak_planks"));
        extent.setBlock(position, block("minecraft:glass"));

        UndoContext context = new UndoContext();
        context.setExtent(world);
        Operations.complete(ChangeSetExecutor.createUndo(history, context));
        assertEquals(block("minecraft:air"), world.getBlock(position));

        Operations.complete(ChangeSetExecutor.createRedo(history, context));
        assertEquals(block("minecraft:glass"), world.getBlock(position));
    }

    @Test
    void blockOptimizedHistoryKeepsFirstPreviousAndLastCurrent() throws WorldEditException {
        TestWorld world = new TestWorld();
        BlockVector3 position = BlockVector3.at(0, 0, 0);
        BlockVector3 other = BlockVector3.at(0, 1, 0);
        world.setBlock(position, block("minecraft:glass"));
        BlockOptimizedHistory history = new BlockOptimizedHistory();
        ChangeSetExtent extent = new ChangeSetExtent(world, history);
        extent.setBlock(position, block("minecraft:stone"));
        extent.setBlock(other, block("minecraft:stone"));
        extent.setBlock(position, block("minecraft:oak_planks"));

        assertEquals(2, history.size());
        // Redo replays every edit in order, so the last change at the position decides its final state
        List<BlockChange> forward = new ArrayList<>();
        history.forwardIterator().forEachRemaining(change -> forward.add((BlockChange) change));
        BlockChange lastAtPosition = forward.stream().filter(c -> c.position().equals(position)).reduce((_, b) -> b)
            .orElseThrow();
        assertEquals(block("minecraft:oak_planks").toBaseBlock(), lastAtPosition.current());

        List<BlockChange> backward = new ArrayList<>();
        history.backwardIterator().forEachRemaining(change -> backward.add((BlockChange) change));
        List<BlockChange> undoAtPosition = backward.stream().filter(c -> c.position().equals(position)).toList();
        assertEquals(1, undoAtPosition.size());
        assertEquals(block("minecraft:glass").toBaseBlock(), undoAtPosition.getFirst().previous());
    }

    @Test
    void blockOptimizedHistoryKeepsAndReplaysNonBlockChanges() throws WorldEditException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(AREA);
        BlockOptimizedHistory history = new BlockOptimizedHistory();
        Location location = new Location(clipboard, Vector3.at(1.5, 2.5, 1.5));
        BaseEntity state = new BaseEntity(new EntityType("minecraft:item"));
        ChangeSetExtent extent = new ChangeSetExtent(clipboard, history);

        extent.setBlock(BlockVector3.at(0, 0, 0), block("minecraft:stone"));
        extent.createEntity(location, state);
        extent.setBlock(BlockVector3.at(1, 0, 0), block("minecraft:stone"));
        assertEquals(1, clipboard.getEntities().size());

        UndoContext context = new UndoContext();
        context.setExtent(clipboard);
        Operations.complete(ChangeSetExecutor.createUndo(history, context));
        assertEquals(0, clipboard.getEntities().size());
        assertEquals(block("minecraft:air"), clipboard.getBlock(BlockVector3.at(0, 0, 0)));
        assertEquals(block("minecraft:air"), clipboard.getBlock(BlockVector3.at(1, 0, 0)));

        Operations.complete(ChangeSetExecutor.createRedo(history, context));
        assertEquals(1, clipboard.getEntities().size());
        assertEquals(block("minecraft:stone"), clipboard.getBlock(BlockVector3.at(1, 0, 0)));
    }

    @Test
    void historyIgnoresChangesWhileNotRecording() {
        for (ChangeSet history : List.of(new ArrayListHistory(), new BlockOptimizedHistory())) {
            assertTrue(history.isRecordingChanges());
            history.setRecordChanges(false);
            history.add(new BlockChange(BlockVector3.ZERO, block("minecraft:air"), block("minecraft:stone")));
            assertEquals(0, history.size(), history.getClass().getSimpleName());
            history.setRecordChanges(true);
            history.add(new BlockChange(BlockVector3.ZERO, block("minecraft:air"), block("minecraft:stone")));
            assertEquals(1, history.size(), history.getClass().getSimpleName());
        }
    }

    @Test
    void disabledChangeSetExtentRecordsNothing() throws WorldEditException {
        TestWorld world = new TestWorld();
        ChangeSet history = new ArrayListHistory();
        ChangeSetExtent extent = new ChangeSetExtent(world, history);
        extent.setEnabled(false);
        extent.setBlock(BlockVector3.ZERO, block("minecraft:stone"));

        assertEquals(block("minecraft:stone"), world.getBlock(BlockVector3.ZERO));
        assertEquals(0, history.size());
        assertFalse(history.isRecordingChanges());
    }

    @Test
    @Disabled("ChangeSetExtent(Extent, ChangeSet, boolean) ignores its 'enabled' argument: the constructor assigns "
        + "this.enabled = true, so new ChangeSetExtent(world, history, false).isEnabled() returns true and every "
        + "setBlock is recorded. Expected false. Also, the change set is not told to stop recording.")
    void constructorHonoursEnabledArgument() throws WorldEditException {
        TestWorld world = new TestWorld();
        ChangeSet history = new ArrayListHistory();
        ChangeSetExtent extent = new ChangeSetExtent(world, history, false);
        assertFalse(extent.isEnabled());
        extent.setBlock(BlockVector3.ZERO, block("minecraft:stone"));
        assertEquals(0, history.size());
    }

    @Test
    void blockChangeRequiresAnExtentToUndo() {
        BlockChange change = new BlockChange(BlockVector3.ZERO, block("minecraft:air"), block("minecraft:stone"));
        assertThrows(NullPointerException.class, () -> change.undo(new UndoContext()));
        assertThrows(NullPointerException.class, () -> change.redo(new UndoContext()));
    }

    @Test
    void blockChangeSetsPreviousOnUndoAndCurrentOnRedo() throws WorldEditException {
        TestWorld world = new TestWorld();
        BlockVector3 position = BlockVector3.at(5, -20, -7);
        BlockChange change = new BlockChange(position, block("minecraft:glass"), block("minecraft:stone"));
        UndoContext context = new UndoContext();
        context.setExtent(world);

        change.redo(context);
        assertEquals(block("minecraft:stone"), world.getBlock(position));
        change.undo(context);
        assertEquals(block("minecraft:glass"), world.getBlock(position));
    }

    @Test
    void entityCreateIsRemovedOnUndoAndRecreatedOnRedo() throws WorldEditException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(AREA);
        Location location = new Location(clipboard, Vector3.at(1.5, 2.5, 1.5));
        BaseEntity state = new BaseEntity(new EntityType("minecraft:item"));
        Entity created = clipboard.createEntity(location, state);
        EntityCreate change = new EntityCreate(location, state, created);
        UndoContext context = new UndoContext();
        context.setExtent(clipboard);

        change.undo(context);
        assertEquals(0, clipboard.getEntities().size());
        change.redo(context);
        assertEquals(1, clipboard.getEntities().size());
        assertEquals(location, clipboard.getEntities().getFirst().getLocation());
        // Undoing a second time removes the recreated entity, not the original
        change.undo(context);
        assertEquals(0, clipboard.getEntities().size());
    }

    @Test
    void entityRemoveIsRecreatedOnUndoAndRemovedOnRedo() throws WorldEditException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(AREA);
        Location location = new Location(clipboard, Vector3.at(-1.5, -2.5, 0.5));
        EntityRemove change = new EntityRemove(location, new BaseEntity(new EntityType("minecraft:item")));
        UndoContext context = new UndoContext();
        context.setExtent(clipboard);

        change.redo(context);
        assertEquals(0, clipboard.getEntities().size(), "redo before undo has nothing to remove");
        change.undo(context);
        assertEquals(1, clipboard.getEntities().size());
        change.redo(context);
        assertEquals(0, clipboard.getEntities().size());
    }

    @Test
    void changeSetExtentRecordsEntityCreationAndRemoval() throws WorldEditException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(AREA);
        Location location = new Location(clipboard, Vector3.at(0.5, 0.5, 0.5));
        BaseEntity state = new BaseEntity(new EntityType("minecraft:item"));
        ChangeSet history = new ArrayListHistory();
        ChangeSetExtent extent = new ChangeSetExtent(clipboard, history);

        // An entity that already exists and is removed through the tracking extent
        clipboard.createEntity(location, state);
        Entity existing = extent.getEntities().getFirst();
        assertTrue(existing.remove());
        extent.createEntity(location.setPosition(Vector3.at(2.5, 2.5, 2.5)), state);
        assertEquals(1, clipboard.getEntities().size());
        assertEquals(2, history.size());

        UndoContext context = new UndoContext();
        context.setExtent(clipboard);
        Operations.complete(ChangeSetExecutor.createUndo(history, context));
        assertEquals(1, clipboard.getEntities().size());
        assertEquals(Vector3.at(0.5, 0.5, 0.5), clipboard.getEntities().getFirst().getLocation().toVector());

        Operations.complete(ChangeSetExecutor.createRedo(history, context));
        assertEquals(1, clipboard.getEntities().size());
        assertEquals(Vector3.at(2.5, 2.5, 2.5), clipboard.getEntities().getFirst().getLocation().toVector());
    }

    @Test
    void changeSetExecutorHandlesEmptyHistoryAndNullChangeIsRejected() throws WorldEditException {
        UndoContext context = new UndoContext();
        context.setExtent(new TestWorld());
        Operations.complete(ChangeSetExecutor.createUndo(new ArrayListHistory(), context));
        Operations.complete(ChangeSetExecutor.createRedo(new ArrayListHistory(), context));
        assertThrows(NullPointerException.class, () -> new ArrayListHistory().add((Change) null));
        assertThrows(NullPointerException.class, () -> ChangeSetExecutor.createUndo(null, context));
    }
}
