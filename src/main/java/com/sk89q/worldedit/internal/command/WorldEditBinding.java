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

package com.sk89q.worldedit.internal.command;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.command.parametric.*;

/**
 * Binds standard WorldEdit classes such as {@link Player} and {@link LocalSession}.
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
    public Object getSelection(ArgumentStack context, Selection selection) throws IncompleteRegionException, ParameterException {
        Player sender = getPlayer(context);
        LocalSession session = worldEdit.getSessionManager().get(sender);
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
        Player sender = getPlayer(context);
        LocalSession session = worldEdit.getSessionManager().get(sender);
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
        Player sender = getPlayer(context);
        return worldEdit.getSessionManager().get(sender);
    }

    /**
     * Gets an {@link Actor} from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return a local player
     * @throws ParameterException on error
     */
    @BindingMatch(type = Actor.class,
            behavior = BindingBehavior.PROVIDES)
    public Actor getActor(ArgumentStack context) throws ParameterException {
        Actor sender = context.getContext().getLocals().get(Actor.class);
        if (sender == null) {
            throw new ParameterException("Missing 'Actor'");
        } else {
            return sender;
        }
    }

    /**
     * Gets an {@link Player} from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return a local player
     * @throws ParameterException on error
     */
    @BindingMatch(type = Player.class,
                  behavior = BindingBehavior.PROVIDES)
    public Player getPlayer(ArgumentStack context) throws ParameterException {
        Actor sender = context.getContext().getLocals().get(Actor.class);
        if (sender == null) {
            throw new ParameterException("No player to get a session for");
        } else if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new ParameterException("Caller is not a player");
        }
    }

    /**
     * Gets an {@link Player} from a {@link ArgumentStack}.
     *
     * @param context the context
     * @return a local player
     * @throws ParameterException on error
     */
    @SuppressWarnings("deprecation")
    @BindingMatch(type = LocalPlayer.class,
                  behavior = BindingBehavior.PROVIDES)
    public Player getLocalPlayer(ArgumentStack context) throws ParameterException {
        Player player = getPlayer(context);
        if (player instanceof LocalPlayer) {
            return (LocalPlayer) player;
        } else {
            throw new ParameterException("This command/function needs to be updated to take 'Player' rather than 'LocalPlayer'");
        }
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
    public Pattern getPattern(ArgumentStack context) throws ParameterException, WorldEditException {
        Actor actor = context.getContext().getLocals().get(Actor.class);
        ParserContext parserContext = new ParserContext();
        parserContext.setActor(context.getContext().getLocals().get(Actor.class));
        if (actor instanceof Entity) {
            parserContext.setWorld(((Entity) actor).getWorld());
        }
        parserContext.setSession(worldEdit.getSessionManager().get(actor));
        return worldEdit.getPatternRegistry().parseFromInput(context.next(), parserContext);
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
    public Mask getMask(ArgumentStack context) throws ParameterException, WorldEditException {
        Actor actor = context.getContext().getLocals().get(Actor.class);
        ParserContext parserContext = new ParserContext();
        parserContext.setActor(context.getContext().getLocals().get(Actor.class));
        if (actor instanceof Entity) {
            parserContext.setWorld(((Entity) actor).getWorld());
        }
        parserContext.setSession(worldEdit.getSessionManager().get(actor));
        return worldEdit.getMaskRegistry().parseFromInput(context.next(), parserContext);
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
        Player sender = getPlayer(context);
        return worldEdit.getDirection(sender, context.next());
    }

}
