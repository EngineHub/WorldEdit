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

import static com.google.common.base.Preconditions.checkNotNull;

public class StackTraceReport implements Report {

    private final StackTraceElement[] stackTrace;

    public StackTraceReport(StackTraceElement[] stackTrace) {
        checkNotNull(stackTrace, "stackTrace");
        this.stackTrace = stackTrace;
    }

    @Override
    public String getTitle() {
        return "Stack Trace";
    }

    @Override
    public String toString() {
        if (stackTrace.length > 0) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (StackTraceElement element : stackTrace) {
                if (first) {
                    first = false;
                } else {
                    builder.append("\n");
                }
                builder.append(element.getClassName())
                        .append(".")
                        .append(element.getMethodName())
                        .append("() (")
                        .append(element.getFileName())
                        .append(":")
                        .append(element.getLineNumber())
                        .append(")");
            }
            return builder.toString();
        } else {
            return "No stack trace available.";
        }
    }

}
