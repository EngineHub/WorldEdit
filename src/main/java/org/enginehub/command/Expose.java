// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describe a class method as a command.
 *
 * <p>Classes that manage commands will use this marker to automatically extract
 * commands from a given class.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Expose {

    /**
     * A friendly name for the command, which may be displayed somewhere.
     *
     * <p>If this value is omitted, then a raw name may be used (extracted from the
     * method name).</p>
     *
     * @return friendly name
     */
    String name() default "";

    /**
     * The name of the command and additional aliases.
     *
     * <p>The first entry is considered the primary command name.</p>
     *
     * @return aliases for a command
     */
    String[] aliases();

    /**
     * A short description for the command.
     *
     * <p>An example would be "Stacks blocks in an area repeatedly in one direction.</p>
     */
    String desc();

    /**
     * A long help text for the command.
     *
     * <p>This may be a paragraph long.</p>
     */
    String help() default "";

}
