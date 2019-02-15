package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.SelectionCylinderEvent;
import com.sk89q.worldedit.internal.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.regions.CylinderRegion;

import static com.google.common.base.Preconditions.checkNotNull;

public class CylinderCUIMultiRegion extends AbstractCUIMultiRegion {

    private final CylinderRegion region;

    public CylinderCUIMultiRegion(CylinderRegion region, MultiRegionStyle style) {
        super(style);
        checkNotNull(region);
        this.region = region;
    }

    public CylinderCUIMultiRegion(CylinderRegion region, MultiRegionStyle style, double gridSpacing, boolean gridCull) {
        super(style, gridSpacing, gridCull);
        checkNotNull(region);
        this.region = region;
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        super.describeCUI(session, player);
        session.dispatchClientCUIEvent(player,
                new WrappedMultiCUIEvent(new SelectionCylinderEvent(region.getCenter(), region.getRadius())),
                getProtocolVersion());
        session.dispatchClientCUIEvent(player,
                new WrappedMultiCUIEvent(new SelectionMinMaxEvent(region.getMinimumY(), region.getMaximumY())),
                getProtocolVersion());
    }

    @Override
    public String getTypeID() {
        return "cylinder";
    }
}
