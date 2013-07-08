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

package com.sk89q.rebar.command.binding;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.rebar.command.parametric.BindingBehavior;
import com.sk89q.rebar.command.parametric.BindingHelper;
import com.sk89q.rebar.command.parametric.BindingMatch;
import com.sk89q.rebar.command.parametric.ArgumentStack;

/**
 * Standard bindings that should be available to most configurations.
 */
public final class StandardBindings extends BindingHelper {

    /**
     * Gets a {@link CommandContext} from a {@link ArgumentStack}.
     * 
     * @param context the context
     * @return a selection
     */
    @BindingMatch(type = CommandContext.class,
                  behavior = BindingBehavior.PROVIDES)
    public CommandContext getCommandContext(ArgumentStack context) {
        context.markConsumed(); // Consume entire stack
        return context.getContext();
    }
    
}