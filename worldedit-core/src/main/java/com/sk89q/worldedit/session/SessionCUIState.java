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

package com.sk89q.worldedit.session;

import com.sk89q.worldedit.math.BlockVector3;

import javax.annotation.Nullable;

/**
 * Holds CUI (CUINetworking)-related state for a session.
 * Extracted from LocalSession to separate concerns and allow move of CUI fields.
 */
public final class SessionCUIState {

    public static final int CUI_VERSION_UNINITIALIZED = -1;

    private int failedCuiAttempts = 0;
    private boolean hasCUISupport = false;
    private int cuiVersion = CUI_VERSION_UNINITIALIZED;
    @Nullable
    private BlockVector3 serverCuiStructureBlockPosition;

    public int getFailedCuiAttempts() {
        return failedCuiAttempts;
    }

    public void setFailedCuiAttempts(int failedCuiAttempts) {
        this.failedCuiAttempts = failedCuiAttempts;
    }

    public void incrementFailedCuiAttempts() {
        this.failedCuiAttempts++;
    }

    public boolean hasCUISupport() {
        return hasCUISupport;
    }

    public void setCUISupport(boolean support) {
        this.hasCUISupport = support;
    }

    public int getCUIVersion() {
        return cuiVersion;
    }

    public void setCUIVersion(int cuiVersion) {
        if (cuiVersion < 0) {
            throw new IllegalArgumentException("CUI protocol version must be non-negative, but '" + cuiVersion + "' was received.");
        }
        this.cuiVersion = cuiVersion;
    }

    @Nullable
    public BlockVector3 getServerCuiStructureBlockPosition() {
        return serverCuiStructureBlockPosition;
    }

    public void setServerCuiStructureBlockPosition(@Nullable BlockVector3 position) {
        this.serverCuiStructureBlockPosition = position;
    }

    /**
     * Reset CUI state when the session becomes idle.
     */
    public void reset() {
        this.cuiVersion = CUI_VERSION_UNINITIALIZED;
        this.hasCUISupport = false;
        this.failedCuiAttempts = 0;
    }
}
