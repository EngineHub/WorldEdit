package com.sk89q.worldedit.cui;

import com.sk89q.worldedit.Vector;

public class SelectionEllipsoidPointEvent extends SelectionPointEvent {

    public SelectionEllipsoidPointEvent(int id, Vector pos, int area) {
        super(id, pos, area);
    }

    @Override
    public String getTypeId() {
        return "e";
    }
}
