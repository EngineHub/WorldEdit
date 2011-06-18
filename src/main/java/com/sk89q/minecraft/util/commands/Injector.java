// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/

package com.sk89q.minecraft.util.commands;

import java.lang.reflect.InvocationTargetException;

/**
 * Constructs new instances.
 */
public interface Injector {

    /**
     * Constructs a new instance of the given class.
     * 
     * @param cls class
     * @return object
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public Object getInstance(Class<?> cls) throws InvocationTargetException,
            IllegalAccessException, InstantiationException;

}
