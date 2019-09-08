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

public class Countable<T> implements Comparable<Countable<T>> {

    private T id;
    private int amount;

    /**
     * Construct the object.
     *
     * @param id the ID
     * @param amount the count of
     */
    public Countable(T id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    public T getID() {
        return id;
    }

    public void setID(T id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

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

    @Override
    public int compareTo(Countable<T> other) {
        return Integer.compare(amount, other.amount);
    }
}
