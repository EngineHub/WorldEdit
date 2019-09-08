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

package com.sk89q.worldedit.util.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.google.common.base.Preconditions.checkNotNull;

public class ShallowObjectReport extends DataReport {

    private static final Logger log = LoggerFactory.getLogger(ShallowObjectReport.class);

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
                log.warn("Failed to get value of '" + field.getName() + "' on " + type);
            }
        }
    }
}
