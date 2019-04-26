package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.generator.ForestGenerator;
import com.sk89q.worldedit.util.TreeGenerator;

public final class TreeGeneratorFactory implements Contextual<ForestGenerator> {
    private final TreeGenerator.TreeType type;

    public TreeGeneratorFactory(TreeGenerator.TreeType type) {
        this.type = type;
    }

    @Override
    public ForestGenerator createFromContext(EditContext input) {
        return new ForestGenerator((EditSession) input.getDestination(), type);
    }

    @Override
    public String toString() {
        return "tree of type " + type;
    }
}
