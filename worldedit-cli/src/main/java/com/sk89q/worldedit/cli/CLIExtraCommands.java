/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.cli;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.task.Task;
import com.sk89q.worldedit.world.World;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;

import java.util.concurrent.ExecutionException;

@CommandContainer
public class CLIExtraCommands {
    @Command(
        name = "selectworld",
        desc = "Select the entire world"
    )
    public void selectWorld(Actor actor, World world, LocalSession session) {
        session.setRegionSelector(world, new CuboidRegionSelector(
            world, world.getMinimumPoint(), world.getMaximumPoint()
        ));
        actor.printInfo(TextComponent.of("Selected the entire world."));
    }

    @Command(
        name = "await",
        desc = "Await all pending tasks"
    )
    public void await() {
        for (Task<?> task : WorldEdit.getInstance().getSupervisor().getTasks()) {
            try {
                task.get();
            } catch (InterruptedException e) {
                WorldEdit.logger.warn("Interrupted awaiting task", e);
            } catch (ExecutionException e) {
                WorldEdit.logger.warn("Error awaiting task", e);
            }
        }
    }
}
