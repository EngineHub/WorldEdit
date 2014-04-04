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
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.DataException;

/**
 * Represents a furnace block.
 * 
 * @author sk89q
 */
public class FurnaceBlock extends ContainerBlock {

    private short burnTime;
    private short cookTime;

    /**
     * Construct an empty furnace block with the default orientation.
     * 
     * @param type type ID
     */
    public FurnaceBlock(int type) {
        super(type, 2);
    }

    /**
     * Construct an empty furnace block with a given orientation.
     * 
     * @param type type ID
     * @param data orientation
     */
    public FurnaceBlock(int type, int data) {
        super(type, data, 2);
    }

    /**
     * Construct an furnace block with a given orientation and inventory.
     * 
     * @param type type ID
     * @param data orientation
     * @param items inventory items
     */
    public FurnaceBlock(int type, int data, BaseItemStack[] items) {
        super(type, data, 2);
        setItems(items);
    }

    /**
     * Get the burn time.
     * 
     * @return the burn time
     */
    public short getBurnTime() {
        return burnTime;
    }

    /**
     * Set the burn time.
     * 
     * @param burnTime the burn time
     */
    public void setBurnTime(short burnTime) {
        this.burnTime = burnTime;
    }

    /**
     * Get the cook time.
     * 
     * @return the cook time
     */
    public short getCookTime() {
        return cookTime;
    }

    /**
     * Set the cook time.
     * 
     * @param cookTime the cook time to set
     */
    public void setCookTime(short cookTime) {
        this.cookTime = cookTime;
    }

    @Override
    public String getNbtId() {
        return "Furnace";
    }

    @Override
    public CompoundTag getNbtData() {
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("Items", new ListTag("Items", CompoundTag.class,
                serializeInventory(getItems())));
        values.put("BurnTime", new ShortTag("BurnTime", burnTime));
        values.put("CookTime", new ShortTag("CookTime", cookTime));
        return new CompoundTag(getNbtId(), values);
    }

    @Override
    public void setNbtData(CompoundTag rootTag) throws DataException {
        if (rootTag == null) {
            return;
        }
        
        Map<String, Tag> values = rootTag.getValue();

        Tag t = values.get("id");
        if (!(t instanceof StringTag)
                || !((StringTag) t).getValue().equals("Furnace")) {
            throw new DataException("'Furnace' tile entity expected");
        }

        ListTag items = NBTUtils.getChildTag(values, "Items", ListTag.class);

        List<CompoundTag> compound = new ArrayList<CompoundTag>();

        for (Tag tag : items.getValue()) {
            if (!(tag instanceof CompoundTag)) {
                throw new DataException(
                        "CompoundTag expected as child tag of Furnace Items");
            }
            compound.add((CompoundTag) tag);
        }
        setItems(deserializeInventory(compound));

        t = values.get("BurnTime");
        if (t instanceof ShortTag) {
            burnTime = ((ShortTag) t).getValue();
        }

        t = values.get("CookTime");
        if (t instanceof ShortTag) {
            cookTime = ((ShortTag) t).getValue();
        }
    }
}
