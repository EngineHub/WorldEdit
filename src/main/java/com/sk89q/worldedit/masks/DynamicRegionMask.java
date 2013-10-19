package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

public class DynamicRegionMask implements Mask {
    private Region region;

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
        try {
            region = session.getSelection(player.getWorld());
        } catch (IncompleteRegionException exc) {
            region = null;
        }
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return region == null || region.contains(pos);
    }
}
