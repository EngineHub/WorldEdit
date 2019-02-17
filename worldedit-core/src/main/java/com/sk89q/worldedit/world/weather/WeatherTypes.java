/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.weather;

import javax.annotation.Nullable;

public class WeatherTypes {

    static {
        // This isn't really a proper registry - so inject these before they're obtained.
        WeatherType.REGISTRY.register("clear", new WeatherType("clear"));
        WeatherType.REGISTRY.register("rain", new WeatherType("rain"));
        WeatherType.REGISTRY.register("thunder_storm", new WeatherType("thunder_storm"));
    }

    @Nullable public static final WeatherType CLEAR = get("clear");
    @Nullable public static final WeatherType RAIN = get("rain");
    @Nullable public static final WeatherType THUNDER_STORM = get("thunder_storm");

    private WeatherTypes() {
    }

    public static @Nullable WeatherType get(final String id) {
        return WeatherType.REGISTRY.get(id);
    }
}
