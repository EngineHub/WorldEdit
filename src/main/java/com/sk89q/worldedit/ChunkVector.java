package com.sk89q.worldedit;

import org.bukkit.Location;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;

public class ChunkVector extends BlockVector {

    public static ChunkVector fromBlock(int x, int y, int z) {
        return new ChunkVector(x >> 4, y >> 4, z >> 4);
    }
    
    public static ChunkVector fromVector(Vector pt) {
        if (pt instanceof ChunkVector)
            return (ChunkVector) pt;
        return new ChunkVector(pt.getBlockX() >> 4, pt.getBlockY() >> 4, pt.getBlockZ() >> 4);
    }
    
    public static ChunkVector fromLocation(Location loc) {
        return new ChunkVector(loc.getBlockX() >> 4, loc.getBlockY() >> 4, loc.getBlockZ() >> 4);
    }
    
    public ChunkVector(int x, int y, int z) {
        super(x, y, z);
    }
    
    public ChunkVector2D to2D() {
        return new ChunkVector2D((int) x, (int) y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkVector)
            super.equals(obj);
        
        return false;
    }

    @Override
    public boolean containedWithin(Vector min, Vector max) {
        if (min instanceof ChunkVector && max instanceof ChunkVector) {
            return super.containedWithin(min, max);
        }
        int minX = this.getBlockX() << 4;
        int minY = this.getBlockY() << 4;
        int minZ = this.getBlockZ() << 4;
        int maxX = (this.getBlockX() << 4) + 15;
        int maxY = (this.getBlockY() << 4) + 15;
        int maxZ = (this.getBlockZ() << 4) + 15;
        return minX >= min.getBlockX() && minY >= min.getBlockY() && minZ >= min.getBlockZ() && maxX <= max.getBlockX() && maxY <= max.getBlockY() && maxZ <= max.getBlockZ();
    }

    @Override
    public boolean containedWithinBlock(Vector min, Vector max) {
        return containedWithin(min, max);
    }
}
