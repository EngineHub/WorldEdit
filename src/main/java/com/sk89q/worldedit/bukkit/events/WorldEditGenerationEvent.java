package com.sk89q.worldedit.bukkit.events;

import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSession.Shape;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditEvent;

public class WorldEditGenerationEvent implements WorldEditEvent {

    private Shape shape;
    private Location location;
    private WorldEdit we;
    private EditSession editSession;
    private LocalSession localSession;

    public WorldEditGenerationEvent(Shape shape, Location loc, WorldEdit we, EditSession eS, LocalSession lS) {
        this.shape = shape;
        this.location = loc;
        this.we = we;
        this.editSession = eS;
        this.localSession = lS;
    }

    public Shape getShape() {
        return shape;
    }

    public Location getLocation() {
        return location;
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
