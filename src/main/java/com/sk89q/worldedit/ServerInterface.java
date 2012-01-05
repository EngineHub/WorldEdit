// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit;

import com.sk89q.minecraft.util.commands.Command;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author sk89q
 */
public abstract class ServerInterface {
    /**
     * Resolves an item name to its ID.
     *
     * @param name
     * @return
     */
    public abstract int resolveItem(String name);

    /**
     * Checks if a mob type is valid.
     *
     * @param type
     * @return
     */
    public abstract boolean isValidMobType(String type);

    /**
     * Reload WorldEdit configuration.
     */
    public abstract void reload();

    /**
     * Schedules the given <code>task</code> to be invoked once every <code>period</code> ticks
     * after an initial delay of <code>delay</code> ticks.
     *
     * @param delay Delay in server ticks before executing first repeat
     * @param period Period in server ticks of the task
     * @param task Task to be executed
     * @return Task id number (-1 if scheduling failed)
     */
    public int schedule(long delay, long period, Runnable task) {
        return -1;
    }

    public List<LocalWorld> getWorlds() {
        return Collections.emptyList();
    }
    
    public void onCommandRegistration(List<Command> commands) {
        // Do nothing :)
    }
}
