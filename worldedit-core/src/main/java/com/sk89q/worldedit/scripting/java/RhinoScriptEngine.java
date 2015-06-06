/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.scripting.java;

import java.io.IOException;
import java.io.Reader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import com.sk89q.worldedit.scripting.RhinoContextFactory;

public class RhinoScriptEngine extends AbstractScriptEngine {
    private ScriptEngineFactory factory;
    private Context cx;

    public RhinoScriptEngine() {
        RhinoContextFactory factory = new RhinoContextFactory(3000);
        factory.enterContext();
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public Object eval(String script, ScriptContext context)
            throws ScriptException {

        Scriptable scope = setupScope(cx, context);

        String filename = (filename = (String) get(ScriptEngine.FILENAME)) == null
                ? "<unknown>" : filename;

        try {
            return cx.evaluateString(scope, script, filename, 1, null);
        } catch (RhinoException e) {
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

    @Override
    public Object eval(Reader reader, ScriptContext context)
            throws ScriptException {

        Scriptable scope = setupScope(cx, context);

        String filename = (filename = (String) get(ScriptEngine.FILENAME)) == null
                ? "<unknown>" : filename;

        try {
            return cx.evaluateReader(scope, reader, filename, 1, null);
        } catch (RhinoException e) {
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
        } catch (IOException e) {
            throw new ScriptException(e);
        } finally {
            Context.exit();
        }
    }

    @Override
    public ScriptEngineFactory getFactory() {
        if (factory != null) {
            return factory;
        } else {
            return new RhinoScriptEngineFactory();
        }
    }

    private Scriptable setupScope(Context cx, ScriptContext context) {
        ScriptableObject scriptable = new ImporterTopLevel(cx);
        Scriptable scope = cx.initStandardObjects(scriptable);
        //ScriptableObject.putProperty(scope, "argv", Context.javaToJS(args, scope));
        return scope;
    }
}
