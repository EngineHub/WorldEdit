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

package com.sk89q.worldedit.operation;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;

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
     * @throws InterruptedException on interruption
     */
    public static void complete(Operation op) throws WorldEditException,
            InterruptedException {
        ImmutableHint opt = new ImmutableHint(Integer.MAX_VALUE, true);
        complete(op, opt);
    }
    
    /**
     * Complete a given operation synchronously until it completes.
     * 
     * @param op operation to execute
     * @param opt execution hints
     * @throws WorldEditException WorldEdit exception
     * @throws InterruptedException on interruption
     */
    public static void complete(Operation op, ExecutionHint opt) 
            throws WorldEditException, InterruptedException {
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
        ImmutableHint opt = new ImmutableHint(Integer.MAX_VALUE, true);
        
        while (op != null) {
            try {
                op = op.resume(opt);
            } catch (MaxChangedBlocksException e) {
                throw e;
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
            }
        }
    }

}
