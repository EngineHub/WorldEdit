// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Used for formatting.
 *
 * @author sk89q
 */
public class LogFormat extends Formatter {
    @Override
    public String format(LogRecord record) {
        StringBuilder text = new StringBuilder();
        Level level = record.getLevel();

        if (level == Level.FINEST) {
            text.append("[FINEST] ");
        } else if (level == Level.FINER) {
            text.append("[FINER] ");
        } else if (level == Level.FINE) {
            text.append("[FINE] ");
        } else if (level == Level.INFO) {
            text.append("[INFO] ");
        } else if (level == Level.WARNING) {
            text.append("[WARNING] ");
        } else if (level == Level.SEVERE) {
            text.append("[SEVERE] ");
        }

        text.append(record.getMessage());
        text.append("\r\n");

        Throwable t = record.getThrown();
        if (t != null) {
            StringWriter writer = new StringWriter();
            t.printStackTrace(new PrintWriter(writer));
            text.append(writer.toString());
        }

        return text.toString();
    }
}
