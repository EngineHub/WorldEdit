package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.internal.cui.CUIEvent;

public class MultiRegionGridEvent implements CUIMultiEvent {
    private final double spacing;
    private final boolean cull;

    public MultiRegionGridEvent(double spacing, boolean cull) {
        this.spacing = spacing;
        this.cull = cull;
    }

    @Override
    public String getTypeId() {
        return "+grid";
    }

    @Override
    public String[] getParameters() {
        if (cull) {
            return new String[] {String.valueOf(spacing), "cull"};
        }
        return new String[] { String.valueOf(spacing)};
    }
}
