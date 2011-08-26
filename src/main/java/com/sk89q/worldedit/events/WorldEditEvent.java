package com.sk89q.worldedit.events;

import com.sk89q.worldedit.WorldEdit;


public interface WorldEditEvent {

    /**
     * Get the WorldEdit API from the plugin throwing the event.
     */
    public WorldEdit getWorldEdit();
}
