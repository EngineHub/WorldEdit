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

package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.io.WorldEditResourceLoader;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Sets up a platform that reports a data version and the registries that schematics refer to, and offers
 * helpers for building and comparing clipboards.
 */
abstract class SchematicTestBase extends BaseWorldEditTest {

    /** The data version the mocked platform reports; golden files are older, so they would need DFU. */
    static final int LIVE_DATA_VERSION = 5023;

    static final List<String> BIOME_IDS = List.of(
        "minecraft:plains", "minecraft:desert", "minecraft:ocean", "minecraft:forest", "minecraft:swamp");
    static final List<String> ENTITY_IDS = List.of(
        "minecraft:armor_stand", "minecraft:item_frame", "minecraft:pig");

    @BeforeAll
    static void setUpSchematicRegistries() {
        TestRegistries.registerBlocks();
        for (String id : BIOME_IDS) {
            if (BiomeType.REGISTRY.get(id) == null) {
                BiomeType.REGISTRY.register(id, new BiomeType(id));
            }
        }
        for (String id : ENTITY_IDS) {
            if (EntityType.REGISTRY.get(id) == null) {
                EntityType.REGISTRY.register(id, new EntityType(id));
            }
        }
        when(MOCKED_PLATFORM.getDataVersion()).thenReturn(LIVE_DATA_VERSION);
        when(MOCKED_PLATFORM.id()).thenReturn("test:platform");
        when(MOCKED_PLATFORM.getPlatformName()).thenReturn("Test");
        when(MOCKED_PLATFORM.getPlatformVersion()).thenReturn("1.0");
        when(MOCKED_PLATFORM.getResourceLoader())
            .thenReturn(new WorldEditResourceLoader(WorldEdit.getInstance()));
    }

    @AfterAll
    static void tearDownSchematicRegistries() {
        when(MOCKED_PLATFORM.getDataVersion()).thenReturn(0);
        when(MOCKED_PLATFORM.id()).thenReturn(null);
        when(MOCKED_PLATFORM.getPlatformName()).thenReturn(null);
        when(MOCKED_PLATFORM.getPlatformVersion()).thenReturn(null);
        when(MOCKED_PLATFORM.getResourceLoader()).thenReturn(null);
        TestRegistries.unregisterBlocks();
        // Nothing else in the core tests registers biomes or entity types, and the registries cannot remove one entry
        BiomeType.REGISTRY.clear();
        EntityType.REGISTRY.clear();
    }

    static BlockType type(String id) {
        return Objects.requireNonNull(BlockType.REGISTRY.get(id), id);
    }

    static BiomeType biome(String id) {
        return Objects.requireNonNull(BiomeType.REGISTRY.get(id), id);
    }

