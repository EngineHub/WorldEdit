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

package com.sk89q.worldedit.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.DataException;

/**
 * Represents a chest block.
 *
 * @author sk89q
 */
public class ChestBlock extends ContainerBlock {

    /**
     * Construct an empty chest block with the default orientation (data value).
     */
    public ChestBlock() {
        super(BlockID.CHEST, 27);
    }

    /**
     * Construct an empty chest block with a custom data value.
     *
     * @param data data indicating the position of the chest
     */
    public ChestBlock(int data) {
        super(BlockID.CHEST, data, 27);
    }

    /**
     * Construct the chest block with a custom data value and a list of items.
     *
     * @param data data indicating the position of the chest
     * @param items array of items
     */
    public ChestBlock(int data, BaseItemStack[] items) {
        super(BlockID.CHEST, data, 27);
        setItems(items);
    }

    @Override
    public String getNbtId() {
        return "Chest";
    }

    @Override
    public CompoundTag getNbtData() {
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("Items", new ListTag("Items", CompoundTag.class, serializeInventory(getItems())));
        return new CompoundTag(getNbtId(), values);
    }

    @Override
    public void setNbtData(CompoundTag rootTag) throws DataException {
        if (rootTag == null) {
            return;
        }
        
        Map<String, Tag> values = rootTag.getValue();

        Tag t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag) t).getValue().equals("Chest")) {
            throw new DataException("'Chest' tile entity expected");
        }

        List<CompoundTag> items = new ArrayList<CompoundTag>();
        
        for (Tag tag : NBTUtils.getChildTag(values, "Items", ListTag.class).getValue()) {
            if (!(tag instanceof CompoundTag)) {
                throw new DataException("CompoundTag expected as child tag of Chest's Items");
            }

            items.add((CompoundTag) tag);
        }

        setItems(deserializeInventory(items));
    }
}
