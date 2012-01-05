package com.sk89q.worldedit.regions;

import java.util.Iterator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

public abstract class AbstractRegion implements Region {
    /**
     * Stores the world.
     */
    protected LocalWorld world;

    public AbstractRegion(LocalWorld world) {
        this.world = world;
    }

    /**
     * Get the iterator.
     *
     * @return iterator of points inside the region
     */
    public Iterator<BlockVector> iterator() {
        return new RegionIterator(this);
    }

    public LocalWorld getWorld() {
        return world;
    }

    public void setWorld(LocalWorld world) {
        this.world = world;
    }

    public void expand(Vector... changes) throws RegionOperationException {
        for (Vector change : changes) {
            expand(change);
        }
    }

    public void contract(Vector... changes) throws RegionOperationException {
        for (Vector change : changes) {
            contract(change);
        }
    }

    public void shift(Vector change) throws RegionOperationException {
        expand(change);
        contract(change);
    }
}
