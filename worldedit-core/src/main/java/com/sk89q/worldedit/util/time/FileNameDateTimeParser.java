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

package com.sk89q.worldedit.util.time;

import com.google.common.collect.Streams;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.file.MorePaths;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Parses date-times by looking at the file name. File names without a time
 * will use 00:00:00.
 *
 * <p>
 * Elements may be separated by a space, dash, or colon.
 * The date and time may additionally be separated by a 'T'.
 * Only the year must have all digits, others may omit padding
 * zeroes.
 * </p>
 *
 * <p>
 * Valid file name examples:
 * <ul>
 *     <li>{@code 2019-06-15}</li>
 *     <li>{@code 2019-06-15 10:20:30}</li>
 *     <li>{@code 2019-06-15 10:20:30}</li>
 *     <li>{@code 2019-06-15T10:20:30}</li>
 *     <li>{@code 2019 06 15 10 20 30}</li>
 *     <li>{@code 2019-06-15-10-20-30}</li>
 *     <li>{@code 2019-6-1-1-2-3}</li>
 * </ul>
 * </p>
 */
public class FileNameDateTimeParser implements SnapshotDateTimeParser {

    private static final FileNameDateTimeParser INSTANCE = new FileNameDateTimeParser();

    public static FileNameDateTimeParser getInstance() {
        return INSTANCE;
    }

    private static final String SEP = "[ \\-_:]";

    private static final Pattern BASIC_FILTER = Pattern.compile(
        "^(?<year>\\d{4})" + SEP + "(?<month>\\d{1,2})" + SEP + "(?<day>\\d{1,2})" +
            // Optionally:
            "(?:" + "[ \\-_:T]" +
            "(?<hour>\\d{1,2})" + SEP + "(?<minute>\\d{1,2})" + SEP + "(?<second>\\d{1,2})" +
            ")?"
    );

    private FileNameDateTimeParser() {
    }

    @Nullable
    @Override
    public ZonedDateTime detectDateTime(Path path) {
        // Make this perform a little better:
        Matcher matcher = Streams.findLast(
            StreamSupport.stream(MorePaths.optimizedSpliterator(path), false)
                .map(p -> BASIC_FILTER.matcher(p.toString()))
                .filter(Matcher::find)
        ).orElse(null);
        if (matcher != null) {
            int year = matchAndParseOrZero(matcher, "year");
            int month = matchAndParseOrZero(matcher, "month");
            int day = matchAndParseOrZero(matcher, "day");
            int hour = matchAndParseOrZero(matcher, "hour");
            int minute = matchAndParseOrZero(matcher, "minute");
            int second = matchAndParseOrZero(matcher, "second");
            return ZonedDateTime.of(year, month, day, hour, minute, second,
                0, ZoneId.systemDefault());
        }
        return null;
    }

    private static int matchAndParseOrZero(Matcher matcher, String group) {
        String match = matcher.group(group);
        if (match == null) {
            return 0;
        }
        return Integer.parseInt(match);
    }

}
