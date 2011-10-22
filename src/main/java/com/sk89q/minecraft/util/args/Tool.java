package com.sk89q.minecraft.util.args;

import com.sk89q.worldedit.tools.delegates.interfaces.ToolFlags;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolIterations;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolPattern;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolSize;

public class Tool {
    @Option(aliases = {"size"})
    private ToolSize size;
    @Option(aliases = {"flags"})
    private ToolFlags flags;
    @Option(aliases = {"pattern"})
    private ToolPattern pattern;
    @Option(aliases = {"iterations"})
    private ToolIterations iterations;
    
    @Option(aliases = {"last"})
    public void last() {
        // TODO implement
    }
}
