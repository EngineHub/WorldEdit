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

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.linbus.tree.LinTagType;

public class BedBlockCompatibilityHandler implements NBTCompatibilityHandler {

    private static final Property<Direction> FACING_PROPERTY;
    private static final Property<String> PART_PROPERTY;

    static {
        Property<Direction> tempFacing;
        Property<String> tempPart;
        try {
            tempFacing = BlockTypes.RED_BED.getProperty("facing");
            tempPart = BlockTypes.RED_BED.getProperty("part");
        } catch (NullPointerException | IllegalArgumentException | ClassCastException e) {
            tempFacing = null;
            tempPart = null;
        }
        FACING_PROPERTY = tempFacing;
        PART_PROPERTY = tempPart;
    }

    @Override
    public BaseBlock updateNbt(BaseBlock block) {
        if (block.getBlockType() != BlockTypes.RED_BED) {
            return block;
        }
        var tag = block.getNbt();
        if (tag == null) {
            return block;
        }
        var typeTag = tag.findTag("color", LinTagType.intTag());
        if (typeTag == null) {
            return block;
        }
        String bedType = convertBedType(typeTag.valueAsInt());
        if (bedType == null) {
            return block;
        }
        BlockType type = BlockTypes.get("minecraft:" + bedType);
        if (type == null) {
            return block;
        }
        BlockState state = type.getDefaultState();

        Property<Direction> facingProp = type.getProperty("facing");
        state = state.with(facingProp, block.getState(FACING_PROPERTY));

        Property<Boolean> occupiedProp = type.getProperty("occupied");
        state = state.with(occupiedProp, false);

        Property<String> partProp = type.getProperty("part");
        state = state.with(partProp, block.getState(PART_PROPERTY));

        var newTag = tag.toBuilder();
        newTag.remove("color");
        return state.toBaseBlock(LazyReference.computed(newTag.build()));
    }

    private String convertBedType(int oldType) {
        String color = switch (oldType) {
            case 0 -> "white";
            case 1 -> "orange";
            case 2 -> "magenta";
            case 3 -> "light_blue";
            case 4 -> "yellow";
            case 5 -> "lime";
            case 6 -> "pink";
            case 7 -> "gray";
            case 8 -> "light_gray";
            case 9 -> "cyan";
            case 10 -> "purple";
            case 11 -> "blue";
            case 12 -> "brown";
            case 13 -> "green";
            case 14 -> "red";
            case 15 -> "black";
            default -> null;
        };
        return color == null ? null : color + "_bed";
    }
}
