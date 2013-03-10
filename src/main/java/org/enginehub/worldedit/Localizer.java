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

package org.enginehub.worldedit;

import org.enginehub.i18n.LocalizerSystem;

/**
 * WorldEdit-specific localization.
 */
public final class Localizer {
    
    private static final String GROUP = "worldedit";

    private Localizer() {
    }

    /**
     * Get the translated form of a message.
     *
     * @param message the message used if a translation is not found
     * @param objects the list of objects to format the string with
     * @return the translated string
     */
    public synchronized static String _(String message, Object... objects) {
        return LocalizerSystem._(GROUP, message, objects);
    }

}
