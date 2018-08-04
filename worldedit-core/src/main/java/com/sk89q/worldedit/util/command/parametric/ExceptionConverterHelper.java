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

package com.sk89q.worldedit.util.command.parametric;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of an {@link ExceptionConverter} that automatically calls
 * the correct method defined on this object.
 * 
 * <p>Only public methods will be used. Methods will be called in order of decreasing
 * levels of inheritance (between classes where one inherits the other). For two
 * different inheritance branches, the order between them is undefined.</p>
 */
public abstract class ExceptionConverterHelper implements ExceptionConverter {
    
    private final List<ExceptionHandler> handlers;

    @SuppressWarnings("unchecked")
    public ExceptionConverterHelper() {
        List<ExceptionHandler> handlers = new ArrayList<>();
        
        for (Method method : this.getClass().getMethods()) {
            if (method.getAnnotation(ExceptionMatch.class) == null) {
                continue;
            }
            
            Class<?>[] parameters = method.getParameterTypes();
            if (parameters.length == 1) {
                Class<?> cls = parameters[0];
                if (Throwable.class.isAssignableFrom(cls)) {
                    handlers.add(new ExceptionHandler(
                            (Class<? extends Throwable>) cls, method));
                }
            }
        }
        
        Collections.sort(handlers);
        
        this.handlers = handlers;
    }

    @Override
    public void convert(Throwable t) throws CommandException {
        Class<?> throwableClass = t.getClass();
        for (ExceptionHandler handler : handlers) {
            if (handler.cls.isAssignableFrom(throwableClass)) {
                try {
                    handler.method.invoke(this, t);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof CommandException) {
                        throw (CommandException) e.getCause();
                    }
                    throw new WrappedCommandException(e);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new WrappedCommandException(e);
                }
            }
        }
    }
    
    private static class ExceptionHandler implements Comparable<ExceptionHandler> {
        final Class<? extends Throwable> cls;
        final Method method;
        
        private ExceptionHandler(Class<? extends Throwable> cls, Method method) {
            this.cls = cls;
            this.method = method;
        }

        @Override
        public int compareTo(ExceptionHandler o) {
            if (cls.equals(o.cls)) {
                return 0;
            } else if (cls.isAssignableFrom(o.cls)) {
                return 1;
            } else if (o.cls.isAssignableFrom(cls)) {
                return -1;
            } else {
                return cls.getCanonicalName().compareTo(o.cls.getCanonicalName());
            }
        }
    }

}
