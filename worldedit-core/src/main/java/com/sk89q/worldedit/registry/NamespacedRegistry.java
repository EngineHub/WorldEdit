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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public final class NamespacedRegistry<V extends Keyed> extends Registry<V> {
    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private final Set<String> knownNamespaces = new HashSet<>();
    private final String defaultNamespace;

    public NamespacedRegistry(final String name) {
        this(name, MINECRAFT_NAMESPACE);
    }

    public NamespacedRegistry(final String name, final String defaultNamespace) {
        super(name);
        this.defaultNamespace = defaultNamespace;
    }

    @Nullable
    @Override
    public V get(final String key) {
        return super.get(this.orDefaultNamespace(key));
    }

    @Override
    public V register(final String key, final V value) {
        requireNonNull(key, "key");
        final int i = key.indexOf(':');
        checkState(i > 0, "key is not namespaced");
        final V registered = super.register(key, value);
        knownNamespaces.add(key.substring(0, i));
        return registered;
    }

    /**
     * Get a set of the namespaces of all registered keys.
     *
     * @return set of namespaces
     */
    public Set<String> getKnownNamespaces() {
        return Collections.unmodifiableSet(knownNamespaces);
    }

    /**
     * Get the default namespace for this registry.
     *
     * @return the default namespace
     */
    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    private String orDefaultNamespace(final String key) {
        if (key.indexOf(':') == -1) {
            return defaultNamespace + ':' + key;
        }
        return key;
    }
}
