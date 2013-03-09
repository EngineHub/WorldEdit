// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.event;

/**
 * Priority constants.
 * 
 * <p>Priorities are simply integers, with {@link #DEFAULT} being at 0. Numbers other
 * than the ones available as constants in this class can be used, in case additional
 * flexibility is needed.</p>
 * 
 * <p>Lowest priorities are run first.</p>
 */
public class Priority {

    public static final int LOWEST = -2000;
    public static final int LOW = -1000;
    public static final int DEFAULT = 0;
    public static final int HIGH = 1000;
    public static final int HIGHEST = 2000;

    private Priority() {
    }

}
