// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.lang.reflect.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditNotInstalled;
import com.sk89q.worldedit.IncompleteRegionException;

/**
 * Class to access WorldEdit from another plugin.
 *
 * @author sk89q
 */
public class WorldEditBridge {
    /**
     * Invokes an object's method through reflection.
     * 
     * @param obj
     * @param name
     * @param args
     * @param types
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private static Object invokeMethod(Object obj, String name, Object[] args,
            Class<?> ... types) throws InvocationTargetException, IllegalAccessException,
            NoSuchMethodException {
        Method method = obj.getClass().getDeclaredMethod(name, types);
        return method.invoke(obj, args);
    }

    /**
     * Invokes a method through reflection.
     *
     * @param obj
     * @param name
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private static Object invokeMethod(Object obj, String name)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = obj.getClass().getDeclaredMethod(name, new Class[]{});
        return method.invoke(obj, new Object[]{});
    }

    /**
     * Get a point from the currently selected region.
     * 
     * @param player
     * @param methodName
     * @return
     * @throws WorldEditNotInstalled
     * @throws IncompleteRegionException
     */
    private static Vector getRegionPoint(Player player, String methodName)
            throws WorldEditNotInstalled, IncompleteRegionException {
        Plugin plugin = etc.getLoader().getPlugin("WorldEdit");
        if (plugin == null) {
            throw new WorldEditNotInstalled();
        }
        try {
            Object listener = invokeMethod(plugin, "getListener");
            Object session = invokeMethod(listener, "_bridgeSession",
                    new Object[]{ player }, Player.class);
            Object region = invokeMethod(session, "getRegion");
            Object minPoint = invokeMethod(region, methodName);
            int x = (Integer)invokeMethod(minPoint, "getBlockX");
            int y = (Integer)invokeMethod(minPoint, "getBlockY");
            int z = (Integer)invokeMethod(minPoint, "getBlockZ");
            return new Vector(x, y, z);
        } catch (InvocationTargetException e) {
            String exceptionName = e.getTargetException().getClass().getCanonicalName();
            if (exceptionName.equals("com.sk89q.worldedit.IncompleteRegionException")) {
                throw new IncompleteRegionException();
            }
            throw new WorldEditNotInstalled();
        } catch (IllegalAccessException e) {
            throw new WorldEditNotInstalled();
        } catch (NoSuchMethodException e) {
            throw new WorldEditNotInstalled();
        }
    }

    /**
     * Get the selection minimum point.
     *
     * @param player
     * @return
     * @throws WorldEditNotInstalled
     * @throws IncompleteRegionException
     */
    public static Vector getRegionMinimumPoint(Player player)
            throws WorldEditNotInstalled, IncompleteRegionException {
        return getRegionPoint(player, "getMinimumPoint");
    }

    /**
     * Get the selection maximum point.
     *
     * @param player
     * @return
     * @throws WorldEditNotInstalled
     * @throws IncompleteRegionException
     */
    public static Vector getRegionMaximumPoint(Player player)
            throws WorldEditNotInstalled, IncompleteRegionException {
        return getRegionPoint(player, "getMaximumPoint");
    }
}

