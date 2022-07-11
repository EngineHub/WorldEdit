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
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.linbus.tree.LinTagType;

public class SkullBlockCompatibilityHandler implements NBTCompatibilityHandler {

    private static final Property<Direction> FACING_PROPERTY;

    static {
        Property<Direction> tempFacing;
        try {
            tempFacing = BlockTypes.SKELETON_WALL_SKULL.getProperty("facing");
        } catch (NullPointerException | IllegalArgumentException | ClassCastException e) {
            tempFacing = null;
        }
        FACING_PROPERTY = tempFacing;
    }

    @Override
    public BaseBlock updateNbt(BaseBlock block) {
        var blockType = block.getBlockType();
        boolean isWall = blockType == BlockTypes.SKELETON_WALL_SKULL;
        if (blockType != BlockTypes.SKELETON_SKULL && !isWall) {
            return block;
        }
        if (FACING_PROPERTY == null) {
            return block;
        }
        var tag = block.getNbt();
        if (tag == null) {
            return block;
        }
        var typeTag = tag.findTag("SkullType", LinTagType.byteTag());
        if (typeTag == null) {
            return block;
        }
        String skullType = convertSkullType(typeTag.valueAsByte(), isWall);
        if (skullType == null) {
            return block;
        }
        BlockType type = BlockTypes.get("minecraft:" + skullType);
        if (type == null) {
            return block;
        }
        BlockState state = type.getDefaultState();
        if (isWall) {
            Property<Direction> newProp = type.getProperty("facing");
            state = state.with(newProp, block.getState(FACING_PROPERTY));
        } else {
            var rotTag = tag.findTag("Rot", LinTagType.byteTag());
            if (rotTag != null) {
                Property<Integer> newProp = type.getProperty("rotation");
                state = state.with(newProp, (int) rotTag.valueAsByte());
            }
        }
        var newTag = tag.toBuilder()
            .remove("SkullType")
            .remove("Rot")
            .build();
        return state.toBaseBlock(newTag);
    }

    private String convertSkullType(byte oldType, boolean isWall) {
        record SkullData(String kind, String suffix) {
        }

        var skullData = switch (oldType) {
            case 0 -> new SkullData("skeleton", "skull");
            case 1 -> new SkullData("wither_skeleton", "skull");
            case 2 -> new SkullData("zombie", "head");
            case 3 -> new SkullData("player", "head");
            case 4 -> new SkullData("creeper", "head");
            case 5 -> new SkullData("dragon", "head");
            default -> null;
        };
        if (skullData == null) {
            return null;
        }
        return skullData.kind + (isWall ? "_wall" : "") + "_" + skullData.suffix;
    }
}
