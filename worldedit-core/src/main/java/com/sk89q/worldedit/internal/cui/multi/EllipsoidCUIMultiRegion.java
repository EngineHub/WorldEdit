package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.SelectionEllipsoidPointEvent;
import com.sk89q.worldedit.regions.EllipsoidRegion;

import static com.google.common.base.Preconditions.checkNotNull;

public class EllipsoidCUIMultiRegion extends AbstractCUIMultiRegion {

    private final EllipsoidRegion region;

    public EllipsoidCUIMultiRegion(EllipsoidRegion region, MultiRegionStyle style) {
        super(style);
        checkNotNull(region);
        this.region = region;
    }

    public EllipsoidCUIMultiRegion(EllipsoidRegion region, MultiRegionStyle style, double gridSpacing, boolean gridCull) {
        super(style, gridSpacing, gridCull);
        checkNotNull(region);
        this.region = region;
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        super.describeCUI(session, player);
        session.dispatchClientCUIEvent(player,
                new WrappedMultiCUIEvent(new SelectionEllipsoidPointEvent(0, region.getCenter())),
                getProtocolVersion());
        session.dispatchClientCUIEvent(player,
                new WrappedMultiCUIEvent(new SelectionEllipsoidPointEvent(1, region.getRadius())),
                getProtocolVersion());
    }

    @Override
    public String getTypeID() {
        return "ellipsoid";
    }
}
