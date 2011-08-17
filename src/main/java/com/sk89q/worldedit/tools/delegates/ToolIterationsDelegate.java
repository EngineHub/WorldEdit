package com.sk89q.worldedit.tools.delegates;

import com.sk89q.worldedit.tools.delegates.interfaces.ToolIterations;

public class ToolIterationsDelegate implements ToolIterations {
    protected int iterations;
    
    public ToolIterationsDelegate(int iterations) {
        this.iterations = iterations;
    }
    
    public int get() {
        return iterations;
    }
    
    public void set(int iterations) {
        this.iterations = iterations;
    }
}
