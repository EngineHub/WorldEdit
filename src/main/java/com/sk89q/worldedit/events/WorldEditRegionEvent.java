package com.sk89q.worldedit.events;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;

public class WorldEditRegionEvent implements WorldEditEvent {

    private Region region;
    private WorldEdit we;
    private EditSession editSession;
    private LocalSession localSession;

    public WorldEditRegionEvent(Region region, WorldEdit we, EditSession eS, LocalSession lS) {
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

}
