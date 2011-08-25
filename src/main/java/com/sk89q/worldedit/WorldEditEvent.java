package com.sk89q.worldedit;


public interface WorldEditEvent {

    /**
     * Get the WorldEdit API from the plugin throwing the event.
     */
    public WorldEdit getWorldEdit();
}
