package com.sk89q.worldedit.util.mask;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AllowedBlocksMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;

public class BlockMaskUtil {

    /**
     * Utility function to expand the masks of copying operations by the disallowed blocks list
     *
     * @param actor the actor issuing the copying operation
     * @param mask a potential mask already set by the actor
     * @param extent the extent
     */
    public static Mask checkForAllowedBlocksMask(Actor actor, Mask mask, Extent extent) {
        WorldEdit worldEdit = WorldEdit.getInstance();

        if (worldEdit.getConfiguration().disableDisallowedBlockCopying
                && actor != null
                && !actor.hasPermission("worldedit.anyblock")) {
            Mask allowedBlocksMask = new AllowedBlocksMask(worldEdit, extent);

            return (mask == null)
                    ? allowedBlocksMask
                    : new MaskIntersection(allowedBlocksMask, mask);
        }

        return mask;
    }

}