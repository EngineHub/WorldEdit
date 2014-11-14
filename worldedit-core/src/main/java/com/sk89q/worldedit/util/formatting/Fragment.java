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

package com.sk89q.worldedit.util.formatting;

/**
 * A fragment of text.
 */
public class Fragment {

    private final StringBuilder builder = new StringBuilder();
    
    Fragment() {
    }

    public Fragment append(String str) {
        builder.append(Style.stripColor(str));
        return this;
    }

    public Fragment append(Object obj) {
        append(String.valueOf(obj));
        return this;
    }

    public Fragment append(StringBuffer sb) {
        append(String.valueOf(sb));
        return this;
    }

    public Fragment append(CharSequence s) {
        append(String.valueOf(s));
        return this;
    }

    public Fragment append(boolean b) {
        append(String.valueOf(b));
        return this;
    }

    public Fragment append(char c) {
        append(String.valueOf(c));
        return this;
    }

    public Fragment append(int i) {
        append(String.valueOf(i));
        return this;
    }

    public Fragment append(long lng) {
        append(String.valueOf(lng));
        return this;
    }

    public Fragment append(float f) {
        append(String.valueOf(f));
        return this;
    }

    public Fragment append(double d) {
        append(String.valueOf(d));
        return this;
    }

    public Fragment newLine() {
        append("\n");
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
    
}
