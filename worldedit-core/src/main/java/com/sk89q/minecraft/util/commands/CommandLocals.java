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

package com.sk89q.minecraft.util.commands;

import java.util.HashMap;
import java.util.Map;

public class CommandLocals {
    
    private final Map<Object, Object> locals = new HashMap<>();

    public boolean containsKey(Object key) {
        return locals.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return locals.containsValue(value);
    }

    public Object get(Object key) {
        return locals.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> key) {
        return (T) locals.get(key);
    }

    public Object put(Object key, Object value) {
        return locals.put(key, value);
    }

}
