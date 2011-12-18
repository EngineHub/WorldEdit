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

package com.sk89q.worldedit.scripting;

import java.util.Map;
import javax.script.ScriptException;
import org.mozilla.javascript.*;
import com.sk89q.worldedit.WorldEditException;

public class RhinoCraftScriptEngine implements CraftScriptEngine {
    private int timeLimit;

    public void setTimeLimit(int milliseconds) {
        timeLimit = milliseconds;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public Object evaluate(String script, String filename, Map<String, Object> args)
            throws ScriptException, Throwable {
        RhinoContextFactory factory = new RhinoContextFactory(timeLimit);
        Context cx = factory.enterContext();
        ScriptableObject scriptable = new ImporterTopLevel(cx);
        Scriptable scope = cx.initStandardObjects(scriptable);

        for (Map.Entry<String, Object> entry : args.entrySet()) {
            ScriptableObject.putProperty(scope, entry.getKey(),
                    Context.javaToJS(entry.getValue(), scope));
        }
        try {
            return cx.evaluateString(scope, script, filename, 1, null);
        } catch (Error e) {
            throw new ScriptException(e.getMessage());
        } catch (RhinoException e) {
            if (e instanceof WrappedException) {
                Throwable cause = ((WrappedException) e).getCause();
                if (cause instanceof WorldEditException) {
                    throw ((WrappedException) e).getCause();
                }
            }

            String msg;
            int line = (line = e.lineNumber()) == 0 ? -1 : line;

            if (e instanceof JavaScriptException) {
                msg = String.valueOf(((JavaScriptException) e).getValue());
            } else {
                msg = e.getMessage();
            }

            ScriptException scriptException =
                    new ScriptException(msg, e.sourceName(), line);
            scriptException.initCause(e);

            throw scriptException;
        } finally {
            Context.exit();
        }
    }

}
