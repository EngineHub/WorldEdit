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

package com.sk89q.worldedit.util.logging;

import java.io.UnsupportedEncodingException;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Nullable;

/**
 * A {@link StreamHandler} delegate that allows for the swap and disable of
 * another handler. When {@link #setHandler(StreamHandler)} is called with
 * null, then records passed onto this handler will be dropped. Otherwise,
 * the delegate handler will receive those records.
 */
public class DynamicStreamHandler extends StreamHandler {

    private @Nullable StreamHandler handler;
    private @Nullable Formatter formatter;
    private @Nullable Filter filter;
    private @Nullable String encoding;
    private Level level = Level.ALL;

    /**
     * Get the delegate handler.
     *
     * @return the delegate handler (Which may be null)
     */
    public @Nullable synchronized StreamHandler getHandler() {
        return handler;
    }

    /**
     * Set the handler.
     *
     * @param handler the delegate handler (which can be null)
     */
    public synchronized void setHandler(@Nullable StreamHandler handler) {
        if (this.handler != null) {
            this.handler.close();
        }

        this.handler = handler;

        if (handler != null) {
            handler.setFormatter(formatter);
            handler.setFilter(filter);
            try {
                handler.setEncoding(encoding);
            } catch (UnsupportedEncodingException ignored) {
            }
            handler.setLevel(level);
        }
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (handler != null) {
            handler.publish(record);
        }
    }

    @Override
    public synchronized void close() throws SecurityException {
        if (handler != null) {
            handler.close();
        }
    }

    @Override
    public void setEncoding(@Nullable String encoding) throws SecurityException, UnsupportedEncodingException {
        StreamHandler handler = this.handler;
        this.encoding = encoding;
        if (handler != null) {
            handler.setEncoding(encoding);
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        StreamHandler handler = this.handler;
        return handler != null && handler.isLoggable(record);
    }

    @Override
    public synchronized void flush() {
        StreamHandler handler = this.handler;
        if (handler != null) {
            handler.flush();
        }
    }

    @Override
    public void setFormatter(@Nullable Formatter newFormatter) throws SecurityException {
        StreamHandler handler = this.handler;
        this.formatter = newFormatter;
        if (handler != null) {
            handler.setFormatter(newFormatter);
        }
    }

    @Override
    public Formatter getFormatter() {
        StreamHandler handler = this.handler;
        Formatter formatter = this.formatter;
        if (handler != null) {
            return handler.getFormatter();
        } else if (formatter != null) {
            return formatter;
        } else {
            return new SimpleFormatter();
        }
    }

    @Override
    public String getEncoding() {
        StreamHandler handler = this.handler;
        String encoding = this.encoding;
        if (handler != null) {
            return handler.getEncoding();
        } else {
            return encoding;
        }
    }

    @Override
    public void setFilter(@Nullable Filter newFilter) throws SecurityException {
        StreamHandler handler = this.handler;
        this.filter = newFilter;
        if (handler != null) {
            handler.setFilter(newFilter);
        }
    }

    @Override
    public Filter getFilter() {
        StreamHandler handler = this.handler;
        Filter filter = this.filter;
        if (handler != null) {
            return handler.getFilter();
        } else {
            return filter;
        }
    }

    @Override
    public synchronized void setLevel(Level newLevel) throws SecurityException {
        if (handler != null) {
            handler.setLevel(newLevel);
        }
        this.level = newLevel;
    }

    @Override
    public synchronized Level getLevel() {
        if (handler != null) {
            return handler.getLevel();
        } else {
            return level;
        }
    }

}
