package com.sk89q.worldedit.tools.delegates.interfaces;

import java.util.Set;

import com.sk89q.worldedit.UnsupportedFlagException;
import com.sk89q.worldedit.tools.enums.ToolFlag;

public interface ToolFlags {

    public abstract boolean add(ToolFlag flag) throws UnsupportedFlagException;

    public abstract boolean remove(ToolFlag flag);

    public abstract boolean contains(ToolFlag flag);
    
    public abstract void clear();
    
    public abstract void set(Set<ToolFlag> flags);
}