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

package com.sk89q.worldedit.util.report;

import com.sk89q.worldedit.internal.util.LogManagerCompat;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;

public class ShallowObjectReport extends DataReport {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    public ShallowObjectReport(String title, Object object) {
        super(title);
        checkNotNull(object, "object");

        scanClass(object, object.getClass());
    }

    void scanClass(Object object, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (field.getAnnotation(Unreported.class) != null) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(object);
                append(field.getName(), String.valueOf(value));
            } catch (IllegalAccessException e) {
                LOGGER.warn("Failed to get value of '" + field.getName() + "' on " + type);
            }
        }
    }
}
