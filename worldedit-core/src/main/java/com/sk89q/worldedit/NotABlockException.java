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

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.item.ItemType;

/**
 * Raised when an item is used when a block was expected.
 */
public class NotABlockException extends WorldEditException {

    /**
     * Create a new instance.
     */
    public NotABlockException() {
        super(TranslatableComponent.of("worldedit.error.not-a-block"));
    }

    /**
     * Create a new instance.
     *
     * @param input the input that was used
     */
    @Deprecated
    public NotABlockException(String input) {
        super(TranslatableComponent.of("worldedit.error.not-a-block.item", TextComponent.of(input)));
    }

    /**
     * Create a new instance.
     *
     * @param input the input that was used
     */
    @Deprecated
    public NotABlockException(int input) {
        super(TranslatableComponent.of("worldedit.error.not-a-block.item", TextComponent.of(input)));
    }

    /**
     * Create a new instance.
     *
     * @param input the input that was used
     */
    public NotABlockException(ItemType input) {
        super(TranslatableComponent.of("worldedit.error.not-a-block.item", input.getRichName()));
    }
}
