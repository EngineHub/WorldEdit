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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides a simple generic way to get objects based on weighted probabilities
 */
public class GenericRandomList<T> {

    private final Random random = new Random();
    protected List<Chance> objects = new ArrayList<Chance>();
    private double max = 0;

    /**
     * Adds an object to the weighted list
     * 
     * @param object
     *            The object
     * @param chance
     *            Whatever positive number
     */
    public void add(T object, double chance) {
        checkNotNull(object);
        objects.add(new Chance(object, chance));
        max += chance;
    }

    /**
     * Returns a random object from the list
     * 
     * @return An object of the type T
     */
    public T get() {
        double r = random.nextDouble();
        double offset = 0;

        for (Chance chance : objects) {
            if (r <= (offset + chance.getChance()) / max) {
                return chance.getObject();
            }
            offset += chance.getChance();
        }

        throw new NullPointerException();
    }

    class Chance {
        private T object;
        private double chance;

        private Chance(T object, double chance) {
            this.object = object;
            this.chance = chance;
        }

        public T getObject() {
            return object;
        }

        public double getChance() {
            return chance;
        }
    }
}
