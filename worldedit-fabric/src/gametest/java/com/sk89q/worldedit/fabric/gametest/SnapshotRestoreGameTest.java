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

package com.sk89q.worldedit.fabric.gametest;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.coremc.internal.NBTConverter;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.snapshot.experimental.Snapshot;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotInfo;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotRestore;
import com.sk89q.worldedit.world.storage.MissingChunkException;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.io.IOException;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Restores blocks from a snapshot made of a chunk exactly as this version of Minecraft saves it, since the
 * chunk format changes between versions (cf. the palette changes in 26.3).
 */
public class SnapshotRestoreGameTest {
    private static final int SIZE = 4;

    @GameTest
    public void restoreBringsBackSavedChunk(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        CuboidRegion region = region(helper, world);
        List<BlockState> saved = pattern();

        paint(world, region, saved);
        helper.assertValueEqual(blocks(world, region), saved, "pattern in the world before taking the snapshot");
        Snapshot snapshot = snapshotOf(helper, region);

        try {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                session.setBlocks(region, BlockTypes.GLASS.getDefaultState());
            }
            helper.assertFalse(blocks(world, region).equals(saved), "the region should have been changed after the snapshot was taken");

            SnapshotRestore restore;
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                restore = new SnapshotRestore(snapshot, session, region);
                restore.restore();
            }

            helper.assertTrue(restore.getMissingChunks().isEmpty(), "no chunks should be missing from the snapshot");
            helper.assertTrue(restore.getErrorChunks().isEmpty(), "no chunks should have failed to read: " + restore.getLastErrorMessage());
            helper.assertValueEqual(blocks(world, region), saved, "blocks after restoring");
        } finally {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                session.setBlocks(region, BlockTypes.AIR.getDefaultState());
            }
        }
        helper.succeed();
    }

    @GameTest
    public void restoreOnlyTouchesRequestedRegion(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        CuboidRegion region = region(helper, world);
        List<BlockState> saved = pattern();
        // Directly above the region, so it is in a chunk the snapshot has and a restore of whole chunks would reach it
        BlockVector3 outside = region.getMaximumPoint().add(0, 1, 0);
        BlockState originalOutside = world.getBlock(outside);

        paint(world, region, saved);
        Snapshot snapshot = snapshotOf(helper, region);

        try {
            world.setBlock(outside, BlockTypes.GLASS.getDefaultState());
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                session.setBlocks(region, BlockTypes.GLASS.getDefaultState());
                new SnapshotRestore(snapshot, session, region).restore();
            }
            helper.assertValueEqual(blocks(world, region), saved, "blocks in the restored region");
            helper.assertValueEqual(world.getBlock(outside), BlockTypes.GLASS.getDefaultState(), "block just outside the restored region");
        } finally {
            world.setBlock(outside, originalOutside);
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
                session.setBlocks(region, BlockTypes.AIR.getDefaultState());
            }
        }
        helper.succeed();
    }

    @GameTest
    public void restoreReportsChunksMissingFromSnapshot(GameTestHelper helper) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        CuboidRegion region = region(helper, world);

        SnapshotRestore restore;
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build();
             Snapshot empty = new TagSnapshot(Map.of())) {
            restore = new SnapshotRestore(empty, session, region);
            restore.restore();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        helper.assertFalse(restore.getMissingChunks().isEmpty(), "the chunk should be reported missing");
        helper.assertTrue(restore.hadTotalFailure(), "restoring from an empty snapshot should be a total failure");
        helper.succeed();
    }

    private static CuboidRegion region(GameTestHelper helper, World world) {
        BlockVector3 origin = FabricAdapter.get().adapt(helper.absolutePos(BlockPos.ZERO));
        return new CuboidRegion(world, origin, origin.add(SIZE - 1, SIZE - 1, SIZE - 1));
    }

    /**
     * Blocks with properties and without, ordered to match iteration over the region.
     */
    private static List<BlockState> pattern() {
        FabricAdapter adapter = FabricAdapter.get();
        List<BlockState> choices = List.of(
            BlockTypes.STONE.getDefaultState(),
            adapter.fromNativeBlockState(Blocks.OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.TOP)),
            BlockTypes.AIR.getDefaultState(),
            BlockTypes.GLOWSTONE.getDefaultState()
        );
        List<BlockState> pattern = new ArrayList<>();
        for (int i = 0; i < SIZE * SIZE * SIZE; i++) {
            pattern.add(choices.get((i + i / 5) % choices.size()));
        }
        return pattern;
    }

    private static void paint(World world, CuboidRegion region, List<BlockState> pattern) throws WorldEditException {
        int i = 0;
        for (BlockVector3 pos : region) {
            world.setBlock(pos, pattern.get(i++));
        }
    }

    private static List<BlockState> blocks(World world, CuboidRegion region) {
        List<BlockState> blocks = new ArrayList<>();
        for (BlockVector3 pos : region) {
            blocks.add(world.getBlock(pos));
        }
        return blocks;
    }

    /**
     * Saves the chunks the region touches, which is two or four of them if it happens to straddle a border.
     */
    private static Snapshot snapshotOf(GameTestHelper helper, CuboidRegion region) {
        Map<BlockVector2, LinCompoundTag> chunks = new HashMap<>();
        for (BlockVector2 pos : region.getChunks()) {
            ChunkAccess chunk = helper.getLevel().getChunk(pos.x(), pos.z());
            CompoundTag nativeTag = NbtUtils.addCurrentDataVersion(SerializableChunkData.copyOf(helper.getLevel(), chunk).write());
            chunks.put(pos, (LinCompoundTag) NBTConverter.fromNative(nativeTag));
        }
        return new TagSnapshot(chunks);
    }

    /**
     * A snapshot made of chunks as they would have been read from a region file.
     */
    private static final class TagSnapshot implements Snapshot {
        private final Map<BlockVector2, LinCompoundTag> chunks;

        private TagSnapshot(Map<BlockVector2, LinCompoundTag> chunks) {
            this.chunks = chunks;
        }

        @Override
        public SnapshotInfo getInfo() {
            return SnapshotInfo.create(URI.create("test:snapshot"), ZonedDateTime.now(ZoneOffset.UTC));
        }

        @Override
        public LinCompoundTag getChunkTag(BlockVector3 position) throws DataException {
            // Chunks are 2D here, as in the region files, so the Y is ignored
            LinCompoundTag tag = chunks.get(BlockVector2.at(position.x(), position.z()));
            if (tag == null) {
                throw new MissingChunkException();
            }
            return tag;
        }

        @Override
        public void close() {
        }
    }
}
