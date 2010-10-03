// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

import org.mozilla.javascript.*;

/**
 * Context factory for the JavaScript engine.
 * 
 * @author Albert
 */
public class ScriptContextFactory extends ContextFactory {
    /**
     * Context that will be used to store start time.
     */
    private static class ScriptContext extends Context {
        long startTime;
    }
    
    static {
        ContextFactory.initGlobal(new ScriptContextFactory());
    }

    @Override
    protected Context makeContext()
    {
        ScriptContext ctx = new ScriptContext();
        ctx.setInstructionObserverThreshold(10000);
        return ctx;
    }

    @Override
    protected void observeInstructionCount(Context ctx, int instructionCount)
    {
        ScriptContext sctx = (ScriptContext)ctx;
        long currentTime = System.currentTimeMillis();
        if (currentTime - sctx.startTime > 3 * 1000) {
            throw new Error("Exceeded 3 seconds");
        }
    }

    @Override
    protected Object doTopCall(Callable callable,
            Context ctx, Scriptable scope,
            Scriptable thisObj, Object[] args)
    {
        ScriptContext sctx = (ScriptContext)ctx;
        sctx.startTime = System.currentTimeMillis();
        return super.doTopCall(callable, ctx, scope, thisObj, args);
    }

}
