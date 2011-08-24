package com.sk89q.minecraft.util.commands;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SimpleInjector<T> implements Injector {
    private final T injectionObject;
    public SimpleInjector(T injectionObject) {
        this.injectionObject = injectionObject;
    }

    public Object getInstance(Class<?> cls) throws InvocationTargetException,
            IllegalAccessException, InstantiationException {
        try {
            Constructor<?> construct = cls.getConstructor(injectionObject.getClass());
            return construct.newInstance(injectionObject);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
