package com.sk89q.worldedit.tools.delegates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.UnsupportedFlagException;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolFlags;
import com.sk89q.worldedit.tools.enums.ToolFlag;

public class ToolFlagsDelegate implements ToolFlags {
    protected Set<ToolFlag> flags;
    protected Set<ToolFlag> allowedFlags;
    
    public ToolFlagsDelegate(Set<ToolFlag> allowedFlags) {
        this.flags = new HashSet<ToolFlag>();
        this.allowedFlags = allowedFlags;
    }
    
    public ToolFlagsDelegate(ToolFlag[] allowedFlags) {
        this(new HashSet<ToolFlag>(Arrays.asList(allowedFlags)));
    }
    
    public boolean add(ToolFlag flag) throws UnsupportedFlagException {
        if (allowedFlags.contains(flag)) {
            return this.flags.add(flag);
        } else {
            throw new UnsupportedFlagException(flag);
        }
    }
    
    public boolean remove(ToolFlag flag) {
        return this.flags.remove(flag);
    }
    
    public boolean contains(ToolFlag flag) {
        return this.flags.contains(flag);
    }
    
    public void set(Set<ToolFlag> flags) {
        for(ToolFlag flag : flags) {
            if (allowedFlags.contains(flag)) {
                this.flags.add(flag);
            }
        }
    }

    public void clear() {
        this.flags.clear();
    }
}
