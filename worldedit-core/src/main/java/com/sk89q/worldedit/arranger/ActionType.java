package com.sk89q.worldedit.arranger;

import com.sk89q.worldedit.util.formatting.text.Component;

/**
 * Represents the "type" of an action. This is a singleton key used to group the same type of
 * actions.
 */
public interface ActionType {
    /**
     * Describe a natural number of actions of this type.
     *
     * @param count the number of actions, between 0 and {@link Long#MAX_VALUE}.
     * @return a component describing the action(s)
     */
    Component describeBulk(long count);
}
