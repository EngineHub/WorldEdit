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

/**
 * A proposal provided by a {@link SuggestionProvider}.
 */
public class Proposal {

    public enum Priority {
        /**
         * The suggestion is very likely. The UI may bold or highlight entries with
         * this priority.
         */
        VERY_LIKELY,

        /**
         * The suggestion is a normal priority which may not need to be bolded or
         * otherwise made to appear special. This it the default priority.
         */
        SOMEWHAT_LIKELY,

        /**
         * Possibly not what the user is looking for, but suggest it anyway. The UI
         * may choose to ignore these possibilities if it appears to be too cluttered.
         */
        CRAZY_IDEA
    }

    private String proposal;
    private boolean hint;
    private boolean replaceWord = false;
    private Priority priority = Priority.SOMEWHAT_LIKELY;

    /**
     * Create a new non-hint proposal.
     *
     * @param proposal the proposal string, which would be appended to the existing text
     */
    public Proposal(String proposal) {
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
    public Proposal(String proposal, boolean hint) {
        if (proposal == null) {
            throw new IllegalArgumentException("Null not allowed");
        }
        this.proposal = proposal;
        this.hint = hint;
    }

    /**
     * Get the proposal string.
     *
     * @return the proposal
     */
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
    public Proposal replaceWord() {
        setReplaceWord(true);
        return this;
    }

    /**
     * Get the priority of the suggestion. The priority determines the likelihood that
     * the proposal is what the user is looking for and may indicate to the UI to
     * display the proposal in a special format.
     *
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Set the priority.
     *
     * @param priority the priority
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * Set the priority.
     *
     * @param priority the priority
     * @return the same object (for convenience)
     */
    public Proposal priority(Priority priority) {
        setPriority(priority);
        return this;
    }

}
