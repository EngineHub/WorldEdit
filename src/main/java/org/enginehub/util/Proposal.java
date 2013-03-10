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

package org.enginehub.util;

/**
 * A possible response.
 * 
 * @param <T> the type of response being proposed
 */
public interface Proposal<T> {

    /**
     * The suggestion is very likely. The UI may bold or highlight entries with
     * this priority or higher.
     */
    static final int HIGH_CONFIDENCE = 2000;

    /**
     * The suggestion is a normal priority which may not need to be bolded or
     * otherwise made to appear special. This it the default priority.
     */
    static final int DEFAULT = 1000;

    /**
     * Possibly not what the user is looking for, but suggest it anyway. The UI
     * may choose to ignore these possibilities if it appears to be too cluttered.
     */
    static final int LOW_CONFIDENCE = 0;
    
    /**
     * Get the confidence.
     * 
     * <p>See the constants on this class.</p>
     * 
     * @return the confidence
     */
    int getConfidence();
    
    /**
     * Get the proposal.
     * 
     * @return the proposal
     */
    T getProposal();

}
