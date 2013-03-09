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

package org.enginehub.worldedit.command;

import java.lang.annotation.Annotation;

import org.enginehub.command.CommandContext;
import org.enginehub.command.CommandException;
import org.enginehub.command.parametric.StaticParameterResolver;
import org.enginehub.worldedit.WorldEdit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.regions.Region;

public class SelectionParametricResolver extends StaticParameterResolver<Region> {
    
    private final WorldEdit worldEdit;
    
    public SelectionParametricResolver(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    @Override
    public int getConfidenceFor(Class<?> clazz, Annotation[] annotations) {
        return Region.class.isAssignableFrom(clazz) ? CLASS_CONFIDENCE : NO_CONFIDENCE;
    }

    @Override
    public Region resolve(CommandContext context) throws CommandException {
        LocalPlayer player = context.getSafeObject(LocalPlayer.class);
        LocalSession session = worldEdit.getSessions().get(player);
        
        try {
            // @TODO: Contextual selections
            return session.getSelection(player.getWorld());
        } catch (IncompleteRegionException e) {
            // @TODO: Translate
            throw new CommandException(
                    "Sorry, it doesn't look like you have a selection yet.");
        }
    }

}
