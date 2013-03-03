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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command handling related utility methods.
 */
public final class CommandUtils {

    private static final Logger logger =
            Logger.getLogger(CommandUtils.class.getCanonicalName());

    private CommandUtils() {
    }

    /**
     * Checks if a command should be visible given a context. If the command does not
     * implement {@link Conditional}, then the command will be assumed visible.
     *
     * <p>All exceptions are caught and sent to the logger for this class.</p>
     *
     * @param command the command
     * @param context the context
     * @return true if the command should be visible
     */
    public static boolean isVisible(Command command, CommandContext context) {
        if (command instanceof Conditional) {
            try {
                return ((Conditional) command).isVisible(context);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Failed to call isVisible()", t);
                return true;
            }
        } else {
            return true;
        }
    }

}
