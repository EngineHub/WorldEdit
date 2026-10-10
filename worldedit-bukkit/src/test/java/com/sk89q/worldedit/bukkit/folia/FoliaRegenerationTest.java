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

package com.sk89q.worldedit.bukkit.folia;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.BukkitRegeneration;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.world.RegenOptions;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FoliaRegenerationTest {
    @Test
    void acceptsAnEntirelyOwnedSelection() {
        var region = new CuboidRegion(BlockVector3.at(-17, 0, -17), BlockVector3.at(31, 255, 31));
        Set<BlockVector2> checked = new HashSet<>();
        assertTrue(FoliaRegeneration.isOwned(region, chunk -> {
            checked.add(chunk);
            return true;
        }));
        assertEquals(region.getChunks(), checked);
    }

    @Test
    void rejectsAnUnownedInteriorChunkEvenWhenCornersAreOwned() {
        var region = new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(47, 255, 47));
        assertFalse(FoliaRegeneration.isOwned(region,
            chunk -> !chunk.equals(BlockVector2.at(1, 1))));
    }

    @Test
    void revalidationRejectsASelectionThatLostOwnership() {
        var region = new CuboidRegion(BlockVector3.at(-1, 0, 0), BlockVector3.at(0, 0, 0));
        Set<BlockVector2> owned = new HashSet<>(region.getChunks());
        assertTrue(FoliaRegeneration.isOwned(region, owned::contains));
        owned.remove(BlockVector2.at(-1, 0));
        assertFalse(FoliaRegeneration.isOwned(region, owned::contains));
    }

    @Test
    void closesGenerationBeforeReturningSnapshotToTheCaller() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.start();
            harness.global.remove().run();
            assertFalse(result.isDone());
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            verify(harness.generation).close();
            assertFalse(result.isDone());
            harness.caller.remove().run();
            assertSame(harness.snapshot, result.join());
        }
    }

    @Test
    void rejectsLostOwnershipAfterGenerationWithoutReturningASnapshot() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.start();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.owned = false;
            harness.caller.remove().run();
            assertTrue(result.isCompletedExceptionally());
            verify(harness.generation).close();
        }
    }

    @Test
    void timeoutClosesTheWorldAndIgnoresLateCompletion() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.start();
            harness.global.remove().run();
            harness.timeout.run();
            verify(harness.generation).close();
            harness.caller.remove().run();
            assertTrue(result.isCompletedExceptionally());
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            verify(harness.generation).close();
        }
    }

    @Test
    void shutdownDisposesActiveGeneration() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.start();
            harness.global.remove().run();
            harness.service.close();
            assertTrue(result.isCompletedExceptionally());
            verify(harness.generation).close();
        }
    }

    @Test
    void shutdownCancelsAResultAlreadyWaitingForItsCaller() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.start();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.service.close();
            assertTrue(result.isCompletedExceptionally());
            verify(harness.generation).close();
        }
    }

    @Test
    void returnsPlayerResultsThroughTheEntityScheduler() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.startAsPlayer();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            assertFalse(result.isDone());
            harness.caller.remove().run();
            assertEquals(List.of(harness.snapshot), harness.applied);
            assertFalse(result.isDone(), "Session completion must wait for the player's scheduler");
            harness.entity.remove().run();
            assertSame(harness.snapshot, result.join());
            verify(harness.generation).close();
        }
    }

    @Test
    void appliesInTheOriginalPos1RegionAfterThePlayerChangesWorlds() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.startAsPlayer();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            when(harness.player.getWorld()).thenReturn(mock(org.bukkit.World.class));
            harness.caller.remove().run();
            assertEquals(List.of(harness.snapshot), harness.applied);
            verify(harness.regionScheduler).execute(eq(harness.plugin), eq(harness.world), eq(1), eq(-2), any());
            assertFalse(result.isDone());
            harness.entity.remove().run();
            assertSame(harness.snapshot, result.join());
            verify(harness.player, never()).getWorld();
            verify(harness.generation).close();
        }
    }

    @Test
    void rejectsPlayersWhoLostOwnership() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.startAsPlayer();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.owned = false;
            harness.caller.remove().run();
            assertTrue(harness.applied.isEmpty());
            harness.entity.remove().run();
            assertTrue(result.isCompletedExceptionally());
            verify(harness.generation).close();
        }
    }

    @Test
    void retirementCancelsTheResultWithoutReopeningTheWorld() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.startAsPlayer();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.caller.remove().run();
            harness.retired.run();
            assertTrue(result.isCompletedExceptionally());
            verify(harness.generation).close();
        }
    }

    @Test
    void rejectedEntitySchedulingCancelsTheResult() throws Exception {
        try (Harness harness = new Harness()) {
            harness.acceptsScheduling = false;
            var result = harness.startAsPlayer();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.caller.remove().run();
            assertTrue(result.isCompletedExceptionally());
            assertTrue(harness.caller.isEmpty());
            verify(harness.generation).close();
        }
    }

    @Test
    void shutdownDoesNotCompleteTheResultWhileApplicationIsRunning() throws Exception {
        try (Harness harness = new Harness()) {
            AtomicReference<CompletableFuture<Clipboard>> result = new AtomicReference<>();
            harness.output = snapshot -> {
                harness.service.close();
                assertFalse(result.get().isDone());
                harness.applied.add(snapshot);
            };
            result.set(harness.start());
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.caller.remove().run();
            assertTrue(result.get().isCompletedExceptionally());
            assertEquals(List.of(harness.snapshot), harness.applied);
        }
    }

    @Test
    void shutdownBeforeRegionApplicationDoesNotApplyTheSnapshot() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.start();
            harness.global.remove().run();
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.service.close();
            harness.caller.remove().run();
            assertTrue(result.isCompletedExceptionally());
            assertTrue(harness.applied.isEmpty());
        }
    }

    @Test
    void rejectsSeedOverridesWhenTheAdapterCannotChangeSeeds() throws Exception {
        try (Harness harness = new Harness()) {
            var result = harness.start(RegenOptions.builder().seed(42L).build());
            assertTrue(result.isCompletedExceptionally());
            assertTrue(harness.global.isEmpty());
            verify(harness.generation, never()).close();
        }
    }

    @Test
    void forwardsSeedOverridesToAdaptersThatSupportThem() throws Exception {
        try (Harness harness = new Harness()) {
            when(harness.adapter.supportsRegenerationSeedOverride()).thenReturn(true);
            var options = RegenOptions.builder().seed(42L).build();
            var result = harness.start(options);
            harness.global.remove().run();
            verify(harness.adapter).beginRegeneration(eq(harness.world), any(), eq(options));
            harness.generated.complete(harness.snapshot);
            harness.global.remove().run();
            harness.caller.remove().run();
            assertSame(harness.snapshot, result.join());
            verify(harness.generation).close();
        }
    }

    private static final class Harness implements AutoCloseable {
        private final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        private final MockedStatic<WorldEditText> text =
            mockStatic(WorldEditText.class);
        private final Queue<Runnable> global = new ArrayDeque<>();
        private final Queue<Runnable> caller = new ArrayDeque<>();
        private final Queue<Runnable> entity = new ArrayDeque<>();
        private final org.bukkit.World world = mock(org.bukkit.World.class);
        private final WorldEditPlugin plugin = mock(WorldEditPlugin.class);
        private final Actor actor = mock(Actor.class);
        private final BukkitImplAdapter adapter = mock(BukkitImplAdapter.class);
        private final RegionScheduler regionScheduler = mock(RegionScheduler.class);
        private final List<Clipboard> applied = new ArrayList<>();
        private Consumer<Clipboard> output = applied::add;
        private org.bukkit.entity.Player player;
        private Runnable retired;
        private boolean acceptsScheduling = true;
        private final Clipboard snapshot = mock(Clipboard.class);
        private final BukkitRegeneration<?> generation = mock(BukkitRegeneration.class);
        private final CompletableFuture<Clipboard> generated = new CompletableFuture<>();
        private final FoliaRegeneration service;
        private Runnable timeout;
        private boolean owned = true;

        private Harness() {
            when(generation.result()).thenReturn(generated);
            var globalScheduler = mock(GlobalRegionScheduler.class);
            bukkit.when(Bukkit::getGlobalRegionScheduler).thenReturn(globalScheduler);
            bukkit.when(Bukkit::getRegionScheduler).thenReturn(regionScheduler);
            bukkit.when(() -> Bukkit.isOwnedByCurrentRegion(eq(world),
                anyInt(), anyInt())).thenAnswer(_ -> owned);
            text.when(() -> WorldEditText.reduceToText(
                any(), any())).thenReturn("regeneration error");
            doAnswer(invocation -> {
                global.add(invocation.getArgument(1));
                return null;
            }).when(globalScheduler).execute(eq(plugin), any());
            doAnswer(invocation -> {
                caller.add(invocation.getArgument(4));
                return null;
            }).when(regionScheduler).execute(eq(plugin), eq(world),
                anyInt(), anyInt(), any());
            when(globalScheduler.runDelayed(eq(plugin),
                any(), anyLong())).thenAnswer(invocation -> {
                    var task = mock(ScheduledTask.class);
                    Consumer<ScheduledTask> callback =
                        invocation.getArgument(1);
                    timeout = () -> callback.accept(task);
                    return task;
                });
            service = new FoliaRegeneration(plugin);
        }

        private CompletableFuture<Clipboard> start() throws Exception {
            return start(RegenOptions.builder().build());
        }

        private CompletableFuture<Clipboard> start(RegenOptions options) throws Exception {
            doReturn(generation).when(adapter).beginRegeneration(eq(world), any(), any());
            BlockVector3 pos1 = BlockVector3.at(31, 64, -17);
            return service.regenerate(world, new CuboidRegion(pos1, BlockVector3.at(0, 64, -1)),
                options, pos1, actor, output, adapter).toCompletableFuture();
        }

        private CompletableFuture<Clipboard> startAsPlayer() throws Exception {
            player = mock(org.bukkit.entity.Player.class);
            var scheduler = mock(EntityScheduler.class);
            var id = UUID.randomUUID();
            when(actor.isPlayer()).thenReturn(true);
            when(actor.getUniqueId()).thenReturn(id);
            bukkit.when(() -> Bukkit.getPlayer(id)).thenReturn(player);
            when(player.getWorld()).thenReturn(world);
            when(player.getScheduler()).thenReturn(scheduler);
            when(scheduler.execute(eq(plugin), any(), any(), anyLong())).thenAnswer(invocation -> {
                retired = invocation.getArgument(2);
                if (acceptsScheduling) {
                    entity.add(invocation.getArgument(1));
                }
                return acceptsScheduling;
            });
            return start();
        }

        @Override
        public void close() {
            service.close();
            text.close();
            bukkit.close();
        }
    }
}
