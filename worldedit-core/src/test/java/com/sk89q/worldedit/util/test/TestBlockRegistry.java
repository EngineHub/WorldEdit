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

package com.sk89q.worldedit.util.test;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BlockRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A {@link BlockRegistry} backed by {@code test-blocks.json}, giving core tests blocks that have real
 * properties without depending on a platform.
 *
 * <p>The file uses the same layout as the data file read by worldedit-cli (without the fields tests do not
 * need, such as default states), and properties are built the same way as in {@code CLIBlockRegistry}.
 * Blocks that are not in the file have no properties. Materials and internal state IDs are not provided.</p>
 */
public class TestBlockRegistry implements BlockRegistry {

    private static final String RESOURCE = "/test-blocks.json";

    private record DataFile(Map<String, BlockManifest> blocks) {
    }

    private record BlockManifest(Map<String, BlockProperty> properties) {
    }

    private record BlockProperty(List<String> values, String type) {
    }

    private record PropertyKey(String name, BlockProperty property) {
    }

    private static final DataFile DATA = load();

    // Real platforms share one Property per vanilla property (see CoreMcTransmogrifier's propertyCache)
    private static final Map<PropertyKey, Property<?>> PROPERTIES = new ConcurrentHashMap<>();

    private final Map<String, BlockMaterial> materials = new ConcurrentHashMap<>();

    private static DataFile load() {
        try (InputStream in = TestBlockRegistry.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource " + RESOURCE);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, DataFile.class);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets the IDs of every block described by the test data.
     */
    public static List<String> blockIds() {
        return List.copyOf(DATA.blocks().keySet());
    }

    private static Property<?> createProperty(String type, String key, List<String> values) {
        return switch (type) {
            case "int" -> new IntegerProperty(key, values.stream().map(Integer::parseInt).toList());
            case "bool" -> new BooleanProperty(key, values.stream().map(Boolean::parseBoolean).toList());
            case "enum" -> new EnumProperty(key, values);
            case "direction" ->
                new DirectionalProperty(key, values.stream().map(String::toUpperCase).map(Direction::valueOf).toList());
            default -> throw new IllegalArgumentException("Unknown property type " + type);
        };
    }

    @Override
    public Component getRichName(BlockType blockType) {
        return TextComponent.of(blockType.id());
    }

    @Nullable
    @Override
    public BlockMaterial getMaterial(BlockState blockState) {
        return materials.computeIfAbsent(blockState.getBlockType().id(), id -> {
            // Only the air/solid distinction is modelled, which is what existence and solidity masks rely on
            boolean air = id.equals("minecraft:air");
            BlockMaterial material = mock(BlockMaterial.class);
            when(material.isAir()).thenReturn(air);
            when(material.isSolid()).thenReturn(!air);
            return material;
        });
    }

    @Override
    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
        BlockManifest manifest = DATA.blocks().get(blockType.id());
        if (manifest == null || manifest.properties() == null) {
            return Collections.emptyMap();
        }
        ImmutableMap.Builder<String, Property<?>> properties = ImmutableMap.builder();
        manifest.properties().forEach((key, value) ->
            properties.put(key, PROPERTIES.computeIfAbsent(new PropertyKey(key, value),
                _ -> createProperty(value.type(), key, value.values()))));
        return properties.build();
    }

    @Override
    public OptionalInt getInternalBlockStateId(BlockState state) {
        return OptionalInt.empty();
    }
}
