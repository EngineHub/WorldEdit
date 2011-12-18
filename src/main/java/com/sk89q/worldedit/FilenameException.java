// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

public class FilenameException extends WorldEditException {
    private static final long serialVersionUID = 6072601657326106265L;

    private String filename;

    public FilenameException(String filename) {
        super();
        this.filename = filename;
    }

    public FilenameException(String filename, String msg) {
        super(msg);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
