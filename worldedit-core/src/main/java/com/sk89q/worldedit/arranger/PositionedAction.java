package com.sk89q.worldedit.arranger;

import com.sk89q.worldedit.math.Vector3;

/**
 * Represents an action with a position in block-units, but potentially fractional.
 */
public interface PositionedAction extends Action {
    /**
     * Get the position of the action.
     *
     * @return the position of the action
     */
    Vector3 getPosition();
}
