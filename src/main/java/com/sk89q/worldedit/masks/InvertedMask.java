package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

public class InvertedMask implements Mask {

    private Mask mask;

    public InvertedMask(Mask mask) {
        this.mask = mask;
    }

    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
        mask.prepare(session, player, target);
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return !mask.matches(editSession, pos);
    }

    public Mask getInvertedMask() {
        return mask;
    }
}
