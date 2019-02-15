package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractCUIMultiRegion implements CUIMultiRegion {

    private final String uuid;
    private final MultiRegionStyle style;
    private boolean gridSet;
    private double spacing;
    private boolean cull;

    protected AbstractCUIMultiRegion(MultiRegionStyle style) {
        this.uuid = UUID.randomUUID().toString();
        this.style = style;
    }

    protected AbstractCUIMultiRegion(MultiRegionStyle style, double gridSpacing, boolean gridCull) {
        this(style);
        this.gridSet = true;
        this.spacing = gridSpacing;
        this.cull = gridCull;
    }

    @Override
    public String getRegionID() {
        return uuid;
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        checkNotNull(session);
        checkNotNull(player);
        session.dispatchClientCUIEvent(player, new MultiRegionShapeEvent(getRegionID(), getTypeID()), getProtocolVersion());
        if (style != null) {
            session.dispatchClientCUIEvent(player, style, getProtocolVersion());
        }
        if (gridSet) {
            session.dispatchClientCUIEvent(player, new MultiRegionGridEvent(spacing, cull), getProtocolVersion());
        }
    }

    @Override
    public abstract String getTypeID();

}
