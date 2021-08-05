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

import java.lang.invoke.MethodHandle;
import java.util.Objects;

public class MethodHandleEventHandler extends EventHandler {

    private final MethodHandle methodHandle;
    private final Object object;

    /**
     * Create a new event handler that uses MethodHandles to dispatch.
     *
     * @param priority the priority
     */
    protected MethodHandleEventHandler(Priority priority, Object object, MethodHandle methodHandle) {
        super(priority);

        this.object = object;
        this.methodHandle = methodHandle;
    }

    @Override
    public void dispatch(Object event) throws Exception {
        try {
            this.methodHandle.invokeExact(object, event);
        } catch (Throwable e) {
            // ew
            throw new Exception(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodHandle, object);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodHandleEventHandler that = (MethodHandleEventHandler) o;

        if (!methodHandle.equals(that.methodHandle)) {
            return false;
        }

        return Objects.equals(object, that.object);
    }
}
