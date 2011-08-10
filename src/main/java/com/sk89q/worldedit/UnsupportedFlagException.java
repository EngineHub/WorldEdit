package com.sk89q.worldedit;

import com.sk89q.worldedit.tools.enums.ToolFlag;

public class UnsupportedFlagException extends WorldEditException {
    private static final long serialVersionUID = -7918278228096300476L;
    
    private ToolFlag flag;
    
    public UnsupportedFlagException(ToolFlag flag) {
        this.flag = flag;
    }
    
    public ToolFlag getFlag() {
        return flag;
    }
}
