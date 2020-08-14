/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.cui;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;

public interface CUIRegion {

    /**
     * Sends CUI events describing the region for
     * versions of CUI equal to or greater than the
     * value supplied by getProtocolVersion().
     *
     */
    void describeCUI(LocalSession session, Actor player);

    /**
     * Sends CUI events describing the region for
     * versions of CUI smaller than the value
     * supplied by getProtocolVersion().
     *
     */
    void describeLegacyCUI(LocalSession session, Actor player);

    /**
     * Returns the CUI version that is required to send
     * up-to-date data. If the CUI version is smaller than
     * this value, the legacy methods will be called.
     *
     * @return the protocol version
     */
    int getProtocolVersion();

    /**
     * Returns the type ID to send to CUI in the selection event.
     *
     * @return the type ID
     */
    String getTypeID();

    /**
     * Returns the type ID to send to CUI in the selection
     * event if the CUI is in legacy mode.
     *
     * @return the legacy type ID
     */
    String getLegacyTypeID();
}
