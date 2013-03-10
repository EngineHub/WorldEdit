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

package org.enginehub.worldedit;

import java.lang.annotation.Annotation;

import org.enginehub.command.CommandContext;
import org.enginehub.command.parametric.StaticParameterResolver;

import com.sk89q.worldedit.LocalWorld;

/**
 * Constructs edit sessions.
 */
public class EditSessionFactory extends StaticParameterResolver<EditSession> {

    EditSessionFactory() {
    }
    
    /**
     * Create a new edit session for a given world.
     * 
     * <p>This method does not impose restrictions on the returned edit session.</p>
     * 
     * @param world the world the edit session is applicable to
     * @return a new edit session
     */
    public EditSession newEditSession(LocalWorld world) {
        return new EditSession(world, -1);
    }

    @Override
    public int getConfidenceFor(Class<?> clazz, Annotation[] annotations) {
        return EditSession.class == clazz ? CLASS_CONFIDENCE : NO_CONFIDENCE;
    }

    @Override
    public EditSession resolve(CommandContext context) {
        return newEditSession(context.getSafeObject(LocalWorld.class));
    }

}