    static byte[] write(ClipboardFormat format, Clipboard clipboard) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ClipboardWriter writer = format.getWriter(out)) {
            writer.write(clipboard);
        }
        return out.toByteArray();
    }

    static Clipboard read(ClipboardFormat format, byte[] data) throws IOException {
        try (ClipboardReader reader = format.getReader(new ByteArrayInputStream(data))) {
            return reader.read();
        }
    }

    static Clipboard readResource(ClipboardFormat format, String name) throws IOException {
        try (InputStream in = open(name); ClipboardReader reader = format.getReader(in)) {
            return reader.read();
        }
    }

    static InputStream open(String name) {
        InputStream in = SchematicTestBase.class.getResourceAsStream("/schematics/" + name);
        if (in == null) {
            throw new UncheckedIOException(new IOException("Missing test resource " + name));
        }
        return in;
    }

    /** Parses a block string the way the schematic readers do, so that omitted properties take their defaults. */
    static BlockState state(String input) {
        ParserContext context = new ParserContext();
        context.setRestricted(false);
        context.setTryLegacy(false);
        context.setPreferringWildcard(false);
        try {
            return WorldEdit.getInstance().getBlockFactory().parseFromInput(input, context).toImmutableState();
        } catch (InputParseException e) {
            throw new IllegalArgumentException(input, e);
        }
    }

    /** Reads the root compound of a gzipped NBT file, whatever its name. */
    static LinRootEntry readRoot(String resource) throws IOException {
        try (InputStream in = new GZIPInputStream(open(resource))) {
            return LinBinaryIO.readUsing(new DataInputStream(in), LinRootEntry::readFrom);
        }
    }

    /** Serialises a root compound as a gzipped NBT file. */
    static byte[] gzip(LinRootEntry root) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DataOutputStream stream = new DataOutputStream(new GZIPOutputStream(out))) {
            LinBinaryIO.write(stream, root);
        }
        return out.toByteArray();
    }

    /** Encodes values as the varints that Sponge schematics use for palette indices. */
    static byte[] varInts(int... values) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int value : values) {
            while ((value & ~0x7F) != 0) {
                out.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
            out.write(value);
        }
        return out.toByteArray();
    }

    static BlockArrayClipboard newClipboard(BlockVector3 min, BlockVector3 max, BlockVector3 origin) {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(new CuboidRegion(min, max));
        clipboard.setOrigin(origin);
        return clipboard;
    }

    static Entity addEntity(Clipboard clipboard, String id, double x, double y, double z, float yaw, float pitch,
                            LinCompoundTag nbt) {
        BaseEntity state = nbt == null
            ? new BaseEntity(new EntityType(id))
            : new BaseEntity(new EntityType(id), LazyReference.computed(nbt));
        return clipboard.createEntity(new Location(clipboard, x, y, z, yaw, pitch), state);
    }

    /** Removes the keys that readers and writers add or normalise, so NBT can be compared with the original. */
    static LinCompoundTag stripBookkeeping(LinCompoundTag tag, String... extraKeys) {
        if (tag == null) {
            return null;
        }
        LinCompoundTag.Builder builder = tag.toBuilder();
        for (String key : List.of("id", "Id", "x", "y", "z", "Pos", "Rotation")) {
            builder.remove(key);
        }
        for (String key : extraKeys) {
            builder.remove(key);
        }
        return builder.build();
    }

    /** Asserts that two clipboards agree on region, origin, blocks, block NBT, biomes and entities. */
    static void assertClipboardsEqual(Clipboard expected, Clipboard actual, boolean biomesPerColumn)
        throws WorldEditException {
        assertEquals(expected.getMinimumPoint(), actual.getMinimumPoint(), "minimum point");
        assertEquals(expected.getMaximumPoint(), actual.getMaximumPoint(), "maximum point");
        assertEquals(expected.getOrigin(), actual.getOrigin(), "origin");
        assertEquals(expected.getRegion().getVolume(), actual.getRegion().getVolume(), "volume");

        for (BlockVector3 pos : expected.getRegion()) {
            BaseBlock want = expected.getFullBlock(pos);
            BaseBlock got = actual.getFullBlock(pos);
            assertEquals(want.toImmutableState(), got.toImmutableState(), () -> "block state at " + pos);
            assertEquals(stripBookkeeping(want.getNbt()), stripBookkeeping(got.getNbt()), () -> "block NBT at " + pos);
        }

        assertEquals(expected.hasBiomes(), actual.hasBiomes(), "has biomes");
        if (expected.hasBiomes()) {
            for (BlockVector3 pos : expected.getRegion()) {
                BlockVector3 source = biomesPerColumn ? pos.withY(expected.getMinimumPoint().y()) : pos;
                assertEquals(expected.getBiome(source), actual.getBiome(pos), () -> "biome at " + pos);
            }
        }

        assertEntitiesEqual(expected, actual);
    }

    static void assertEntitiesEqual(Clipboard expected, Clipboard actual) {
        List<Entity> want = sortedEntities(expected);
        List<Entity> got = sortedEntities(actual);
        assertEquals(want.size(), got.size(), "entity count");
        for (int i = 0; i < want.size(); i++) {
            BaseEntity wantState = want.get(i).getState();
            BaseEntity gotState = got.get(i).getState();
            Location wantLoc = want.get(i).getLocation();
            Location gotLoc = got.get(i).getLocation();
            assertEquals(wantState.getType(), gotState.getType(), "entity type #" + i);
            assertEquals(wantLoc.toVector(), gotLoc.toVector(), "entity position #" + i);
            assertEquals(wantLoc.getYaw(), gotLoc.getYaw(), "entity yaw #" + i);
            assertEquals(wantLoc.getPitch(), gotLoc.getPitch(), "entity pitch #" + i);
            // An entity without data is written with a Rotation, so it comes back with an (empty) compound
            assertEquals(entityData(wantState), entityData(gotState), "entity NBT #" + i);
        }
    }

    private static LinCompoundTag entityData(BaseEntity entity) {
        LinCompoundTag nbt = entity.getNbt();
        return nbt == null ? LinCompoundTag.builder().build() : stripBookkeeping(nbt);
    }

    private static List<Entity> sortedEntities(Clipboard clipboard) {
        List<Entity> entities = new ArrayList<>(clipboard.getEntities());
        entities.sort(Comparator
            .comparingDouble((Entity e) -> e.getLocation().getX())
            .thenComparingDouble(e -> e.getLocation().getY())
            .thenComparingDouble(e -> e.getLocation().getZ())
            .thenComparing(e -> e.getState().getType().id()));
        return entities;
    }
}
