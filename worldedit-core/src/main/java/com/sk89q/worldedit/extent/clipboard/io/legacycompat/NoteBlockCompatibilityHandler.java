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

package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.linbus.tree.LinTagType;

public class NoteBlockCompatibilityHandler implements NBTCompatibilityHandler {
    private static final IntegerProperty NOTE_PROPERTY;

    static {
        IntegerProperty temp;
        try {
            temp = (IntegerProperty) (Property<?>) BlockTypes.NOTE_BLOCK.getProperty("note");
        } catch (NullPointerException | IllegalArgumentException | ClassCastException e) {
            temp = null;
        }
        NOTE_PROPERTY = temp;
    }

    @Override
    public BaseBlock updateNbt(BaseBlock block) {
        if (NOTE_PROPERTY == null || block.getBlockType() != BlockTypes.NOTE_BLOCK) {
            return block;
        }
        var tag = block.getNbt();
        if (tag == null) {
            return block;
        }
        // note that instrument was not stored (in state or nbt) previously.
        // it will be updated to the block below when it gets set into the world for the first time
        var noteTag = tag.findTag("note", LinTagType.byteTag());
        if (noteTag == null) {
            return block;
        }
        return block.with(NOTE_PROPERTY, (int) noteTag.valueAsByte()).toBaseBlock();
    }
}
