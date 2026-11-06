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

import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.test.ResourceLockKeys;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.BundledRegistries;
import com.sk89q.worldedit.world.registry.ItemMaterial;
import com.sk89q.worldedit.world.registry.ItemRegistry;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ResourceLock(ResourceLockKeys.WORLDEDIT_PLATFORM)
public abstract class BaseWorldEditTest {
    protected static final Platform MOCKED_PLATFORM = mock(Platform.class);

    @BeforeAll
    static void setUpPlatform() {
        when(MOCKED_PLATFORM.getRegistries()).thenReturn(new BundledRegistries() {
            @Override
            public BlockRegistry getBlockRegistry() {
                return new BlockRegistry() {
                    @Override
                    public Component getRichName(BlockType blockType) {
                        return TextComponent.of(blockType.id());
                    }

                    @Override
                    public @Nullable BlockMaterial getMaterial(BlockType blockType) {
                        return null;
                    }

                    @Override
                    public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
                        return Map.of();
                    }

                    @Override
                    public OptionalInt getInternalBlockStateId(BlockState state) {
                        return OptionalInt.empty();
                    }
                };
            }

            @Override
            public ItemRegistry getItemRegistry() {
                return new ItemRegistry() {
                    @Override
                    public Component getRichName(ItemType itemType) {
                        return TextComponent.of(itemType.id());
                    }

                    @SuppressWarnings("removal")
                    @Override
                    public @Nullable ItemMaterial getMaterial(ItemType itemType) {
                        return null;
                    }
                };
            }
        });
        when(MOCKED_PLATFORM.getCapabilities()).thenReturn(
                Stream.of(Capability.values())
                        .collect(Collectors.toMap(Function.identity(), _ -> Preference.NORMAL))
        );
        when(MOCKED_PLATFORM.getConfiguration()).thenReturn(new LocalConfiguration() {
            @Override
            public void load() {
            }
        });
        WorldEdit.getInstance().getPlatformManager().register(MOCKED_PLATFORM);
        WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());
        assertTrue(WorldEdit.getInstance().getPlatformManager().isInitialized(), "Platform is not initialized");
    }

    @AfterAll
    static void tearDown() {
        WorldEdit.getInstance().getPlatformManager().unregister(MOCKED_PLATFORM);
    }
}
