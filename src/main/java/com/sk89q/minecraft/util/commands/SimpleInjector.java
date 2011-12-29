// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.minecraft.util.commands;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class SimpleInjector implements Injector {
    private static final Logger logger = Logger.getLogger(SimpleInjector.class.getCanonicalName());
	private Object[] args;
	private Class<?>[] argClasses;

	public SimpleInjector(Object... args) {
		this.args = args;
		argClasses = new Class[args.length];
		for (int i = 0; i < args.length; ++i) {
			argClasses[i] = args[i].getClass();
		}
	}

	public Object getInstance(Class<?> clazz) {
		try {
			Constructor<?> ctr = clazz.getConstructor(argClasses);
            ctr.setAccessible(true);
			return ctr.newInstance(args);
		} catch (NoSuchMethodException e) {
            logger.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
            logger.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
            logger.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
            logger.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
			return null;
		}
	}
}
