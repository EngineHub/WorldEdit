package com.sk89q.worldedit.events;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;

public class WorldEditBlockEvent implements WorldEditEvent {

    private WorldEdit we;
    private EditSession editSession;
    private LocalSession localSession;
    private Vector block;
    private boolean cancelled;

    public WorldEditBlockEvent(Vector block, WorldEdit we, EditSession eS, LocalSession lS) {
        this.we = we;
        this.editSession = eS;
        this.localSession = lS;
    }

    public Vector getBlock() {
        return block;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
