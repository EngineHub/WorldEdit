// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.data.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents chests.
 *
 * @author sk89q
 */
public class ChestBlock extends BaseBlock implements TileEntityBlock, ContainerBlock {
    /**
     * Store the list of items.
     */
    private BaseItemStack[] items;

    /**
     * Construct the chest block.
     */
    public ChestBlock() {
        super(BlockID.CHEST);
        items = new BaseItemStack[27];
    }

    /**
     * Construct the chest block.
     *
     * @param data
     */
    public ChestBlock(int data) {
        super(BlockID.CHEST, data);
        items = new BaseItemStack[27];
    }

    /**
     * Construct the chest block.
     *
     * @param data
     * @param items
     */
    public ChestBlock(int data, BaseItemStack[] items) {
        super(BlockID.CHEST, data);
        this.items = items;
    }

    /**
     * Get the list of items.
     *
     * @return
     */
    public BaseItemStack[] getItems() {
        return items;
    }

    /**
     * Set the list of items.
     */
    public void setItems(BaseItemStack[] items) {
        this.items = items;
    }

    /**
     * Get the tile entity ID.
     * 
     * @return
     */
    public String getTileEntityID() {
        return "Chest";
    }

    /**
     * Store additional tile entity data. Returns true if the data is used.
     *
     * @return map of values
     * @throws DataException
     */
    public Map<String,Tag> toTileEntityNBT()
            throws DataException {
        List<Tag> itemsList = new ArrayList<Tag>();
        for (int i = 0; i < items.length; ++i) {
            BaseItemStack item = items[i];
            if (item != null) {
                Map<String,Tag> data = new HashMap<String,Tag>();
                CompoundTag itemTag = new CompoundTag("Items", data);
                data.put("id", new ShortTag("id", (short)item.getType()));
                data.put("Damage", new ShortTag("Damage", item.getDamage()));
                data.put("Count", new ByteTag("Count", (byte)item.getAmount()));
                data.put("Slot", new ByteTag("Slot", (byte)i));
                itemsList.add(itemTag);
            }
        }
        Map<String,Tag> values = new HashMap<String,Tag>();
        values.put("Items", new ListTag("Items", CompoundTag.class, itemsList));
        return values;
    }

    /**
     * Get additional information from the title entity data.
     *
     * @param values
     * @throws DataException
     */
    public void fromTileEntityNBT(Map<String,Tag> values)
            throws DataException  {
        if (values == null) {
            return;
        }


        Tag t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag)t).getValue().equals("Chest")) {
            throw new DataException("'Chest' tile entity expected");
        }

        ListTag items = (ListTag)Chunk.getChildTag(values, "Items", ListTag.class);
        BaseItemStack[] newItems = new BaseItemStack[27];

        for (Tag tag : items.getValue()) {
            if (!(tag instanceof CompoundTag)) {
                throw new DataException("CompoundTag expected as child tag of Chest's Items");
            }

            CompoundTag item = (CompoundTag)tag;
            Map<String,Tag> itemValues = item.getValue();

            short id = (Short)((ShortTag)Chunk.getChildTag(itemValues, "id", ShortTag.class))
                    .getValue();
            short damage = (Short)((ShortTag)Chunk.getChildTag(itemValues, "Damage", ShortTag.class))
                    .getValue();
            byte count = (Byte)((ByteTag)Chunk.getChildTag(itemValues, "Count", ByteTag.class))
                    .getValue();
            byte slot = (Byte)((ByteTag)Chunk.getChildTag(itemValues, "Slot", ByteTag.class))
                    .getValue();

            if (slot >= 0 && slot <= 26) {
                newItems[slot] = new BaseItemStack(id, count, damage);
            }
        }

        this.items = newItems;
    }
}
