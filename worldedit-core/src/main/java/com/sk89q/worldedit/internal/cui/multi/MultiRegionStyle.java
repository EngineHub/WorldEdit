package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.internal.cui.CUIEvent;

public class MultiRegionStyle implements CUIEvent {

    private String gridColor;
    private String edgeColor;
    private String primaryPointColor;
    private String secondaryPointColor;

    public MultiRegionStyle(String gridColor, String edgeColor, String primaryPointColor, String secondaryPointColor) {
        this.gridColor = gridColor;
        this.edgeColor = edgeColor;
        this.primaryPointColor = primaryPointColor;
        this.secondaryPointColor = secondaryPointColor;
    }

    public String getGridColor() {
        return gridColor;
    }

    public void setGridColor(String gridColor) {
        this.gridColor = gridColor;
    }

    public String getEdgeColor() {
        return edgeColor;
    }

    public void setEdgeColor(String edgeColor) {
        this.edgeColor = edgeColor;
    }

    public String getPrimaryPointColor() {
        return primaryPointColor;
    }

    public void setPrimaryPointColor(String primaryPointColor) {
        this.primaryPointColor = primaryPointColor;
    }

    public String getSecondaryPointColor() {
        return secondaryPointColor;
    }

    public void setSecondaryPointColor(String secondaryPointColor) {
        this.secondaryPointColor = secondaryPointColor;
    }

    @Override
    public String getTypeId() {
        return "+col";
    }

    @Override
    public String[] getParameters() {
        return new String[] { gridColor, edgeColor, primaryPointColor, secondaryPointColor };
    }
}
