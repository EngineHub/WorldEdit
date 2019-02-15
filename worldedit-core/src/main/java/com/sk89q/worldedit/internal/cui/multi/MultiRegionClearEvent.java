package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.internal.cui.CUIEvent;

public class MultiRegionClearEvent implements CUIMultiEvent {
    private final String regionID;

    public MultiRegionClearEvent(String regionID) {
        this.regionID = regionID;
    }

    @Override
    public String getTypeId() {
        return "+s";
    }

    @Override
    public String[] getParameters() {
        return new String[] { "clear", regionID };
    }
}
