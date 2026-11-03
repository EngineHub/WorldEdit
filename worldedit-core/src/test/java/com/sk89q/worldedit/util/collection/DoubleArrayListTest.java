/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DoubleArrayListTest {

    private static List<Double> keys(Iterator<Map.Entry<Double, String>> iterator) {
        List<Double> keys = new ArrayList<>();
        iterator.forEachRemaining(entry -> keys.add(entry.getKey()));
        return keys;
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4})
    void matchesArrayListModel(long seed) {
        Random random = new Random(seed);
        List<Double> modelKeys = new ArrayList<>();
        List<String> modelValues = new ArrayList<>();
        DoubleArrayList<Double, String> list = new DoubleArrayList<>(false);

        for (int round = 0; round < 50; round++) {
            int puts = random.nextInt(20);
            for (int i = 0; i < puts; i++) {
                double key = random.nextDouble();
                String value = "v" + random.nextInt();
                list.put(key, value);
                modelKeys.add(key);
                modelValues.add(value);
            }
            assertEquals(modelKeys.size(), list.size());

            List<Double> forwardKeys = new ArrayList<>();
            List<String> forwardValues = new ArrayList<>();
            for (Map.Entry<Double, String> entry : list) {
                forwardKeys.add(entry.getKey());
                forwardValues.add(entry.getValue());
            }
            assertEquals(modelKeys, forwardKeys);
            assertEquals(modelValues, forwardValues);

            List<Double> reversedKeys = new ArrayList<>();
            List<String> reversedValues = new ArrayList<>();
            list.iterator(true).forEachRemaining(entry -> {
                reversedKeys.add(entry.getKey());
                reversedValues.add(entry.getValue());
            });
            Collections.reverse(reversedKeys);
            Collections.reverse(reversedValues);
            assertEquals(modelKeys, reversedKeys);
            assertEquals(modelValues, reversedValues);

            if (random.nextInt(5) == 0) {
                list.clear();
                modelKeys.clear();
                modelValues.clear();
                assertEquals(0, list.size());
                assertFalse(list.iterator().hasNext());
            }
        }
    }

    @Test
    void defaultIterationOrderFollowsConstructorFlag() {
        DoubleArrayList<Double, String> forward = new DoubleArrayList<>(false);
        DoubleArrayList<Double, String> reversed = new DoubleArrayList<>(true);
        for (int i = 0; i < 4; i++) {
            forward.put((double) i, "x");
            reversed.put((double) i, "x");
        }
        assertEquals(List.of(0.0, 1.0, 2.0, 3.0), keys(forward.iterator()));
        assertEquals(List.of(3.0, 2.0, 1.0, 0.0), keys(reversed.iterator()));
        assertEquals(List.of(3.0, 2.0, 1.0, 0.0), keys(forward.iterator(true)));
        assertEquals(List.of(0.0, 1.0, 2.0, 3.0), keys(reversed.iterator(false)));
    }

    @Test
    void emptyListIteratorsAreExhausted() {
        DoubleArrayList<Double, String> list = new DoubleArrayList<>(false);
        assertFalse(list.iterator(false).hasNext());
        assertFalse(list.iterator(true).hasNext());
        assertThrows(NoSuchElementException.class, () -> list.iterator(false).next());
        assertThrows(NoSuchElementException.class, () -> list.iterator(true).next());
    }

    @Test
    void entriesAndIteratorsAreReadOnly() {
        DoubleArrayList<Double, String> list = new DoubleArrayList<>(false);
        list.put(1.0, "a");
        Iterator<Map.Entry<Double, String>> iterator = list.iterator();
        Map.Entry<Double, String> entry = iterator.next();
        assertThrows(UnsupportedOperationException.class, () -> entry.setValue("b"));
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        assertThrows(UnsupportedOperationException.class, () -> list.iterator(true).remove());
    }
}
