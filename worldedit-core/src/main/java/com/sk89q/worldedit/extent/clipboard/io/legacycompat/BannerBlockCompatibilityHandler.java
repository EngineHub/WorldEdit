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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BannerBlockCompatibilityHandler implements NBTCompatibilityHandler {

    private static final DirectionalProperty FacingProperty;
    private static final IntegerProperty RotationProperty;

    static {
        DirectionalProperty tempFacing;
        IntegerProperty tempRotation;
        try {
            tempFacing = (DirectionalProperty) (Property<?>) BlockTypes.WHITE_WALL_BANNER.getProperty("facing");
            tempRotation = (IntegerProperty) (Property<?>) BlockTypes.WHITE_BANNER.getProperty("rotation");
        } catch (NullPointerException | IllegalArgumentException | ClassCastException e) {
            tempFacing = null;
            tempRotation = null;
        }
        FacingProperty = tempFacing;
        RotationProperty = tempRotation;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
        return block.getBlockType() == BlockTypes.WHITE_BANNER
                || block.getBlockType() == BlockTypes.WHITE_WALL_BANNER;
    }

    @Override
    public <B extends BlockStateHolder<B>> B updateNBT(B block, Map<String, Tag> values) {
        Tag typeTag = values.get("Base");
        if (typeTag instanceof IntTag) {
            boolean isWall = block.getBlockType() == BlockTypes.WHITE_WALL_BANNER;
            String bannerType = convertBannerType(((IntTag) typeTag).getValue(), isWall);
            if (bannerType != null) {
                BlockType type = BlockTypes.get("minecraft:" + bannerType);
                if (type != null) {
                    BlockState state = type.getDefaultState();

                    if (isWall) {
                        Property facingProp = type.getProperty("facing");
                        state = state.with(facingProp, block.getState(FacingProperty));
                    } else {
                        Property rotationProp = type.getProperty("rotation");
                        state = state.with(rotationProp, block.getState(RotationProperty));
                    }

                    values.remove("Base");

                    Tag patternsTag = values.get("Patterns");
                    if (patternsTag instanceof ListTag) {
                        List<Tag> tempList = new ArrayList<>();
                        for (Tag pattern : ((ListTag) patternsTag).getValue()) {
                            if (pattern instanceof CompoundTag) {
                                Map<String, Tag> patternMap = ((CompoundTag) pattern).getValue();
                                Tag colorTag = patternMap.get("Color");

                                CompoundTagBuilder builder = CompoundTagBuilder.create();
                                builder.putAll(patternMap);
                                if (colorTag instanceof IntTag) {
                                    builder.putInt("Color", 15 - ((IntTag) colorTag).getValue());
                                }
                                tempList.add(builder.build());
                            } else {
                                tempList.add(pattern);
                            }
                        }
                        values.put("Patterns", new ListTag(((ListTag) patternsTag).getType(), tempList));
                    }
                    return (B) state;
                }
            }
        }
        return block;
    }

    private static String convertBannerType(int oldType, boolean isWall) {
        String color;
        switch (oldType) {
            case 0:
                color = "black";
                break;
            case 1:
                color = "red";
                break;
            case 2:
                color = "green";
                break;
            case 3:
                color = "brown";
                break;
            case 4:
                color = "blue";
                break;
            case 5:
                color = "purple";
                break;
            case 6:
                color = "cyan";
                break;
            case 7:
                color = "light_gray";
                break;
            case 8:
                color = "gray";
                break;
            case 9:
                color = "pink";
                break;
            case 10:
                color = "lime";
                break;
            case 11:
                color = "yellow";
                break;
            case 12:
                color = "light_blue";
                break;
            case 13:
                color = "magenta";
                break;
            case 14:
                color = "orange";
                break;
            case 15:
                color = "white";
                break;
            default:
                return null;
        }
        return color + (isWall ? "_wall_banner" : "_banner");
    }
}
