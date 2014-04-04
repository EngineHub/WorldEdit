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
 * Represents dispensers.
 *
 * @author sk89q
 */
public class DispenserBlock extends ContainerBlock {

    /**
     * Construct an empty dispenser block.
     */
    public DispenserBlock() {
        super(BlockID.DISPENSER, 9);
    }

    /**
     * Construct an empty dispenser block.
     *
     * @param data data value (orientation)
     */
    public DispenserBlock(int data) {
        super(BlockID.DISPENSER, data, 9);
    }

    /**
     * Construct a dispenser block with the given orientation and inventory.
     *
     * @param data data value (orientation)
     * @param items array of items in the inventory
     */
    public DispenserBlock(int data, BaseItemStack[] items) {
        super(BlockID.DISPENSER, data, 9);
        this.setItems(items);
    }

    @Override
    public String getNbtId() {
        return "Trap";
    }

    @Override
    public CompoundTag getNbtData() {
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("Items", new ListTag("Items", CompoundTag.class,
                serializeInventory(getItems())));
        return new CompoundTag(getNbtId(), values);
    }

    @Override
    public void setNbtData(CompoundTag rootTag) throws DataException {
        if (rootTag == null) {
            return;
        }
        
        Map<String, Tag> values = rootTag.getValue();

        Tag t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag) t).getValue().equals("Trap")) {
            throw new DataException("'Trap' tile entity expected");
        }

        List<CompoundTag> items = new ArrayList<CompoundTag>();
        for (Tag tag : NBTUtils.getChildTag(values, "Items", ListTag.class).getValue()) {
            if (!(tag instanceof CompoundTag)) {
                throw new DataException("CompoundTag expected as child tag of Trap Items");
            }

            items.add((CompoundTag) tag);
        }

        setItems(deserializeInventory(items));
    }
}
