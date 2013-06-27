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

package com.sk89q.worldedit;

/**
 * Thrown on a runtime error caused by restrictions set on block placement or
 * other factors.
 * 
 * @see MaxChangedBlocksException
 */
public class EditException extends RuntimeException {

    private static final long serialVersionUID = -2132361734795604725L;
    
    public EditException() {
    }

    public EditException(String message) {
        super(message);
    }

    public EditException(Throwable cause) {
        super(cause);
    }

    public EditException(String message, Throwable cause) {
        super(message, cause);
    }

}
