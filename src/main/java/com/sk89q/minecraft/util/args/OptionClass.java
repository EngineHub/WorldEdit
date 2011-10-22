package com.sk89q.minecraft.util.args;

public class OptionClass extends ArgumentClass {
    private final String[] aliases;
    
    public OptionClass(Option o) {
        super(o.required(), o.usage());
        this.aliases = o.aliases();
    }
    
    public String[] aliases() {
        return aliases;
    }
}
