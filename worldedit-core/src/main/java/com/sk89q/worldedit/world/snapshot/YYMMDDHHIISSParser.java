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

package com.sk89q.worldedit.world.snapshot;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YYMMDDHHIISSParser implements SnapshotDateParser {

    private final Pattern datePattern =
            Pattern.compile("([0-9]+)[^0-9]?([0-9]+)[^0-9]?([0-9]+)[^0-9]?"
                    + "([0-9]+)[^0-9]?([0-9]+)[^0-9]?([0-9]+)(\\..*)?");

    @Override
    public Calendar detectDate(File file) {
        Matcher matcher = datePattern.matcher(file.getName());
        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            int hrs = Integer.parseInt(matcher.group(4));
            int min = Integer.parseInt(matcher.group(5));
            int sec = Integer.parseInt(matcher.group(6));
            Calendar calender = new GregorianCalendar();
            calender.set(year, month, day, hrs, min, sec);
            return calender;
        }
        return null;
    }

}
