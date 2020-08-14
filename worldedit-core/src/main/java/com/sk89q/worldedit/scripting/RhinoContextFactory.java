/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.scripting;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

public class RhinoContextFactory extends ContextFactory {

    protected int timeLimit;

    public RhinoContextFactory(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    protected Context makeContext() {
        RhinoContext cx = new RhinoContext(this);
        try {
            // Try to set ES6 compat flag (since 1.7.7)
            Context.class.getDeclaredField("VERSION_ES6");
            cx.setLanguageVersion(RhinoContext.VERSION_ES6);
        } catch (NoSuchFieldException e) {
            // best we can do, compatible with 1.7R2 that many people probably use
            cx.setLanguageVersion(Context.VERSION_1_7);
        }
        cx.setInstructionObserverThreshold(10000);
        return cx;
    }

    @Override
    protected void observeInstructionCount(Context cx, int instructionCount) {
        RhinoContext mcx = (RhinoContext) cx;
        long currentTime = System.currentTimeMillis();

        if (currentTime - mcx.startTime > timeLimit) {
            throw new Error("Script timed out (" + timeLimit + "ms)");
        }
    }

    @Override
    protected Object doTopCall(Callable callable, Context cx, Scriptable scope,
            Scriptable thisObj, Object[] args) {
        RhinoContext mcx = (RhinoContext) cx;
        mcx.startTime = System.currentTimeMillis();

        return super.doTopCall(callable, cx, scope, thisObj, args);
    }

    private static class RhinoContext extends Context {
        long startTime;

        private RhinoContext(ContextFactory factory) {
            super(factory);
        }
    }

}
