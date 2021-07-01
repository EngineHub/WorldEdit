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
