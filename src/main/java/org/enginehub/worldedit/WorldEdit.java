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

import org.enginehub.command.CommandManager;
import org.enginehub.command.parametric.ParametricBuilder;
import org.enginehub.event.EventSystem;
import org.enginehub.event.Handler;
import org.enginehub.session.FactoryBasedSessionMap;
import org.enginehub.session.SessionFactory;
import org.enginehub.session.SessionMap;
import org.enginehub.util.Owner;
import org.enginehub.worldedit.command.SelectionParametricResolver;
import org.enginehub.worldedit.event.WandUseEvent;
import org.enginehub.worldedit.operation.ReplaceBlocks;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.regions.RegionSelector;

/**
 * An instance of WorldEdit.
 */
public final class WorldEdit implements 
    SessionFactory<LocalPlayer, LocalSession>, Owner {

    private static WorldEdit instance;

    private LocalConfiguration config = new LocalConfiguration();
    // @TODO: Better configuration
    
    private final OperationRegistry operations;
    private final CommandManager commands;
    private final ParametricBuilder builder;
    private final EditSessionFactory editSessionFactory;
    private final FactoryBasedSessionMap<LocalPlayer, LocalSession> sessions;

    private WorldEdit() {
        commands = new CommandManager();
        editSessionFactory = new EditSessionFactory();
        builder = new ParametricBuilder();
        builder.add(editSessionFactory);
        operations = new OperationRegistry(commands, builder);
        sessions = new FactoryBasedSessionMap<LocalPlayer, LocalSession>(this,
                LocalPlayer.class, LocalSession.class);
        builder.add(sessions);
        builder.add(new SelectionParametricResolver(this));

        // @TODO: Better way to register operations
        operations.add(ReplaceBlocks.class);
        
        EventSystem.getInstance().register(this);
    }

    /**
     * Get the singleton for WorldEdit.
     *
     * <p>The returned object can be stored for later usage.</p>
     *
     * @return singleton
     */
    public synchronized static WorldEdit getInstance() {
        if (instance == null) {
            instance = new WorldEdit();
        }

        return instance;
    }

    /**
     * Get the operations registry that is used to register operations with WorldEdit.
     *
     * @return the operations registry
     */
    public OperationRegistry getOperations() {
        return operations;
    }

    /**
     * Get the commands manager.
     *
     * @return the commands manager
     */
    public CommandManager getCommands() {
        return commands;
    }
    
    /**
     * Get the object responsible for constructing new {@link EditSession}s.
     * 
     * @return the factory
     */
    public EditSessionFactory getEditSessionFactory() {
        return editSessionFactory;
    }

    /**
     * Get the configuration.
     * 
     * @return the configuration
     */
    public LocalConfiguration getConfiguration() {
        return config;
    }

    /**
     * Set the configuration.
     * 
     * @param config the configuration
     */
    @Deprecated
    public void setConfiguration(LocalConfiguration config) {
        this.config = config;
    }

    /**
     * Get the session map.
     * 
     * @return the sessions
     */
    public SessionMap<LocalPlayer, LocalSession> getSessions() {
        return sessions;
    }

    @Override
    public LocalSession createSession(LocalPlayer key) {
        return new LocalSession(config);
    }
    
    @Handler
    public void onWandUse(WandUseEvent event) {
        LocalPlayer actor = event.getActor();

        if (!actor.hasPermission("worldedit.selection.pos")) {
            return;
        }
        
        LocalSession session = sessions.get(event.getActor());

        if (session.isToolControlEnabled()) {
            RegionSelector selector = session.getRegionSelector(actor.getWorld());
            
            // Do selection
            switch (event.getFunction()) {
            case PRIMARY:
                if (selector.selectPrimary(event.getPosition())) {
                    selector.explainPrimarySelection(
                            actor, session, event.getPosition());
                }
                break;
                
            case SECONDARY:
                if (selector.selectSecondary(event.getPosition())) {
                    selector.explainSecondarySelection(
                            actor, session, event.getPosition());
                }
                break;
            }

            event.setCancelled(true);
        }
    }

}
