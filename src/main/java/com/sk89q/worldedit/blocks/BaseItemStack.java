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

/**
 * Represents a stack of BaseItems.
 *
 * @author sk89q
 */
public class BaseItemStack extends BaseItem {
    /**
     * Amount of an item.
     */
    private int amount = 1;

    /**
     * Construct the object with default stack size of one, with data value of 0.
     *
     * @param id with data value of 0.
     */
    public BaseItemStack(int id) {
        super(id);
    }

    /**
     * Construct the object.
     *
     * @param id type ID
     * @param amount amount in the stack
     */
    public BaseItemStack(int id, int amount) {
        super(id);
        this.amount = amount;
    }

    /**
     * Construct the object.
     *
     * @param id type ID
     * @param amount amount in the stack
     * @param data data value
     */
    public BaseItemStack(int id, int amount, short data) {
        super(id, data);
        this.amount = amount;
    }

    /**
     * Get the number of items in the stack.
     * 
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Set the amount of items in the stack.
     * 
     * @param amount the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
