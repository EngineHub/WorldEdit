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

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;

import static com.google.common.base.Preconditions.checkNotNull;

public enum PlacementType {
    WORLD("worldedit.toggleplace.world", "worldedit.toggleplace.world-offset") {
        @Override
        public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
            return BlockVector3.ZERO;
        }
    },

    PLAYER("worldedit.toggleplace.player", "worldedit.toggleplace.player-offset") {
        @Override
        public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
            if (!canBeUsedBy(actor)) {
                throw new IncompleteRegionException();
            }
            return ((Locatable) actor).getBlockLocation().toVector().toBlockPoint();
        }

        @Override
        public boolean canBeUsedBy(Actor actor) {
            checkNotNull(actor);
            return actor instanceof Locatable;
        }
    },

    HERE(null, null) {
        @Override
        public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
            throw new IllegalStateException("PlacementType.HERE cannot be used. Use PLAYER or WORLD instead.");
        }

        @Override
        public boolean canBeUsedBy(Actor actor) {
            return PLAYER.canBeUsedBy(actor);
        }

        @Override
        public String getTranslationKey() {
            throw new IllegalStateException("PlacementType.HERE cannot be used. Use PLAYER or WORLD instead.");
        }

        @Override
        public String getTranslationKeyWithOffset() {
            throw new IllegalStateException("PlacementType.HERE cannot be used. Use PLAYER or WORLD instead.");
        }
    },

    POS1("worldedit.toggleplace.pos1", "worldedit.toggleplace.pos1-offset") {
        @Override
        public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
            return selector.getPrimaryPosition();
        }
    },

    MIN("worldedit.toggleplace.min", "worldedit.toggleplace.min-offset") {
        @Override
        public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
            return selector.getRegion().getMinimumPoint();
        }
    },

    MAX("worldedit.toggleplace.max", "worldedit.toggleplace.max-offset") {
        @Override
        public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
            return selector.getRegion().getMaximumPoint();
        }
    };

    private final String translationKey;
    private final String translationKeyWithOffset;

    PlacementType(String translationKey, String translationKeyWithOffset) {
        this.translationKey = translationKey;
        this.translationKeyWithOffset = translationKeyWithOffset;
    }

    public abstract BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException;

    public boolean canBeUsedBy(Actor actor) {
        return true;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getTranslationKeyWithOffset() {
        return translationKeyWithOffset;
    }
}
