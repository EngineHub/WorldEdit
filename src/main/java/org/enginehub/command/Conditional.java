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
 * Sometimes a command may not need to be visible to a user, or a group of a commands
 * should not be visible as an option because none of its sub-commands are available,
 * and this interface provides the ability for calling code to query the visibility
 * of a given command.
 *
 * <p>An invisible command may or may not be callable, depending on the implementing
 * command handler. Do not implement this interface as a security measure, as it may not
 * even be utilized by all command handling code!</p>
 */
public interface Conditional {

    /**
     * Returns whether, given a context, this command should be visible.
     *
     * <p>A command may not be visible, for example, if the user is not authorized
     * to use it, which would provide for some very basic security through
     * obscurity, or simply to keep clutter down.</p>
     *
     * @param context the context
     * @return true if the command should be visible
     */
    boolean isVisible(CommandContext context);

}
