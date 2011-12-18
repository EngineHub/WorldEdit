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
     * Construct the object.
     *
     * @param id
     */
    public BaseItemStack(int id) {
        super(id);
    }

    /**
     * Construct the object.
     *
     * @param id
     * @param amount 
     */
    public BaseItemStack(int id, int amount) {
        super(id);
        this.amount = amount;
    }

    /**
     * Construct the object.
     *
     * @param id
     * @param amount 
     * @param damage 
     */
    public BaseItemStack(int id, int amount, short damage) {
        super(id, damage);
        this.amount = amount;
    }

    /**
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
