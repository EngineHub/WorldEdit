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

package org.enginehub.worldedit.operation;

import org.enginehub.worldedit.MaxChangedBlocksException;
import org.enginehub.worldedit.WorldEditException;


/**
 * Operation helper methods.
 */
public class OperationHelper {
    
    private OperationHelper() {
    }
    
    /**
     * Complete a given operation synchronously until it completes.
     * 
     * @param op operation to execute
     * @throws WorldEditException WorldEdit exception
     */
    public static void complete(Operation op) throws WorldEditException {
        Execution opt = new Execution();
        opt.setPreferSingleRun(true);
        
        complete(op, opt);
    }
    
    /**
     * Complete a given operation synchronously until it completes.
     * 
     * @param op operation to execute
     * @param opt execution hints
     * @throws WorldEditException WorldEdit exception
     */
    public static void complete(Operation op, Execution opt) throws WorldEditException {
        while (op != null) {
            op = op.resume(opt);
        }
    }
    
    /**
     * Complete a given operation synchronously until it completes. Catch all
     * errors that is not {@link MaxChangedBlocksException} for legacy reasons.
     * 
     * @param op operation to execute
     * @throws MaxChangedBlocksException thrown when too many blocks have been changed
     */
    public static void completeLegacy(Operation op) throws MaxChangedBlocksException {
        Execution opt = new Execution();
        opt.setPreferSingleRun(true);
        
        while (op != null) {
            try {
                op = op.resume(opt);
            } catch (MaxChangedBlocksException e) {
                throw e;
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
