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


package com.sk89q.worldedit;

import com.sk89q.worldedit.EditSession.ReorderMode;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.util.test.TestWorld;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.sk89q.worldedit.util.test.ExtentTestSupport.block;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that an edit session's edits go through its own (buffering) extents rather than straight to the world.
 *
 * <p>Tree generators once read the unbuffered world and so could not see blocks the session had not flushed yet. They
 * now use the session's buffered reads; the generators themselves need a real world, so these tests only cover the
 * session's side of that contract ({@code getBlockWithBuffer} versus {@code getBlock}).</p>
 */
@SuppressWarnings("deprecation")
class EditSessionBufferingTest extends BaseWorldEditTest {

    private static final BlockVector3 POSITION = BlockVector3.at(2, -7, -3);

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    private static EditSession newSession(TestWorld world) {
        return WorldEdit.getInstance().newEditSession(world);
    }

    @Test
    void chunkBatchedEditsAreOnlyVisibleThroughTheSessionUntilFlushed() throws WorldEditException {
        TestWorld world = new TestWorld();
        world.setBlock(POSITION, block("minecraft:glass"));
        try (EditSession session = newSession(world)) {
            session.setBatchingChunks(true);
            assertTrue(session.setBlock(POSITION, block("minecraft:stone")));

            assertEquals(block("minecraft:stone"), session.getBlockWithBuffer(POSITION));
            assertEquals(block("minecraft:stone").toBaseBlock(), session.getFullBlockWithBuffer(POSITION));
            assertEquals(block("minecraft:glass"), session.getBlock(POSITION), "getBlock reads the real world");
            assertEquals(block("minecraft:glass"), world.getBlock(POSITION), "the world has not been written yet");

            session.flushSession();
            assertEquals(block("minecraft:stone"), world.getBlock(POSITION));
        }
    }

    @Test
    void multiStageReorderedEditsAreBufferedUntilFlushed() throws WorldEditException {
        TestWorld world = new TestWorld();
        try (EditSession session = newSession(world)) {
            session.setReorderMode(ReorderMode.MULTI_STAGE);
            session.setBlock(POSITION, block("minecraft:torch"));
            session.setBlock(POSITION.add(0, -1, 0), block("minecraft:stone"));

            assertEquals(block("minecraft:torch"), session.getBlockWithBuffer(POSITION));
            assertEquals(block("minecraft:air"), world.getBlock(POSITION));
            assertEquals(0, world.getBlocks().size(), "nothing reached the world before the flush");
        }
        assertEquals(block("minecraft:torch"), world.getBlock(POSITION), "closing the session flushes it");
        assertEquals(block("minecraft:stone"), world.getBlock(POSITION.add(0, -1, 0)));
    }

    @Test
    void disablingBufferingFlushesAndWritesThroughAfterwards() throws WorldEditException {
        TestWorld world = new TestWorld();
        try (EditSession session = newSession(world)) {
            session.setBatchingChunks(true);
            session.setBlock(POSITION, block("minecraft:stone"));
            assertTrue(session.isBufferingEnabled());

            session.disableBuffering();

            assertEquals(block("minecraft:stone"), world.getBlock(POSITION), "pending edits are flushed");
            assertFalse(session.isBatchingChunks());
            session.setBlock(POSITION.add(1, 0, 0), block("minecraft:glass"));
        }
        assertEquals(block("minecraft:glass"), world.getBlock(POSITION.add(1, 0, 0)));
    }

    @ParameterizedTest
    @EnumSource(ReorderMode.class)
    void eachReorderModeFlushesTheExpectedBlocksAndCanBeUndone(ReorderMode mode) throws WorldEditException {
        TestWorld world = new TestWorld();
        BlockVector3 stonePosition = BlockVector3.at(0, -1, 0);
        BlockVector3 torchPosition = BlockVector3.at(0, 0, 0);
        world.setBlock(stonePosition.add(5, 0, 0), block("minecraft:glass"));
        BaseBlock untouched = world.getFullBlock(stonePosition.add(5, 0, 0));
        EditSession session = newSession(world);
        try (session) {
            session.setReorderMode(mode);
            session.setBlock(torchPosition, block("minecraft:torch"));
            session.setBlock(stonePosition, block("minecraft:stone"));
            session.setBlock(stonePosition.add(5, 0, 0), block("minecraft:air"));
        }

        assertEquals(block("minecraft:torch"), world.getBlock(torchPosition));
        assertEquals(block("minecraft:stone"), world.getBlock(stonePosition));
        assertEquals(block("minecraft:air"), world.getBlock(stonePosition.add(5, 0, 0)));

        try (EditSession undoSession = newSession(world)) {
            session.undo(undoSession);
        }
        assertEquals(block("minecraft:air"), world.getBlock(torchPosition));
        assertEquals(block("minecraft:air"), world.getBlock(stonePosition));
        assertEquals(untouched, world.getFullBlock(stonePosition.add(5, 0, 0)));
        assertEquals(1, world.getBlocks().size());
    }

    @Test
    void historyIsRecordedForBufferedEditsAndRedoRestoresThem() throws WorldEditException {
        TestWorld world = new TestWorld();
        EditSession session = newSession(world);
        try (session) {
            session.setBatchingChunks(true);
            session.setBlock(POSITION, block("minecraft:stone"));
            session.setBlock(POSITION, block("minecraft:oak_planks"));
        }
        // Both edits hit the same buffered position, so only the net change is recorded
        assertEquals(1, session.getBlockChangeCount());

        try (EditSession undoSession = newSession(world)) {
            session.undo(undoSession);
        }
        assertEquals(block("minecraft:air"), world.getBlock(POSITION));
        try (EditSession redoSession = newSession(world)) {
            session.redo(redoSession);
        }
        assertEquals(block("minecraft:oak_planks"), world.getBlock(POSITION));
    }
}
