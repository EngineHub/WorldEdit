package com.sk89q.worldedit.events;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;

public class WorldEditBlockEvent implements WorldEditEvent {

    protected WorldEdit we;
    protected LocalSession localSession;
    protected Vector vector;
    protected boolean cancelled;
    protected WorldEditEvent.Type type;

    public WorldEditBlockEvent(Type type, Vector vector, WorldEdit we, LocalSession lS) {
        this.type = type;
        this.vector = vector;
        this.we = we;
        this.localSession = lS;
    }

    public Vector getVector() {
        return vector;
    }

    public WorldEdit getWorldEdit() {
        return we;
    }

    public LocalSession getLocalSession() {
        return localSession;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public Type getType() {
        return type;
    }

}
