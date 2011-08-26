package com.sk89q.worldedit.events;

import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;

public class WorldEditGenerationEvent implements WorldEditEvent {

    protected Set<Vector> blocks;
    protected WorldEdit we;
    protected EditSession editSession;
    protected LocalSession localSession;
    protected WorldEditEvent.Type type;

    public WorldEditGenerationEvent(Type type, Set<Vector> blocks, WorldEdit we, EditSession eS, LocalSession lS) {
        this.type = type;
        this.blocks = blocks;
        this.we = we;
        this.editSession = eS;
        this.localSession = lS;
    }

    public Set<Vector> getBlocks() {
        return blocks;
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
