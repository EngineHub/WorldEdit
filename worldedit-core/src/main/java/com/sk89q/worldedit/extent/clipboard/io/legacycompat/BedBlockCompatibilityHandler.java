/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Map;

public class BedBlockCompatibilityHandler implements NBTCompatibilityHandler {

    private static final DirectionalProperty FacingProperty;
    private static final EnumProperty PartProperty;

    static {
        DirectionalProperty tempFacing;
        EnumProperty tempPart;
        try {
            tempFacing = (DirectionalProperty) (Property<?>) BlockTypes.RED_BED.getProperty("facing");
            tempPart = (EnumProperty) (Property<?>) BlockTypes.RED_BED.getProperty("part");
        } catch (NullPointerException | IllegalArgumentException | ClassCastException e) {
            tempFacing = null;
            tempPart = null;
        }
        FacingProperty = tempFacing;
        PartProperty = tempPart;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
        return block.getBlockType() == BlockTypes.RED_BED;
    }

    @Override
    public <B extends BlockStateHolder<B>> B updateNBT(B block, Map<String, Tag> values) {
        Tag typeTag = values.get("color");
        if (typeTag instanceof IntTag) {
            String bedType = convertBedType(((IntTag) typeTag).getValue());
            if (bedType != null) {
                BlockType type = BlockTypes.get("minecraft:" + bedType);
                if (type != null) {
                    BlockState state = type.getDefaultState();

                    Property facingProp = type.getProperty("facing");
                    state = state.with(facingProp, block.getState(FacingProperty));

                    Property occupiedProp = type.getProperty("occupied");
                    state = state.with(occupiedProp, false);

                    Property partProp = type.getProperty("part");
                    state = state.with(partProp, block.getState(PartProperty));

                    values.remove("color");
                    return (B) state;
                }
            }
        }
        return block;
    }

    private String convertBedType(int oldType) {
        String color;
        switch (oldType) {
            case 0:
                color = "white";
                break;
            case 1:
                color = "orange";
                break;
            case 2:
                color = "magenta";
                break;
            case 3:
                color = "light_blue";
                break;
            case 4:
                color = "yellow";
                break;
            case 5:
                color = "lime";
                break;
            case 6:
                color = "pink";
                break;
            case 7:
                color = "gray";
                break;
            case 8:
                color = "light_gray";
                break;
            case 9:
                color = "cyan";
                break;
            case 10:
                color = "purple";
                break;
            case 11:
                color = "blue";
                break;
            case 12:
                color = "brown";
                break;
            case 13:
                color = "green";
                break;
            case 14:
                color = "red";
                break;
            case 15:
                color = "black";
                break;
            default:
                return null;
        }
        return color + "_bed";
    }
}
