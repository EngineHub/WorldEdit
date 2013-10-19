package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;

public class RandomMask extends AbstractMask {
    private final double ratio;

    public RandomMask(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return Math.random() < ratio;
    }
}
