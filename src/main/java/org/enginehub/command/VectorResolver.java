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

import com.sk89q.worldedit.Vector;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

public class VectorResolver extends StaticResolver<Vector> {

    /**
     * Matches 1,2,3
     */
    private static final Pattern singleXyz = Pattern.compile(
            "(\\d(?:\\.\\d*)?+),(\\d(?:\\.\\d*)?+),(\\d(?:\\.\\d*)?+)");

    @Override
    public int getConfidenceFor(Class<?> clazz, Annotation[] annotations) {
        return Vector.class.isAssignableFrom(clazz) ? ParameterResolverFactory.CLASS_CONFIDENCE : 0;
    }

    @Override
    public Vector resolve(CommandContext context) {
        context.next();
    }
}
