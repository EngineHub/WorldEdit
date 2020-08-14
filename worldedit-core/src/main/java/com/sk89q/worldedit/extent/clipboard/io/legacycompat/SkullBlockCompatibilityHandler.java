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

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Map;

public class SkullBlockCompatibilityHandler implements NBTCompatibilityHandler {

    private static final Property<Direction> FacingProperty;

    static {
        Property<Direction>  tempFacing;
        try {
            tempFacing = BlockTypes.SKELETON_WALL_SKULL.getProperty("facing");
        } catch (NullPointerException | IllegalArgumentException | ClassCastException e) {
            tempFacing = null;
        }
        FacingProperty = tempFacing;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
        return block.getBlockType() == BlockTypes.SKELETON_SKULL
                || block.getBlockType() == BlockTypes.SKELETON_WALL_SKULL;
    }

    @Override
    public <B extends BlockStateHolder<B>> BlockStateHolder<?> updateNBT(B block, Map<String, Tag> values) {
        boolean isWall = block.getBlockType() == BlockTypes.SKELETON_WALL_SKULL;
        Tag typeTag = values.get("SkullType");
        if (typeTag instanceof ByteTag) {
            String skullType = convertSkullType(((ByteTag) typeTag).getValue(), isWall);
            if (skullType != null) {
                BlockType type = BlockTypes.get("minecraft:" + skullType);
                if (type != null) {
                    BlockState state = type.getDefaultState();
                    if (isWall) {
                        Property<Direction> newProp = type.getProperty("facing");
                        state = state.with(newProp, block.getState(FacingProperty));
                    } else {
                        Tag rotTag = values.get("Rot");
                        if (rotTag instanceof ByteTag) {
                            Property<Integer> newProp = type.getProperty("rotation");
                            state = state.with(newProp, (int) ((ByteTag) rotTag).getValue());
                        }
                    }
                    values.remove("SkullType");
                    values.remove("Rot");
                    return state;
                }
            }
        }
        return block;
    }

    private String convertSkullType(Byte oldType, boolean isWall) {
        switch (oldType) {
            case 0:
                return isWall ? "skeleton_wall_skull" : "skeleton_skull";
            case 1:
                return isWall ? "wither_skeleton_wall_skull" : "wither_skeleton_skull";
            case 2:
                return isWall ? "zombie_wall_head" : "zombie_head";
            case 3:
                return isWall ? "player_wall_head" : "player_head";
            case 4:
                return isWall ? "creeper_wall_head" : "creeper_head";
            case 5:
                return isWall ? "dragon_wall_head" : "dragon_head";
            default:
                return null;
        }
    }
}
