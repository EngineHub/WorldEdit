package com.sk89q.worldedit.masks;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 *
 * @author 1337
 */
public class UnderOverlayMask implements Mask {

    boolean overlay;
    Set<Integer> ids = new HashSet<Integer>();

    public UnderOverlayMask(Set<Integer> ids, boolean overlay) {
        addAll(ids);
        this.overlay = overlay;
    }

    public void addAll(Set<Integer> ids){
        this.ids.addAll(ids);
    }

    public boolean matches(EditSession editSession, Vector pos) {
        int id = editSession.getBlock(pos.setY(pos.getBlockY() + (overlay ? -1 : 1))).getType();
        return ids.isEmpty() ? id != BlockID.AIR : ids.contains(id);
    }

}

