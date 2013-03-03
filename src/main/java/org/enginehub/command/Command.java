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
 * A fully defined command with information about the command and a method of executing
 * the command.
 */
public interface Command extends CommandExecutor {

    /**
     * Get the friendly name for a command.
     *
     * <p>A friendly name would be in title case, and may be used when describing the
     * command in a list or in documentation. An example friendly name may be
     * "Stack Area." Friendly names are recommended, but are optional. If there is no
     * friendly name, the expected behavior is to find the name from another
     * source (such as the command's alias or its class canonical name).</p>
     *
     * @return a friendly name or null
     */
    String getName();

    /**
     * Get the aliases of the command, which are the names by which the command can be
     * called via command line or any other text-based interface.
     *
     * <p>Multiple aliases can be returned, but the first value is the "primary"
     * command. Implementations of command managers should attempt to expose all aliases
     * when possible.</p>
     *
     * @return the list of command aliases to use, which must be a non-zero length array
     */
    String[] getAliases();

    /**
     * Get a short one-liner description for the command, with no ending punctuation.
     *
     * <p>A description is optional, but it is highly recommended.</p>
     *
     * @return the description or null
     */
    String getDescription();

    /**
     * Get help text for the command, which may consist of one or more paragraphs if
     * necessary.
     *
     * <p>The displaying software must be able wrap the text and format it
     * appropriately. While recommended, the help text is optional.</p>
     *
     * @return the help text or null
     */
    String getHelp();

    /**
     * Get the usage description for the parameters of this command.
     *
     * <p>An example would be {@code <id> [<amount> [<direction>]]}. Brackets are
     * used to denote optional arguments. The return value of this method should not
     * be machine-validated and there is no guarantee that the value follows
     * the given format (but it is recommended).</p>
     *
     * @return usage string or null
     */
    String getUsage();

}
