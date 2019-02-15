package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.SelectionCylinderEvent;
import com.sk89q.worldedit.internal.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.internal.cui.SelectionPoint2DEvent;
import com.sk89q.worldedit.regions.Polygonal2DRegion;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Polygonal2DCUIMultiRegion extends AbstractCUIMultiRegion {

    private final Polygonal2DRegion region;

    public Polygonal2DCUIMultiRegion(Polygonal2DRegion region, MultiRegionStyle style) {
        super(style);
        checkNotNull(region);
        this.region = region;
    }

    public Polygonal2DCUIMultiRegion(Polygonal2DRegion region, MultiRegionStyle style, double gridSpacing, boolean gridCull) {
        super(style, gridSpacing, gridCull);
        checkNotNull(region);
        this.region = region;
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        super.describeCUI(session, player);
        final List<BlockVector2D> points = region.getPoints();
        for (int id = 0; id < points.size(); id++) {
            session.dispatchClientCUIEvent(player,
                    new WrappedMultiCUIEvent(new SelectionPoint2DEvent(id, points.get(id), region.getArea())),
                    getProtocolVersion());
        }

        session.dispatchClientCUIEvent(player,
                new WrappedMultiCUIEvent(new SelectionMinMaxEvent(region.getMinimumY(), region.getMaximumY())),
                getProtocolVersion());
    }

    @Override
    public String getTypeID() {
        return "polygon2d";
    }
}
