package com.sk89q.worldedit;

import org.bukkit.Location;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class ChunkVector2D extends BlockVector2D {

    public static ChunkVector2D fromBlock(int x, int y, int z) {
        return new ChunkVector2D(x >> 4, z >> 4);
    }

    public static ChunkVector2D fromVector(Vector pt) {
        if (pt instanceof ChunkVector)
            return new ChunkVector2D(pt.getBlockX(), pt.getBlockZ());
        return new ChunkVector2D(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
    }

    public static ChunkVector2D fromLocation(Location loc) {
        return new ChunkVector2D(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public static ChunkVector2D fromVector2D(Vector2D pt) {
        if (pt instanceof ChunkVector2D)
            return (ChunkVector2D) pt;
        return new ChunkVector2D(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
    }

    public ChunkVector2D(int x, int z) {
        super(x, z);
    }

    public ChunkVector toChunkVector(int y) {
        return new ChunkVector((int) this.x, y, (int) this.z);
    }

    public ChunkVector toChunkVector(double y) {
        return toChunkVector((int) y);
    }
    
    @Override
    public boolean containedWithin(Vector2D min, Vector2D max) {
        if (min instanceof ChunkVector2D && max instanceof ChunkVector2D) {
            return super.containedWithin(min, max);
        }
        int minX = this.getBlockX() << 4;
        int minZ = this.getBlockZ() << 4;
        int maxX = (this.getBlockX() << 4) + 15;
        int maxZ = (this.getBlockZ() << 4) + 15;
        return minX >= min.getBlockX() && minZ >= min.getBlockZ() && maxX <= max.getBlockX() && maxZ <= max.getBlockZ();
    }

    @Override
    public boolean containedWithinBlock(Vector2D min, Vector2D max) {
        return containedWithin(min, max);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkVector2D) {
            return super.equals(obj);
        }
        return false;
    }
}