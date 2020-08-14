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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataReport implements Report {

    private final String title;
    private final List<Line> lines = Lists.newArrayList();

    public DataReport(String title) {
        checkNotNull(title, "title");
        this.title = title;
    }

    public void append(String key, String message) {
        checkNotNull(key, "key");
        lines.add(new Line(key, message));
    }

    public void append(String key, String message, Object... values) {
        checkNotNull(message, "values");
        checkNotNull(values, "values");
        append(key, String.format(message, values));
    }

    public void append(String key, byte value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, short value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, int value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, long value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, float value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, double value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, boolean value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, char value) {
        append(key, String.valueOf(value));
    }

    public void append(String key, Object value) {
        append(key, getStringValue(value, Sets.newHashSet()));
    }

    private static String getStringValue(Object value, Set<Object> seen) {
        if (seen.contains(value)) {
            return "<Recursive>";
        } else {
            seen.add(value);
        }

        if (value instanceof Object[]) {
            value = Arrays.asList(value);
        }

        if (value instanceof Collection<?>) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Object entry : (Collection<?>) value) {
                if (first) {
                    first = false;
                } else {
                    builder.append("\n");
                }
                builder.append(getStringValue(entry, Sets.newHashSet(seen)));
            }
            return builder.toString();
        } else if (value instanceof Map<?, ?>) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (first) {
                    first = false;
                } else {
                    builder.append("\n");
                }

                String key = getStringValue(entry.getKey(), Sets.newHashSet(seen)).replaceAll("[\r\n]", "");
                if (key.length() > 60) {
                    key = key.substring(0, 60) + "...";
                }

                builder
                        .append(key)
                        .append(": ")
                        .append(getStringValue(entry.getValue(), Sets.newHashSet(seen)));
            }
            return builder.toString();
        } else {
            return String.valueOf(value);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        if (!lines.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Line line : lines) {
                if (first) {
                    first = false;
                } else {
                    builder.append("\n");
                }
                builder.append(line.key).append(": ");
                if (line.value == null) {
                    builder.append("null");
                } else if (line.value.contains("\n")) {
                    builder.append("\n");
                    builder.append(line.value.replaceAll("(?m)^", "\t"));
                } else {
                    builder.append(line.value);
                }
            }
            return builder.toString();
        } else {
            return "No data.";
        }
    }

    private static class Line {
        private final String key;
        private final String value;

        public Line(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

}
