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

package com.sk89q.worldedit.util.command;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Compares the primary aliases of two {@link CommandMapping} using
 * {@link String#compareTo(String)}.
 */
public final class PrimaryAliasComparator implements Comparator<CommandMapping> {

    /**
     * An instance of this class.
     */
    public static final PrimaryAliasComparator INSTANCE = new PrimaryAliasComparator(null);
    private final @Nullable Pattern removalPattern;

    /**
     * Create a new instance.
     *
     * @param removalPattern a regex to remove unwanted characters from the compared aliases
     */
    public PrimaryAliasComparator(@Nullable Pattern removalPattern) {
        this.removalPattern = removalPattern;
    }

    private String clean(String alias) {
        if (removalPattern != null) {
            return removalPattern.matcher(alias).replaceAll("");
        }
        return alias;
    }

    @Override
    public int compare(CommandMapping o1, CommandMapping o2) {
        return clean(o1.getPrimaryAlias()).compareTo(clean(o2.getPrimaryAlias()));
    }

}
