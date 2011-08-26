package com.sk89q.worldedit.events;

import com.sk89q.worldedit.WorldEdit;


public interface WorldEditEvent {

    public enum Type {
        BLOCK_DESTROY,
        BLOCK_CREATE,
        REGION,
        GENERATE;
    }

    /**
     * Get the WorldEdit API from the plugin throwing the event.
     */
    public WorldEdit getWorldEdit();

    /**
     * Get the type of event.
     * @return
     */
    public Type getType();
}
