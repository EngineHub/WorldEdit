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

package com.sk89q.worldedit.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public class Registry<V extends Keyed> implements Iterable<V> {
    private final Map<String, V> map = new HashMap<>();
    private final String name;

    public Registry(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public V get(final String key) {
        checkState(key.equals(key.toLowerCase(Locale.ROOT)), "key must be lowercase");
        return this.map.get(key);
    }

    public V register(final String key, final V value) {
        requireNonNull(key, "key");
        requireNonNull(value, "value");
        checkState(key.equals(key.toLowerCase(Locale.ROOT)), "key must be lowercase");
        checkState(!this.map.containsKey(key), "key '%s' already has an associated %s", key, this.name);
        this.map.put(key, value);
        return value;
    }

    public void clear() {
        this.map.clear();
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(this.map.keySet());
    }

    public Collection<V> values() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    @Override
    public Iterator<V> iterator() {
        return this.map.values().iterator();
    }

}
