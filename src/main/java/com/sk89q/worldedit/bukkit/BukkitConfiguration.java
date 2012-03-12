// $Id$
/*
 * WorldEdit
 * Copyright (C) 2011 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.YAMLConfiguration;

import java.util.logging.Logger;

/**
 * YAMLConfiguration but with setting for no op permissions.
 */
public class BukkitConfiguration extends YAMLConfiguration {

    public boolean noOpPermissions = false;
    public boolean overridePermissions = false;

    public BukkitConfiguration(YAMLProcessor config, Logger logger) {
        super(config, logger);
    }
    
    @Override
    public void load() {
        super.load();
        noOpPermissions = config.getBoolean("no-op-permissions", false);
        overridePermissions = config.getBoolean("override-op-permissions", false); // Feel free to pick a better name! Just something to get things started.
    }
}
