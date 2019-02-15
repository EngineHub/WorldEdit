package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;

public interface CUIMultiRegion extends CUIRegion {

    /**
     * Get the unique ID for this region.
     *
     * @return a uuid tied to this region
     */
    String getRegionID();

    /**
     * Removes the multi-region from the player's CUI.
     *
     * @param session the player's session
     * @param player the player to send the event to
     */
    default void clearRegion(LocalSession session, Actor player) {
        session.dispatchClientCUIEvent(player, new MultiRegionClearEvent(getRegionID()), getProtocolVersion());
    }

    @Override
    default int getProtocolVersion() {
        return 4;
    }

    @Override
    default void describeLegacyCUI(LocalSession session, Actor player) {
        throw new UnsupportedOperationException("Can't send multi-regions to legacy CUI.");
    }

    @Override
    default String getLegacyTypeID() {
        throw new UnsupportedOperationException("Can't send multi-regions to legacy CUI.");
    }
}
