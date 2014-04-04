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

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.DataException;

/**
 * Represents a block that stores items.
 *
 * @author sk89q
 */
public abstract class ContainerBlock extends BaseBlock implements TileEntityBlock {
    
    private BaseItemStack[] items;

    public ContainerBlock(int type, int inventorySize) {
        super(type);
        this.items = new BaseItemStack[inventorySize];
    }

    public ContainerBlock(int type, int data, int inventorySize) {
        super(type, data);
        this.items = new BaseItemStack[inventorySize];
    }

    /**
     * Get the list of items.
     *
     * @return
     */
    public BaseItemStack[] getItems() {
        return this.items;
    }

    /**
     * Set the list of items.
     *
     * @param items
     */
    public void setItems(BaseItemStack[] items) {
        this.items = items;
    }
    
    @Override
    public boolean hasNbtData() {
        return true;
    }

    public Map<String, Tag> serializeItem(BaseItemStack item) {
        Map<String, Tag> data = new HashMap<String, Tag>();
        data.put("id", new ShortTag("id", (short) item.getType()));
        data.put("Damage", new ShortTag("Damage", item.getData()));
        data.put("Count", new ByteTag("Count", (byte) item.getAmount()));
        if (item.getEnchantments().size() > 0) {
            List<CompoundTag> enchantmentList = new ArrayList<CompoundTag>();
            for(Map.Entry<Integer, Integer> entry : item.getEnchantments().entrySet()) {
                Map<String, Tag> enchantment = new HashMap<String, Tag>();
                enchantment.put("id", new ShortTag("id", entry.getKey().shortValue()));
                enchantment.put("lvl", new ShortTag("lvl", entry.getValue().shortValue()));
                enchantmentList.add(new CompoundTag(null, enchantment));
            }

            Map<String, Tag> auxData = new HashMap<String, Tag>();
            auxData.put("ench", new ListTag("ench", CompoundTag.class, enchantmentList));
            data.put("tag", new CompoundTag("tag", auxData));
        }
        return data;
    }

    public BaseItemStack deserializeItem(Map<String, Tag> data) throws DataException {
        short id = NBTUtils.getChildTag(data, "id", ShortTag.class).getValue();
        short damage = NBTUtils.getChildTag(data, "Damage", ShortTag.class).getValue();
        byte count = NBTUtils.getChildTag(data, "Count", ByteTag.class).getValue();

        BaseItemStack stack = new BaseItemStack(id, count, damage);

        if (data.containsKey("tag")) {
            Map<String, Tag> auxData = NBTUtils.getChildTag(data, "tag", CompoundTag.class).getValue();
            ListTag ench = (ListTag)auxData.get("ench");
            for(Tag e : ench.getValue()) {
                Map<String, Tag> vars = ((CompoundTag) e).getValue();
                short enchId = NBTUtils.getChildTag(vars, "id", ShortTag.class).getValue();
                short enchLevel = NBTUtils.getChildTag(vars, "lvl", ShortTag.class).getValue();
                stack.getEnchantments().put((int) enchId, (int) enchLevel);
            }
        }
        return stack;
    }

    public BaseItemStack[] deserializeInventory(List<CompoundTag> items) throws DataException {
        BaseItemStack[] stacks = new BaseItemStack[items.size()];
        for (CompoundTag tag : items) {
            Map<String, Tag> item = tag.getValue();
            BaseItemStack stack = deserializeItem(item);
            byte slot = NBTUtils.getChildTag(item, "Slot", ByteTag.class).getValue();
            if (slot >= 0 && slot < stacks.length) {
                stacks[slot] = stack;
            }
        }
        return stacks;
    }

    public List<CompoundTag> serializeInventory(BaseItemStack[] items) {
        List<CompoundTag> tags = new ArrayList<CompoundTag>();
        for (int i = 0; i < items.length; ++i) {
            if (items[i] != null) {
                Map<String, Tag> tagData = serializeItem(items[i]);
                tagData.put("Slot", new ByteTag("Slot", (byte) i));
                tags.add(new CompoundTag("", tagData));
            }
        }
        return tags;
    }
}
