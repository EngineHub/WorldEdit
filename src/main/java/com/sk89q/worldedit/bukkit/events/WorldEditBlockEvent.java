package com.sk89q.worldedit.bukkit.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockEvent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditEvent;

public class WorldEditBlockEvent extends BlockEvent implements WorldEditEvent, Cancellable {

    private WorldEdit we;
    private EditSession editSession;
    private LocalSession localSession;
    private boolean cancelled;

    public WorldEditBlockEvent(Type type, Block block, WorldEdit we, EditSession eS, LocalSession lS) {
        super(type, block);
        this.we = we;
        this.editSession = eS;
        this.localSession = lS;
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
