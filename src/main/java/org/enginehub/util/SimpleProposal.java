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
 * Holds a proposal.
 *
 * @param <T> the type of object being proposed
 */
public class SimpleProposal<T> implements Proposal<T> {
    
    private final T proposal;
    private final int confidence;

    /**
     * Store the given proposed response.
     * 
     * @param proposal the proposal
     * @param confidence the confidence
     */
    public SimpleProposal(T proposal, int confidence) {
        this.proposal = proposal;
        this.confidence = confidence;
    }

    @Override
    public int getConfidence() {
        return confidence;
    }

    @Override
    public T getProposal() {
        return proposal;
    }

}
