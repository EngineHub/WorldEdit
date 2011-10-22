package com.sk89q.minecraft.util.args;

public class ArgumentClass {    
    private final String usage;

    private final boolean required;
        
    public ArgumentClass(Argument a) {

        this.usage = a.usage();
        this.required = a.required();
    }

    protected ArgumentClass(boolean required, String usage) {

        this.required = required;
        this.usage = usage;
    }
        
    public String usage() {
        return usage;
    }
    
    public boolean required() {
        return required;
    }
}
