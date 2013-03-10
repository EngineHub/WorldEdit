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

package org.enginehub.command;

import org.enginehub.util.Proposal;

/**
 * A proposal provided by a {@link SuggestionProvider}.
 */
public class Suggestion implements Proposal<String> {
    
    private String proposal;
    private boolean hint;
    private boolean replaceWord = false;
    private int confidence = DEFAULT;

    /**
     * Create a new non-hint proposal.
     *
     * @param proposal the proposal string, which would be appended to the existing text
     */
    public Suggestion(String proposal) {
        if (proposal == null) {
            throw new IllegalArgumentException("Null not allowed");
        }
        this.proposal = proposal;
        this.hint = false;
    }

    /**
     * Create a new proposal.
     *
     * @param proposal the proposal string, which would be appended to the existing text
     * @param hint true to indicate a hint
     */
    public Suggestion(String proposal, boolean hint) {
        if (proposal == null) {
            throw new IllegalArgumentException("Null not allowed");
        }
        this.proposal = proposal;
        this.hint = hint;
    }

    @Override
    public String getProposal() {
        return proposal;
    }

    /**
     * Set the proposal string.
     *
     * @param proposal the proposal
     */
    public void setProposal(String proposal) {
        this.proposal = proposal;
    }

    /**
     * Gets whether the proposal is a hint.
     *
     * <p>Hints are not to be literally typed, and merely serve as documentation.
     * A hint may be {@code <name>} for example, while a non-hint proposal
     * would be {@code Eduardo}.</p>
     *
     * @return true if a hint
     */
    public boolean isHint() {
        return hint;
    }

    /**
     * Set whether the proposal is a hint.
     *
     * @param hint true if a hint
     */
    public void setHint(boolean hint) {
        this.hint = hint;
    }

    /**
     * Get whether the entire word should be replaced with the propsal.
     *
     * @return true if replacing
     */
    public boolean getReplaceWord() {
        return replaceWord;
    }

    /**
     * Set whether the word should be replaced with the proposal, rather than simply
     * "appended."
     *
     * @param replaceWord true to replace the entire world
     */
    public void setReplaceWord(boolean replaceWord) {
        this.replaceWord = replaceWord;
    }

    /**
     * Set the proposal to replace the entire word.
     *
     * @return the same object (for convenience)
     */
    public Suggestion replaceWord() {
        setReplaceWord(true);
        return this;
    }

    @Override
    public int getConfidence() {
        return confidence;
    }

    /**
     * Set the confidence.
     *
     * @param confidence the confidence
     */
    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    /**
     * Set the confidence.
     *
     * @param confidence the confidence
     * @return the same object (for convenience)
     */
    public Suggestion confidence(int confidence) {
        setConfidence(confidence);
        return this;
    }

}
