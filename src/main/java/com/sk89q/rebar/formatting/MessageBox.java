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

package com.sk89q.rebar.formatting;

/**
 * Makes for a box with a border above and below.
 */
public class MessageBox extends StyledFragment {
    
    private static final String BORDER;
    
    private final StyledFragment contents = new StyledFragment();
    
    static {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            builder.append("\u2550");
        }
        BORDER = builder.toString();
    }

    /**
     * Create a new box.
     */
    public MessageBox() {
        append(BORDER);
        newLine();
        append(contents);
        newLine();
        append(BORDER);
    }

    /**
     * Get the internal contents.
     * 
     * @return the contents
     */
    public StyledFragment getContents() {
        return contents;
    }

}
