package com.sk89q.worldedit.events;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;

public class WorldEditBlockBreakEvent extends WorldEditBlockEvent {

    public WorldEditBlockBreakEvent(Vector vector, WorldEdit we, LocalSession lS) {
        super(Type.BLOCK_DESTROY, vector, we, lS);
    }

}
