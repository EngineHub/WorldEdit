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

package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.generator.FeatureGenerator;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;

public final class FeatureGeneratorFactory implements Contextual<FeatureGenerator> {
    private final ConfiguredFeatureType type;

    public FeatureGeneratorFactory(ConfiguredFeatureType type) {
        this.type = type;
    }

    @Override
    public FeatureGenerator createFromContext(EditContext input) {
        return new FeatureGenerator((EditSession) input.getDestination(), type);
    }

    @Override
    public String toString() {
        return "feature of type " + type;
    }
}
