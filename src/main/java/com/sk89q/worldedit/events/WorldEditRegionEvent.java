package com.sk89q.worldedit.events;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;

public class WorldEditRegionEvent implements WorldEditEvent {

    protected Region region;
    protected WorldEdit we;
    protected EditSession editSession;
    protected LocalSession localSession;
    protected WorldEditEvent.Type type;

    public WorldEditRegionEvent(Type type, Region region, WorldEdit we, EditSession eS, LocalSession lS) {
        this.type = type;
        this.region = region;
        this.we = we;
        this.editSession = eS;
        this.localSession = lS;
    }

    public Region getRegion() {
        return region;
    }

    public WorldEdit getWorldEdit() {
        return we;
    }

    public EditSession getEditSession() {
        return editSession;
    }

    public LocalSession getLocalSession() {
        return localSession;
    }

    public Type getType() {
        return type;
    }
}
