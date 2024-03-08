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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

public record Placement(PlacementType placementType, BlockVector3 offset) {
    public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
        return placementType.getPlacementPosition(selector, actor).add(offset);
    }

    public boolean canBeUsedBy(Actor actor) {
        return placementType.canBeUsedBy(actor);
    }

    public Component getInfo() {
        if (offset.equals(BlockVector3.ZERO)) {
            return TranslatableComponent.of(placementType.getTranslationKey());
        } else {
            return TranslatableComponent.of(
                    placementType.getTranslationKeyWithOffset(),
                    TextComponent.of(offset.getX()),
                    TextComponent.of(offset.getY()),
                    TextComponent.of(offset.getZ())
            );
        }
    }
}
