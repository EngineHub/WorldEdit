package com.sk89q.minecraft.util.args;

import java.lang.reflect.Field;

import com.sk89q.minecraft.util.args.spi.Setter;

public class FieldSetter implements Setter {
    private final Object object;
    private final Field field;

    public FieldSetter(Object object, Field field) {
        this.object = object;
        this.field = field;
    }
    
    public int min() {
        return 1;
    }
    
    public int max() {
        return 1;
    }
    
    public void addVaulues(Object... values) {
        if (values.length != 1) {
            throw new IllegalArgumentException();
        }
        
        try {
            field.set(object, values[0]);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            try {
                field.set(object, values[0]);
            } catch (IllegalAccessException iae) {
                throw new IllegalAccessError();
            }
        }
    }

    public Class<?>[] getTypes() {
        return new Class<?>[]{field.getType()};
    }

}
