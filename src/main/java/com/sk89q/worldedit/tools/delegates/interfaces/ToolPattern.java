package com.sk89q.worldedit.tools.delegates.interfaces;

import com.sk89q.worldedit.patterns.Pattern;

public interface ToolPattern {

    public abstract void set(Pattern pattern);

    public abstract Pattern get();

}