package com.sk89q.worldedit.tools.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum ToolFlag {
    HOLLOW("h"),
    FLAT("f");
    
    private static final Map<String, ToolFlag> lookupName = new HashMap<String, ToolFlag>();
    private final String[] aliases;
    
    private ToolFlag() {
        aliases = new String[]{};
    }
    
    private ToolFlag(final String alias) {
        aliases = new String[]{alias};        
    }
    
    private ToolFlag(final String[] aliases) {
        Set<String> aliasSet = new HashSet<String>(Arrays.asList(aliases));
        this.aliases = (String[])aliasSet.toArray();
    }
    
    public static ToolFlag getFlag(String alias) {
        return lookupName.get(alias.toLowerCase());
    }
    
    public String[] getAliases() {
        return aliases;
    }
    
    static {
        for (ToolFlag flag : values()) {
            lookupName.put(flag.name().toLowerCase(), flag);
            for (String alias : flag.aliases) {
                lookupName.put(alias.toLowerCase(), flag);
            }
        }
    }
}
