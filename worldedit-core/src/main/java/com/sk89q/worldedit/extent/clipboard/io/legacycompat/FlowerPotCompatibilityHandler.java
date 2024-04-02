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

import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.enginehub.linbus.tree.LinTagType;

public class FlowerPotCompatibilityHandler implements NBTCompatibilityHandler {
    @Override
    public BaseBlock updateNbt(BaseBlock block) {
        if (block.getBlockType() != BlockTypes.FLOWER_POT) {
            return block;
        }
        var tag = block.getNbt();
        if (tag == null) {
            return block;
        }
        var item = tag.findTag("Item", LinTagType.stringTag());
        if (item == null) {
            return block;
        }
        String id = item.value();
        if (id.isEmpty()) {
            return BlockTypes.FLOWER_POT.getDefaultState().toBaseBlock();
        }
        int data = 0;
        var dataTag = tag.findTag("Data", LinTagType.intTag());
        if (dataTag != null) {
            data = dataTag.valueAsInt();
        }
        BlockState newState = convertLegacyBlockType(id, data);
        return newState != null ? newState.toBaseBlock() : block;
    }

    private BlockState convertLegacyBlockType(String id, int data) {
        int newId = switch (id) {
            case "minecraft:red_flower" -> 38; // now poppy
            case "minecraft:yellow_flower" -> 37; // now dandelion
            case "minecraft:sapling" -> 6; // oak_sapling
            case "minecraft:deadbush", "minecraft:tallgrass" ->
                31; // dead_bush with fern and grass (not 32!)
            default -> 0;
        };
        String plantedName = null;
        if (newId == 0 && id.startsWith("minecraft:")) {
            plantedName = id.substring(10);
        } else {
            BlockState plantedWithData = LegacyMapper.getInstance().getBlockFromLegacy(newId, data);
            if (plantedWithData != null) {
                plantedName = plantedWithData.getBlockType().id().substring(10); // remove "minecraft:"
            }
        }
        if (plantedName != null) {
            BlockType potAndPlanted = BlockTypes.get("minecraft:potted_" + plantedName);
            if (potAndPlanted != null) {
                return potAndPlanted.getDefaultState();
            }
        }
        return null;
    }
}
