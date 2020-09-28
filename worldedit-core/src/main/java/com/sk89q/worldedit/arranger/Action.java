package com.sk89q.worldedit.arranger;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.World;

/**
 * Represents an action that can be applied against a {@link World}.
 * Some common examples are block, biome, and entity placement.
 *
 * <p>
 * Note that an {@code Action} does not have a one-to-one correspondence with an
 * {@link ActionType}. A single action may result in multiple, more fine-grained actions
 * being reported by the returned {@link ActionReport}.
 * </p>
 */
public interface Action {
    /**
     * Apply this action to a world, and generate a report from that.
     *
     * @param world the world to apply to
     * @return a report of the types of actions performed
     */
    ActionReport apply(World world) throws WorldEditException;

    /**
     * Describes what this action does.
     *
     * @return a component describing this action
     */
    Component describe();
}
