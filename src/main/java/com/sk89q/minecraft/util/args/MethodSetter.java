package com.sk89q.minecraft.util.args;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sk89q.minecraft.util.args.spi.Setter;

public class MethodSetter implements Setter {

    private Object object;
    private Method method;

    public MethodSetter(Object object, Method method) {
        this.object = object;
        this.method = method;
    }
    
    public int min() {
        return method.isVarArgs() ? method.getTypeParameters().length - 1 : method.getTypeParameters().length;
    }
    
    public int max() {
        return method.isVarArgs() ? -1 : method.getTypeParameters().length;
    }
    
    public void addVaulues(Object... values) {        
        try {
            try {
                method.invoke(object, values);
            } catch (IllegalAccessException e) {
                method.setAccessible(true);
                try {
                    method.invoke(object, values);
                } catch (IllegalAccessException iae) {
                    throw new IllegalAccessError();
                }
            }
        } catch (InvocationTargetException e) {
            
        }
        
    }

    public Class<?>[] getTypes() {
        return method.getParameterTypes();
    }

}
