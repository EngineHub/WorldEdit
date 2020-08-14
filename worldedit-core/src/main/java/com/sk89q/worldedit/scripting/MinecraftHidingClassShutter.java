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

package com.sk89q.worldedit.scripting;

import org.mozilla.javascript.ClassShutter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hides Minecraft's obfuscated & de-obfuscated names from scripts.
 */
class MinecraftHidingClassShutter implements ClassShutter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftHidingClassShutter.class);

    @Override
    public boolean visibleToScripts(String fullClassName) {
        if (!fullClassName.contains(".")) {
            // Default package -- probably Minecraft
            return false;
        }
        return !fullClassName.startsWith("net.minecraft");
    }
}
