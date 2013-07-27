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

package com.sk89q.rebar.util;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract implementation of {@link Describable}.
 */
public abstract class AbstractDescribable implements Describable {
    
    private final Map<Class<?>, Object> metadata = new HashMap<Class<?>, Object>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getMetadata(Class<T> metadataClass) {
        Object m = metadata.get(metadataClass);
        if (m != null) {
            return (T) m;
        }
        return null;
    }

    @Override
    public void setMetadata(Object m) {
        metadata.put(m.getClass(), m);
    }

}
