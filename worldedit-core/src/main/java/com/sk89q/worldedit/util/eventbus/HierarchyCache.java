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

package com.sk89q.worldedit.util.eventbus;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.internal.annotation.RequiresNewerGuava;

import java.util.*;

/**
 * Holds a cache of class hierarchy.
 *
 * <p>This exists because Bukkit has an ancient version of Guava and the cache
 * library in Guava has since changed.</>
 */
@RequiresNewerGuava
class HierarchyCache {

    private final Map<Class<?>, Set<Class<?>>> cache = new WeakHashMap<>();

    public Set<Class<?>> get(Class<?> concreteClass) {
        Set<Class<?>> ret = cache.get(concreteClass);
        if (ret == null) {
            ret = build(concreteClass);
            cache.put(concreteClass, ret);
        }
        return ret;
    }

    protected Set<Class<?>> build(Class<?> concreteClass) {
        List<Class<?>> parents = Lists.newLinkedList();
        Set<Class<?>> classes = Sets.newHashSet();

        parents.add(concreteClass);

        while (!parents.isEmpty()) {
            Class<?> clazz = parents.remove(0);
            classes.add(clazz);

            Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                parents.add(parent);
            }

            Collections.addAll(parents, clazz.getInterfaces());
        }

        return classes;
    }

}
