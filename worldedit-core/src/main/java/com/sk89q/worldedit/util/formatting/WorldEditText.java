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

package com.sk89q.worldedit.util.formatting;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.enginehub.piston.config.ConfigHolder;
import org.enginehub.piston.config.ConfigRenderer;
import org.enginehub.piston.config.TextConfig;
import org.enginehub.piston.util.TextHelper;

import java.util.Locale;

public class WorldEditText {
    private static final ConfigRenderer RENDERER = ConfigRenderer.getInstance();
    public static final ConfigHolder CONFIG_HOLDER = ConfigHolder.create();

    static {
        CONFIG_HOLDER.getConfig(TextConfig.commandPrefix()).setValue("/");
    }

    public static Component format(Component component, Locale locale) {
        return WorldEdit.getInstance().getTranslationManager().convertText(
            RENDERER.render(component, CONFIG_HOLDER),
            locale
        );
    }

    public static String reduceToText(Component component, Locale locale) {
        return TextHelper.reduceToText(format(component, locale));
    }

    private WorldEditText() {
    }

}
