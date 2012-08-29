package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

public class RandomMask implements Mask {

    private final double ratio;

    public RandomMask(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return Math.random() < ratio;
    }
}
