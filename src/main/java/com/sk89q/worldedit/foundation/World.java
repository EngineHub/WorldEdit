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

package com.sk89q.worldedit.foundation;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.transaction.Transaction;

/**
 * Represents a world instance that can be modified. The world instance could be
 * loaded in-game or loaded in a stand-alone editor.
 * 
 * <p>This class is meant to replace {@link LocalWorld} eventually, once this class has been
 * fleshed out with the required methods and it has been decided that it is time to
 * start breaking some API compatibility.</p>
 */
public interface World extends Extent {
    
    /**
     * Create a new world changing transaction.
     * 
     * @return a new transaction
     */
    Transaction createTransaction();

}
