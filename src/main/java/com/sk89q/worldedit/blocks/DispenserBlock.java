// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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
 * Represents dispensers.
 *
 * @author sk89q
 */
public class DispenserBlock extends ContainerBlock {

    /**
     * Construct the dispenser block.
     */
    public DispenserBlock() {
        super(BlockID.DISPENSER, 9);
    }

    /**
     * Construct the dispenser block.
     *
     * @param data
     */
    public DispenserBlock(int data) {
        super(BlockID.DISPENSER, data, 9);
    }

    /**
     * Construct the dispenser block.
     *
     * @param data
     * @param items
     */
    public DispenserBlock(int data, BaseItemStack[] items) {
        super(BlockID.DISPENSER, data, 9);
        this.setItems(items);
    }

    /**
     * Get the tile entity ID.
     *
     * @return
     */
    public String getTileEntityID() {
        return "Trap";
    }

    /**
     * Store additional tile entity data. Returns true if the data is used.
     *
     * @return map of values
     * @throws DataException
     */
    public Map<String, Tag> toTileEntityNBT()
            throws DataException {
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("Items", new ListTag("Items", CompoundTag.class, serializeInventory(getItems())));
        return values;
    }

    /**
     * Get additional information from the title entity data.
     *
     * @param values
     * @throws DataException
     */
    public void fromTileEntityNBT(Map<String, Tag> values)
            throws DataException {
        if (values == null) {
            return;
        }

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
