package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.request.RequestSelection;

/**
 * @deprecated Use {@link RequestSelection} with {@link com.sk89q.worldedit.function.mask.RegionMask}
 */
@Deprecated
public class DynamicRegionMask extends AbstractMask {
    private Region region;

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
        try {
            region = session.getSelection(player.getWorld());
        } catch (IncompleteRegionException exc) {
            region = null;
        }
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return region == null || region.contains(pos);
    }
}
