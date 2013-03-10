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

import org.enginehub.util.BestProposalRequest;
import org.enginehub.util.Proposal;

/**
 * A type of event that requests for a value.
 * 
 * <p>Multiple listeners can present a response, all providing a certain confidence
 * level, with the most confidence response being selected. If two responses share
 * the same confidence level, the later response (as determined by an event's
 * priority) will win.</p>
 * 
 * @param <T> the object being requested
 */
public abstract class Request<T> extends Event implements BestProposalRequest<Proposal<T>> {
    
    private Proposal<T> bestProposal;

    @Override
    public Proposal<T> getBestProposal() {
        return bestProposal;
    }

    @Override
    public void addProposal(Proposal<T> proposal) {
        if (bestProposal == null || 
                proposal.getConfidence() >= bestProposal.getConfidence()) {
            this.bestProposal = proposal;
        }
    }

}
