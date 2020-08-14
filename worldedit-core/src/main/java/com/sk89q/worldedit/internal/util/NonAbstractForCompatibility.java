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

package com.sk89q.worldedit.internal.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated method is only non-{@code abstract} for compatibility with old subclasses,
 * and will be made {@code abstract} in the next major version of WorldEdit.
 *
 * <p>
 * Any new subclasses <em>must</em> override the annotated method, failing to do so will result in
 * an exception at runtime.
 * </p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonAbstractForCompatibility {

    // Note that this annotation only functions properly if no other method in the same class
    // shares the name of the annotated function AND is also annotated with this annotation.
    // Otherwise, we cannot uniquely determine the calling method via reflection hacks.
    // This could be changed, but it's not currently necessary.

    /**
     * The name of the method delegated to by the annotated method.
     */
    String delegateName();

    /**
     * The parameter types of the method delegated to by the annotated method.
     */
    Class<?>[] delegateParams();

}
