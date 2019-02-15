package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.internal.cui.CUIEvent;

public class MultiRegionShapeEvent implements CUIMultiEvent {
    private final String regionID;
    private final String regionType;

    public MultiRegionShapeEvent(String regionID, String regionType) {
        this.regionID = regionID;
        this.regionType = regionType;
    }

    @Override
    public String getTypeId() {
        return "+s";
    }

    @Override
    public String[] getParameters() {
        return new String[] { regionType, regionID };
    }
}
