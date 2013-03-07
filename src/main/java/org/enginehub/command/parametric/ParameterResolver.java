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

package org.enginehub.command.parametric;

import org.enginehub.command.CommandContext;

/**
 * The resolver for a given method parameter.
 *
 * @see ParameterResolverFactory
 * @param <T> the type returned
 */
public interface ParameterResolver<T> {

    /**
     * Return the expected object by consuming arguments from the context as necessary.
     *
     * @param context the context containing the command's arguments
     * @return the object, possibly null if allowed
     */
    T resolve(CommandContext context);

}