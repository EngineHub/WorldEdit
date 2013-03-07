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

import java.lang.annotation.Annotation;


/**
* A static implementation of the {@link ParameterResolverFactory} and
 * {@link ParameterResolver} pair that does not create new objects.
 *
 * @param <T> the type resolved
 */
public abstract class StaticParameterResolver<T> implements ParameterResolverFactory<T>,
        ParameterResolver<T> {

    @Override
    public final ParameterResolver<T> getResolver(Class<?> clazz, Annotation[] annotations) {
        return this;
    }

}
