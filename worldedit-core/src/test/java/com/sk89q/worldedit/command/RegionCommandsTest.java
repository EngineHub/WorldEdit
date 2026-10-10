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

package com.sk89q.worldedit.command;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegionCommandsTest extends BaseWorldEditTest {
    private static final BlockVector3 POSITION = BlockVector3.at(0, 64, 0);
    private static List<BlockType> previousBlocks;
    private DeferredWorld world;
    private LocalSession session;
    private Actor actor;
    private CuboidRegion region;
    private List<Component> errors;

    @BeforeAll
    static void registerBlocks() {
        previousBlocks = List.copyOf(BlockType.REGISTRY.values());
        TranslationManager translations = mock(TranslationManager.class);
        when(translations.convertText(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(MOCKED_PLATFORM.getTranslationManager()).thenReturn(translations);
        for (String id : List.of("minecraft:air", "minecraft:oak_wood", "minecraft:chest")) {
            if (BlockType.REGISTRY.get(id) == null) {
                BlockType.REGISTRY.register(id, new BlockType(id));
            }
        }
    }

    @AfterAll
    static void restoreBlocks() {
        BlockType.REGISTRY.clear();
        previousBlocks.forEach(block -> BlockType.REGISTRY.register(block.id(), block));
    }

    @BeforeEach
    @SuppressWarnings("deprecation")
    void setUp() throws WorldEditException {
        world = new DeferredWorld();
        region = new CuboidRegion(world, POSITION, POSITION);
        actor = mock(Actor.class);
        when(actor.hasPermission(anyString())).thenReturn(true);
        session = new LocalSession();
        session.setWorldOverride(world);
        session.setBlockChangeLimit(-1);
        session.setPlaceAtPos1(true);
        session.getRegionSelector(world).selectPrimary(POSITION, ActorSelectorLimits.forActor(actor));
        errors = new ArrayList<>();
        doAnswer(invocation -> errors.add(invocation.getArgument(0))).when(actor).printError(any(Component.class));
        world.blocks.setBlock(POSITION, BlockType.REGISTRY.get("minecraft:oak_wood").getDefaultState());
    }

    private void regenerate(boolean clipboard) throws WorldEditException {
        // Model the dispatcher's immediate disposal of its injected session.
        try (EditSession injected = session.createEditSession(actor)) {
            injected.enableStandardMode();
            new RegionCommands().regenerate(actor, world, session, injected, region, null, false, clipboard);
            session.remember(injected);
        }
    }

    private BlockArrayClipboard generated() throws WorldEditException {
        BlockArrayClipboard generated = new BlockArrayClipboard(region.clone());
        generated.setBlock(POSITION, BlockType.REGISTRY.get("minecraft:chest").getDefaultState());
        return generated;
    }

    @Test
    void waitsForGenerationWithoutReportingFailure() throws WorldEditException {
        regenerate(false);
        assertTrue(errors.isEmpty(), "A pending generation is not a failed regeneration");
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
    }

    @Test
    void appliesInANewSessionAndRemembersUndo() throws WorldEditException {
        regenerate(false);
        world.result.complete(generated());
        assertEquals(BlockType.REGISTRY.get("minecraft:chest"), world.getBlock(POSITION).getBlockType());
        try (EditSession undo = session.undo(null, actor)) {
            assertNotNull(undo, "The asynchronous edit must be in history");
        }
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
        assertTrue(errors.isEmpty());
    }

    @Test
    void replacesClipboardOnlyAfterSuccess() throws WorldEditException {
        Clipboard oldClipboard = generated();
        session.setClipboard(new ClipboardHolder(oldClipboard));
        regenerate(true);
        assertSame(oldClipboard, session.getClipboard().getClipboard());
        BlockArrayClipboard generated = generated();
        world.result.complete(generated);
        assertSame(generated, session.getClipboard().getClipboard());
        assertEquals(POSITION, generated.getOrigin());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
    }

    @Test
    void generationFailurePreservesClipboardAndWorld() throws WorldEditException {
        Clipboard oldClipboard = generated();
        session.setClipboard(new ClipboardHolder(oldClipboard));
        regenerate(true);
        world.result.completeExceptionally(new IllegalStateException("generation failed"));
        assertSame(oldClipboard, session.getClipboard().getClipboard());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
        assertFalse(errors.isEmpty());
    }

    @Test
    void respectsTheSessionMaskWithoutChangingIt() throws WorldEditException {
        var mask = Masks.negate(Masks.alwaysTrue());
        session.setMask(mask);
        regenerate(false);
        assertSame(mask, session.getMask());
        world.result.complete(generated());
        assertSame(mask, session.getMask());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
        assertNull(session.undo(null, actor), "A fully masked edit must not create an undo entry");
        assertTrue(errors.isEmpty());
    }

    @Test
    void regeneratesOnlyMatchingBlocksAndRemembersUndo() throws WorldEditException {
        BlockVector3 second = POSITION.add(1, 0, 0);
        region.setPos2(second);
        world.blocks.setBlock(second, BlockType.REGISTRY.get("minecraft:oak_wood").getDefaultState());
        BlockArrayClipboard snapshot = generated();
        snapshot.setBlock(second, BlockType.REGISTRY.get("minecraft:chest").getDefaultState());
        Mask mask = POSITION::equals;
        session.setMask(mask);
        regenerate(false);
        world.result.complete(snapshot);
        assertSame(mask, session.getMask());
        assertEquals(BlockType.REGISTRY.get("minecraft:chest"), world.getBlock(POSITION).getBlockType());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(second).getBlockType());
        assertNotNull(session.undo(null, actor));
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(second).getBlockType());
        assertTrue(errors.isEmpty());
    }

    @Test
    void keepsTheOriginalMaskWhenTheSessionMaskChanges() throws WorldEditException {
        session.setMask(Masks.negate(Masks.alwaysTrue()));
        regenerate(false);
        var replacement = Masks.alwaysTrue();
        session.setMask(replacement);
        world.result.complete(generated());
        assertSame(replacement, session.getMask());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
        assertTrue(errors.isEmpty());
    }

    @Test
    void regeneratesToClipboardWithoutApplyingTheSessionMask() throws WorldEditException {
        var mask = Masks.negate(Masks.alwaysTrue());
        session.setMask(mask);
        regenerate(true);
        world.result.complete(generated());
        assertSame(mask, session.getMask());
        assertEquals(BlockType.REGISTRY.get("minecraft:chest"),
            session.getClipboard().getClipboard().getBlock(POSITION).getBlockType());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
        assertTrue(errors.isEmpty());
    }

    @Test
    void keepsTheOriginalWorldWhenTheSessionOverrideChanges() throws WorldEditException {
        regenerate(false);
        DeferredWorld other = new DeferredWorld();
        other.blocks.setBlock(POSITION, BlockType.REGISTRY.get("minecraft:oak_wood").getDefaultState());
        session.setWorldOverride(other);
        world.result.complete(generated());
        assertEquals(BlockType.REGISTRY.get("minecraft:chest"), world.getBlock(POSITION).getBlockType());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), other.getBlock(POSITION).getBlockType());
    }

    @Test
    void keepsTheOriginalSelectionWhenTheSelectionChanges() throws WorldEditException {
        BlockArrayClipboard snapshot = generated();
        regenerate(false);
        region.setPos1(POSITION.add(1, 0, 0));
        region.setPos2(POSITION.add(1, 0, 0));
        world.result.complete(snapshot);
        assertEquals(BlockType.REGISTRY.get("minecraft:chest"), world.getBlock(POSITION).getBlockType());
        assertEquals(BlockType.REGISTRY.get("minecraft:air"), world.getBlock(POSITION.add(1, 0, 0)).getBlockType());
    }

    @Test
    void preservesGeneratedBlockEntityData() throws WorldEditException {
        BlockArrayClipboard snapshot = generated();
        LinCompoundTag tag = LinCompoundTag.builder().putString("id", "minecraft:chest")
            .putString("LootTable", "minecraft:chests/simple_dungeon").build();
        snapshot.setBlock(POSITION, BlockType.REGISTRY.get("minecraft:chest").getDefaultState().toBaseBlock(tag));
        regenerate(false);
        world.result.complete(snapshot);
        assertEquals(tag, world.getFullBlock(POSITION).getNbt());
        assertTrue(errors.isEmpty());
    }

    @Test
    void keepsPartialChangesUndoableWhenTheBlockLimitIsExceeded() throws WorldEditException {
        BlockVector3 second = POSITION.add(1, 0, 0);
        region.setPos2(second);
        world.blocks.setBlock(second, BlockType.REGISTRY.get("minecraft:oak_wood").getDefaultState());
        BlockArrayClipboard snapshot = generated();
        snapshot.setBlock(second, BlockType.REGISTRY.get("minecraft:chest").getDefaultState());
        session.setBlockChangeLimit(1);
        regenerate(false);
        world.result.complete(snapshot);
        assertFalse(errors.isEmpty());
        assertNotNull(session.undo(null, actor));
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(POSITION).getBlockType());
        assertEquals(BlockType.REGISTRY.get("minecraft:oak_wood"), world.getBlock(second).getBlockType());
    }

    private static final class DeferredWorld extends NullWorld {
        private final BlockArrayClipboard blocks = new BlockArrayClipboard(new CuboidRegion(POSITION, POSITION.add(1, 0, 0)));
        private final CompletableFuture<Clipboard> result = new CompletableFuture<>();

        @Override
        public boolean supportsAsyncRegeneration() {
            return true;
        }

        @Override
        public CompletionStage<Clipboard> regenerateAsync(Region region, RegenOptions options, Actor actor) {
            return result;
        }

        @Override
        public boolean regenerate(Region region, Extent extent, RegenOptions options) {
            return false;
        }

        @Override
        public BlockState getBlock(BlockVector3 position) {
            return blocks.getBlock(position);
        }

        @Override
        public BaseBlock getFullBlock(BlockVector3 position) {
            return blocks.getFullBlock(position);
        }

        @Override
        public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block,
                                                               SideEffectSet sideEffects) throws WorldEditException {
            return blocks.setBlock(position, block);
        }
    }
}
