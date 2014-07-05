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

package com.sk89q.worldedit.function.util;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, Consumer is expected to
 * operate via side-effects.
 *
 * This is a functional interface whose functional method is accept(Object).
 *
 * This class is modeled after the Java 8 class java.util.function.Consumer.
 */
public interface WEConsumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    public void accept(T t);
}
