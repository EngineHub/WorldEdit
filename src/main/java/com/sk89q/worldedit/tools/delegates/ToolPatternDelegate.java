package com.sk89q.worldedit.tools.delegates;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.tools.delegates.interfaces.ToolPattern;

public class ToolPatternDelegate implements ToolPattern {
    protected Pattern pattern;
  
    public ToolPatternDelegate(Pattern pattern) {
        this.set(pattern);
    }

    public ToolPatternDelegate() {
        this.set(new SingleBlockPattern(new BaseBlock(0)));
    }

    public void set(Pattern pattern) {
        this.pattern = pattern;
    }
    
    public Pattern get() {
        return pattern;
    }
}
