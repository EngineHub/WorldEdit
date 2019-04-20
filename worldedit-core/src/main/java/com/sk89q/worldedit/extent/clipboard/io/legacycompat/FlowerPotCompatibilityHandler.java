package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.Map;

public class FlowerPotCompatibilityHandler implements NBTCompatibilityHandler {
    @Override
    public <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
        return block.getBlockType() == BlockTypes.FLOWER_POT;
    }

    @Override
    public <B extends BlockStateHolder<B>> B updateNBT(B block, Map<String, Tag> values) {
        Tag item = values.get("Item");
        if (item instanceof StringTag) {
            String id = ((StringTag) item).getValue();
            int data = 0;
            Tag dataTag = values.get("Data");
            if (dataTag instanceof IntTag) {
                data = ((IntTag) dataTag).getValue();
            }
            BlockState newState = convertLegacyBlockType(id, data);
            if (newState != null) {
                return (B) newState; // generics pls :\
            }
        }
        return block;
    }

    private BlockState convertLegacyBlockType(String id, int data) {
        int newId = 0;
        switch (id) {
            case "minecraft:red_flower":
                newId = 38; // now poppy
                break;
            case "minecraft:yellow_flower":
                newId = 37; // now dandelion
                break;
            case "minecraft:sapling":
                newId = 6; // oak_sapling
                break;
            case "minecraft:deadbush":
            case "minecraft:tallgrass":
                newId = 31; // dead_bush with fern and grass (not 32!)
                break;
            default:
                break;
        }
        String plantedName = null;
        if (newId == 0) {
            plantedName = id.substring(10);
        } else {
            BlockState plantedWithData = LegacyMapper.getInstance().getBlockFromLegacy(newId, data);
            if (plantedWithData != null) {
                plantedName = plantedWithData.getBlockType().getId().substring(10); // remove "minecraft:"
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
