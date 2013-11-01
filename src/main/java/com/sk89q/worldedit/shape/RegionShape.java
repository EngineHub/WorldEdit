package com.sk89q.worldedit.shape;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.shape.ArbitraryShape;

/**
 * Generates solid and hollow shapes according to materials returned by the
 * {@link #getMaterial} method.
 *
 * @author TomyLobo
 */
public class RegionShape extends ArbitraryShape {
    public RegionShape(Region extent) {
        super(extent);
    }

    @Override
    protected BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial) {
        if (!this.extent.contains(new Vector(x, y, z))) {
            return null;
        }

        return defaultMaterial;
    }
}
