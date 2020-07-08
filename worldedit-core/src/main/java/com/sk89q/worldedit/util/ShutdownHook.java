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

public class ShutdownHook<V> implements AutoCloseable {

    private final Thread hook;
    private final V value;

    public ShutdownHook(Thread hook, V value) {
        this.hook = hook;
        this.value = value;

        Runtime.getRuntime().addShutdownHook(hook);
    }

    public V getValue() {
        return value;
    }

    @Override
    public void close() throws Exception {
        Runtime.getRuntime().removeShutdownHook(hook);
    }
}
