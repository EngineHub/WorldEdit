package com.sk89q.worldedit.regions;

import java.util.List;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

public class Polygonal2DPointsDragNDrop extends Polygonal2DRegionSelector {

    private int index;

    public Polygonal2DPointsDragNDrop(RegionSelector oldSelector) {
        super(oldSelector);
    }

    public Polygonal2DPointsDragNDrop(LocalWorld world, List<BlockVector2D> points, int minY, int maxY) {
        super(world, points, minY, maxY);
    }

    @Override
    public boolean selectPrimary(Vector pos) {
        int newIndex = region.indexOf(new BlockVector2D(pos.getX(), pos.getZ()));
        if (newIndex == -1 || index == newIndex) {
            return false;
        }

        index = newIndex;
        return true;
    }

    @Override
    public boolean selectSecondary(Vector pos) {
        if (index == -1) {
            return false;
        }

        BlockVector2D currentPt = region.getPoint(index);
        BlockVector2D newPt = new BlockVector2D(pos.getX(), pos.getZ());
        int y = pos.getBlockY();
        if (currentPt.equals(newPt)
                && y >= region.getMininumY()
                && y <= region.getMaximumY()) {
            return false;
        }

        if (index == 0) {
            pos1 = pos.toBlockVector();
        }

        region.setPoint(index, newPt);
        region.expandY(y);
        return true;
    }

    @Override
    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        player.print("Selected point at " + pos.toVector2D() + ".");
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        player.print("Set new position at " + pos + ".");

        explainRegionAdjust(player, session);
    }

    @Override
    public void clear() {
        super.clear();
        index = -1;
    }
}
