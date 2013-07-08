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

package com.sk89q.worldedit.util;

import com.sk89q.rebar.command.parametric.BindingBehavior;
import com.sk89q.rebar.command.parametric.BindingHelper;
import com.sk89q.rebar.command.parametric.BindingMatch;
import com.sk89q.rebar.command.parametric.ParameterException;
import com.sk89q.rebar.command.parametric.ArgumentStack;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.UnknownDirectionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.annotation.Direction;
import com.sk89q.worldedit.annotation.Selection;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

/**
 * Binds standard WorldEdit classes such as {@link LocalPlayer} and {@link LocalSession}.
 */
public class WorldEditBinding extends BindingHelper {
    
    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     * 
     * @param worldEdit the WorldEdit instance to bind to
     */
    public WorldEditBinding(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /**
     * Gets a selection from a {@link ArgumentStack}.
     * 
     * @param context the context
     * @param selection the annotation
     * @return a selection
     * @throws IncompleteRegionException if no selection is available
     * @throws ParameterException on other error
     */
    @BindingMatch(classifier = Selection.class,
                  type = Region.class,
                  behavior = BindingBehavior.PROVIDES)
    public Object getSelection(ArgumentStack context, Selection selection)
            throws IncompleteRegionException, ParameterException {
        LocalPlayer sender = getLocalPlayer(context);
        LocalSession session = worldEdit.getSession(sender);
        return session.getSelection(sender.getWorld());
    }

    /**
     * Gets an {@link EditSession} from a {@link ArgumentStack}.
     * 
     * @param context the context
     * @return an edit session
     * @throws ParameterException on other error
     */
    @BindingMatch(type = EditSession.class,
                  behavior = BindingBehavior.PROVIDES)
    public EditSession getEditSession(ArgumentStack context) throws ParameterException {
        LocalPlayer sender = getLocalPlayer(context);
        LocalSession session = worldEdit.getSession(sender);
        EditSession editSession = session.createEditSession(sender);
        editSession.enableQueue();
        context.getContext().getLocals().put(EditSession.class, editSession);
        session.tellVersion(sender); 
        return editSession;
    }

    /**
     * Gets an {@link LocalSession} from a {@link ArgumentStack}.
     * 
     * @param context the context
     * @return a local session
     * @throws ParameterException on error
     */
    @BindingMatch(type = LocalSession.class,
                  behavior = BindingBehavior.PROVIDES)
    public LocalSession getLocalSession(ArgumentStack context) throws ParameterException {
        LocalPlayer sender = getLocalPlayer(context);
        return worldEdit.getSession(sender);
    }

    /**
     * Gets an {@link LocalPlayer} from a {@link ArgumentStack}.
     * 
     * @param context the context
     * @return a local player
     * @throws ParameterException on error
     */
    @BindingMatch(type = LocalPlayer.class,
                  behavior = BindingBehavior.PROVIDES)
    public LocalPlayer getLocalPlayer(ArgumentStack context) throws ParameterException {
        LocalPlayer sender = context.getContext().getLocals().get(LocalPlayer.class);
        if (sender == null) {
            throw new ParameterException("No player to get a session for");
        }
        return sender;
    }

    /**
     * Gets an {@link Pattern} from a {@link ArgumentStack}.
     * 
     * @param context the context
     * @return a pattern
     * @throws ParameterException on error
     * @throws WorldEditException on error
     */
    @BindingMatch(type = Pattern.class,
                  behavior = BindingBehavior.CONSUMES,
                  consumedCount = 1)
    public Pattern getPattern(ArgumentStack context) 
            throws ParameterException, WorldEditException {
        return worldEdit.getBlockPattern(getLocalPlayer(context), context.next());
    }

    /**
     * Gets an {@link Mask} from a {@link ArgumentStack}.
     * 
     * @param context the context
     * @return a pattern
     * @throws ParameterException on error
     * @throws WorldEditException on error
     */
    @BindingMatch(type = Mask.class,
                  behavior = BindingBehavior.CONSUMES,
                  consumedCount = 1)
    public Mask getMask(ArgumentStack context) 
            throws ParameterException, WorldEditException {
        return worldEdit.getBlockMask(getLocalPlayer(context), 
                getLocalSession(context), context.next());
    }

    /**
     * Get a direction from the player.
     * 
     * @param context the context
     * @param direction the direction annotation
     * @return a pattern
     * @throws ParameterException on error
     * @throws UnknownDirectionException on an unknown direction
     */
    @BindingMatch(classifier = Direction.class,
                  type = Vector.class,
                  behavior = BindingBehavior.CONSUMES,
                  consumedCount = 1)
    public Vector getDirection(ArgumentStack context, Direction direction) 
            throws ParameterException, UnknownDirectionException {
        LocalPlayer sender = getLocalPlayer(context);
        return worldEdit.getDirection(sender, context.next());
    }

}
