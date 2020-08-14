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

package com.sk89q.worldedit.world.weather;

import javax.annotation.Nullable;

public final class WeatherTypes {

    public static final WeatherType CLEAR = register("clear");
    public static final WeatherType RAIN = register("rain");
    public static final WeatherType THUNDER_STORM = register("thunder_storm");

    private WeatherTypes() {
    }

    private static WeatherType register(String id) {
        return register(new WeatherType(id));
    }

    public static WeatherType register(WeatherType weather) {
        return WeatherType.REGISTRY.register(weather.getId(), weather);
    }

    @Nullable
    public static WeatherType get(final String id) {
        return WeatherType.REGISTRY.get(id);
    }
}
