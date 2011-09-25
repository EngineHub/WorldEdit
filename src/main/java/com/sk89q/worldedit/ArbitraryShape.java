package com.sk89q.worldedit;

import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

public abstract class ArbitraryShape {
    private Region extent;

    public ArbitraryShape(Region extent) {
        this.extent = extent;
    }

    protected Region getExtent() {
        return extent;
    }

    protected abstract boolean isInside(double x, double y, double z);

    public int generate(EditSession editSession, Pattern pattern, boolean hollow) throws MaxChangedBlocksException {
        int affected = 0;

        for (BlockVector position : getExtent()) {
            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            if (!isInside(x, y, z))
                continue;

            if (hollow) {
                boolean draw = false;
                do {
                    if (!isInside(x+1, y, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x-1, y, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y+1, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y-1, z)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y, z+1)) {
                        draw = true;
                        break;
                    }
                    if (!isInside(x, y, z-1)) {
                        draw = true;
                        break;
                    }

                } while (false);

                if (!draw) {
                    continue;
                }
            }

            if (editSession.setBlock(position, pattern)) {
                ++affected;
            }
        }

        return affected;
    }
}
