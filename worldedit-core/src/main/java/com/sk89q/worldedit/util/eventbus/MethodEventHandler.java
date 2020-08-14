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

package com.sk89q.worldedit.util.eventbus;

import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Invokes a {@link Method} to dispatch an event.
 */
public class MethodEventHandler extends EventHandler {

    private final Object object;
    private final Method method;

    /**
     * Create a new event handler.
     *
     * @param priority the priority
     * @param method the method
     */
    public MethodEventHandler(Priority priority, Object object, Method method) {
        super(priority);
        checkNotNull(method);
        this.object = object;
        this.method = method;
    }

    /**
     * Get the method.
     *
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    @Override
    public void dispatch(Object event) throws Exception {
        method.invoke(object, event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodEventHandler that = (MethodEventHandler) o;

        if (!method.equals(that.method)) {
            return false;
        }
        if (object != null ? !object.equals(that.object) : that.object != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + method.hashCode();
        return result;
    }
}
