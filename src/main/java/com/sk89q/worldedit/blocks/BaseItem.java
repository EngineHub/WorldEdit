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
 * Represents an item.
 *
 * @author sk89q
 */
public class BaseItem {
    /**
     * Item ID.
     */
    private int id;
    /**
     * Item damage.
     */
    private short damage;

    /**
     * Construct the object.
     *
     * @param id
     */
    public BaseItem(int id) {
        this.id = id;
        this.damage = 0;
    }

    /**
     * Construct the object.
     *
     * @param id
     * @param damage
     */
    public BaseItem(int id, short damage) {
        this.id = id;
        this.damage = damage;
    }

    /**
     * @return the id
     */
    public int getType() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setType(int id) {
        this.id = id;
    }

    /**
     * @return the damage
     */
    public short getDamage() {
        return damage;
    }

    /**
     * @param damage the damage to set
     */
    public void setDamage(short damage) {
        this.damage = damage;
    }
}
