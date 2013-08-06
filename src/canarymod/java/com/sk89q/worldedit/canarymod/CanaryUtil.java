package com.sk89q.worldedit.canarymod;

import net.canarymod.api.world.World;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

public class CanaryUtil {
    /**
     * Turn a position into a world edit vector
     * @param p
     * @return
     */
    public static Vector toVector(Position p) {
        return new Vector(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Turn a worldedit vector to a canarymod position
     * @param v
     * @return
     */
    public static Position toPosition(Vector v) {
        return new Position(v.getX(),v.getY(), v.getZ());
    }

    /**
     * Turn a worldedit vector to a canarymod location
     * @param world
     * @param v
     * @return
     */
    public static Location toLocation(World world, Vector v) {
        return new Location(world, v.getX(), v.getY(), v.getZ(), 0f, 0f);
    }

    /**
     * Get a WorldEdit {@link CanaryWorld} from a CanaryMod {@link World}
     * @param world
     * @return a new {@link CanaryWorld}
     */
    public static LocalWorld getLocalWorld(World world) {
        return new CanaryWorld(world);
    }
}
