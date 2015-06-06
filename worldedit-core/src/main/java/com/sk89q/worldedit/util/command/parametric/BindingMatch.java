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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a match of a binding.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindingMatch {
    
    /**
     * The classifier.
     * 
     * @return the classifier, or {@link Annotation} if not set
     */
    Class<? extends Annotation> classifier() default Annotation.class;
    
    /**
     * The type.
     * 
     * @return the type, or {@link Class} if not set
     */
    Class<?>[] type() default Class.class;
    
    /**
     * The binding behavior.
     * 
     * @return the behavior
     */
    BindingBehavior behavior();
    
    /**
     * Get the number of arguments that this binding consumes.
     * 
     * @return -1 if unknown or irrelevant
     */
    int consumedCount() default -1;
    
    /**
     * Set whether an array of modifier annotations is provided in the list of
     * arguments.
     * 
     * @return true to provide modifiers
     */
    boolean provideModifiers() default false;

}
