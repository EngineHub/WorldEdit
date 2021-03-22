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

package com.sk89q.worldedit.internal.util;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LogManagerCompat {

    public static Logger getLogger() {
        return LogManager.getLogger(getCallerCallerClassName());
    }

    private static String getCallerCallerClassName() {
        List<StackTraceElement> lazyStack = Throwables.lazyStackTrace(new Throwable());
        // 0 - this method
        // 1 - caller
        // 2 - caller caller
        return lazyStack.get(2).getClassName();
    }

    private LogManagerCompat() {
    }
}
