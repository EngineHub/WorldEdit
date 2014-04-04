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

package com.sk89q.worldedit.util;

/**
 *
 * @author sk89q
 * @param <T>
 */
public class Countable<T> implements Comparable<Countable<T>> {
    /**
     * ID.
     */
    private T id;
    /**
     * Amount.
     */
    private int amount;

    /**
     * Construct the object.
     *
     * @param id
     * @param amount
     */
    public Countable(T id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    /**
     * @return the id
     */
    public T getID() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setID(T id) {
        this.id = id;
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

    /**
     * Decrement the amount.
     */
    public void decrement() {
        --this.amount;
    }

    /**
     * Increment the amount.
     */
    public void increment() {
        ++this.amount;
    }

    /**
     * Comparison.
     *
     * @param other
     * @return
     */
    public int compareTo(Countable<T> other) {
        if (amount > other.amount) {
            return 1;
        } else if (amount == other.amount) {
            return 0;
        } else {
            return -1;
        }
    }
}
