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

package com.sk89q.worldedit.event;

import com.sk89q.rebar.event.Event;
import com.sk89q.rebar.event.ExecutorList;
import com.sk89q.worldedit.LocalConfiguration;

/**
 * Raised when the configuration is loaded.
 */
public class ConfigurationLoadEvent extends Event {
    
    private static final ExecutorList<ConfigurationLoadEvent> executors = new
            ExecutorList<ConfigurationLoadEvent>();
    
    private final LocalConfiguration configuration;

    public ConfigurationLoadEvent(LocalConfiguration configuration) {
        this.configuration = configuration;
    }

    public LocalConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected ExecutorList<ConfigurationLoadEvent> getExecutors() {
        return executors;
    }
    
    protected static ExecutorList<ConfigurationLoadEvent> staticExecutors() {
        return executors;
    }

}
