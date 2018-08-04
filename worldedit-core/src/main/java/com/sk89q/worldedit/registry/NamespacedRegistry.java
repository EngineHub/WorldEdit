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

package com.sk89q.worldedit.registry;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

public final class NamespacedRegistry<V> extends Registry<V> {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private final String defaultNamespace;

    public NamespacedRegistry(final String name) {
        this(name, MINECRAFT_NAMESPACE);
    }

    public NamespacedRegistry(final String name, final String defaultNamespace) {
        super(name);
        this.defaultNamespace = defaultNamespace;
    }

    public @Nullable V get(final String key) {
        return super.get(this.orDefaultNamespace(key));
    }

    public V register(final String key, final V value) {
        requireNonNull(key, "key");
        checkState(key.indexOf(':') > -1, "key is not namespaced");
        return super.register(key, value);
    }

    private String orDefaultNamespace(final String key) {
        if (key.indexOf(':') == -1) {
            return defaultNamespace + ':' + key;
        }
        return key;
    }
}
