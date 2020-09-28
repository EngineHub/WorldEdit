package com.sk89q.worldedit.arranger;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;

/**
 * Represents an action with a position in block-units, but <strong>never</strong> fractional.
 */
public interface BlockPositionedAction extends PositionedAction {
    @Override
    default Vector3 getPosition() {
        return getBlockPosition().toVector3();
    }

    /**
     * Get the position of the action, in integer form.
     *
     * @return the position of the action
     */
    BlockVector3 getBlockPosition();
}
