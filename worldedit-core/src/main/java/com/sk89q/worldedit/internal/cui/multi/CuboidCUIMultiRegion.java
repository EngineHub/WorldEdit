package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.regions.CuboidRegion;

import static com.google.common.base.Preconditions.checkNotNull;

public class CuboidCUIMultiRegion extends AbstractCUIMultiRegion {

    private final CuboidRegion region;

    public CuboidCUIMultiRegion(CuboidRegion region, MultiRegionStyle style) {
        super(style);
        checkNotNull(region);
        this.region = region;
    }

    public CuboidCUIMultiRegion(CuboidRegion region, MultiRegionStyle style, double gridSpacing, boolean gridCull) {
        super(style, gridSpacing, gridCull);
        checkNotNull(region);
        this.region = region;
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        super.describeCUI(session, player);
        if (region.getPos1() != null) {
            session.dispatchClientCUIEvent(player,
                    new WrappedMultiCUIEvent(new SelectionPointEvent(0, region.getPos1(), 0)),
                    getProtocolVersion());
        }
        if (region.getPos2() != null) {
            session.dispatchClientCUIEvent(player,
                    new WrappedMultiCUIEvent(new SelectionPointEvent(1, region.getPos2(), region.getArea())),
                    getProtocolVersion());
        }
    }

    @Override
    public String getTypeID() {
        return "cuboid";
    }
}
