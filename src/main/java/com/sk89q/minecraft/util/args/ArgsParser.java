package com.sk89q.minecraft.util.args;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.sk89q.minecraft.util.args.spi.Setter;
import com.sk89q.minecraft.util.args.spi.TypeHandler;

public class ArgsParser {
    private final Map<Class<?>, TypeHandler> typeHandlers = new HashMap<Class<?>, TypeHandler>();
    
    public ArgsParser(Object obj) {
        if(obj == null) return;
        
        new ClassParser().parse(obj, this);
    }

    public void addOption(Setter setter, Option option) {
        OptionClass opt = new OptionClass(option);
    }

    public void addArgument(Setter setter, Argument argument) {
        ArgumentClass arg = new ArgumentClass(argument);
    }
    
    protected void createOptionHandlers(Setter setter) {

        Constructor<? extends TypeHandler> handler;
        
        for(Class<?> cls : setter.getTypes()) {
            if(Enum.class.isAssignableFrom(cls)) {                
                typeHandlers.put(cls, new EnumOptionHandler(setter, cls));
            } else {
                handler = getConstructor(cls);
            }
    
            try {
                return handlerType.newInstance(this, o, setter);
            } catch (InstantiationException e) {
                //throw new IllegalAnnotationError(e);
            } catch (IllegalAccessException e) {
                //throw new IllegalAnnotationError(e);
            } catch (InvocationTargetException e) {
                //throw new IllegalAnnotationError(e);
            }
        }
    }
    
    private static Constructor<? extends TypeHandler> getConstructor(Class<? extends TypeHandler> handlerClass) {
        try {
            return handlerClass.getConstructor(ArgsParser.class, OptionClass.class, Setter.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException();
        }
    }
}
