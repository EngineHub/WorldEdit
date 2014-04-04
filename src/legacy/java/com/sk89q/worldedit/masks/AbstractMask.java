package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

/**
 * @deprecated Switch to {@link com.sk89q.worldedit.function.mask.AbstractMask}
 */
@Deprecated
public abstract class AbstractMask implements Mask {
    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
    }

}
