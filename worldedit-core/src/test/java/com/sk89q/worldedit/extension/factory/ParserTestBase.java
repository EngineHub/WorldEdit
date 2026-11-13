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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.world.item.ItemType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Shared setup for tests of the block, item, mask and pattern parsers.
 */
abstract class ParserTestBase extends BaseWorldEditTest {

    @BeforeAll
    static void registerTypes() {
        TestRegistries.registerBlocks();
        for (String id : new String[] {"minecraft:stone", "minecraft:diamond_sword", "minecraft:oak_planks"}) {
            if (ItemType.REGISTRY.get(id) == null) {
                ItemType.REGISTRY.register(id, new ItemType(id));
            }
        }
    }

    @AfterAll
    static void unregisterTypes() {
        TestRegistries.unregisterBlocks();
        ItemType.REGISTRY.clear();
    }

    /**
     * Creates a context that does not need permissions and does not try legacy IDs.
     */
    static ParserContext context() {
        ParserContext context = new ParserContext();
        context.setRestricted(false);
        context.setTryLegacy(false);
        context.setExtent(newExtent());
        return context;
    }

    /**
     * Like {@link #context()} but with an actor, which makes unknown properties an error instead of being ignored.
     */
    static ParserContext contextWithActor() {
        ParserContext context = context();
        context.setActor(mock(Actor.class));
        return context;
    }

    static Extent newExtent() {
        return new BlockArrayClipboard(new CuboidRegion(BlockVector3.at(-8, -8, -8), BlockVector3.at(8, 8, 8)));
    }

    /**
     * Asserts that parsing failed with the given exception type and translation key.
     */
    static <T extends InputParseException> T assertParseError(
        Class<T> type, String expectedKey, ParseAction action
    ) {
        T exception = assertThrows(type, action::run);
        Component message = exception.getRichMessage();
        TranslatableComponent translatable = assertInstanceOf(TranslatableComponent.class, message);
        assertEquals(expectedKey, translatable.key());
        return exception;
    }

    @FunctionalInterface
    interface ParseAction {
        void run() throws Exception;
    }
}
