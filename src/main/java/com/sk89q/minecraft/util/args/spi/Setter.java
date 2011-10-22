package com.sk89q.minecraft.util.args.spi;


public interface Setter {
    int min();
    
    int max();
    
    void addVaulues(Object ... values);
    
    Class<?>[] getTypes();
}
