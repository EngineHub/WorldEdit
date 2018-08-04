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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class RhinoScriptEngineFactory implements ScriptEngineFactory {
    private static List<String> names;
    private static List<String> mimeTypes;
    private static List<String> extensions;

    static {
        names = new ArrayList<>(5);
        names.add("ECMAScript");
        names.add("ecmascript");
        names.add("JavaScript");
        names.add("javascript");
        names.add("js");
        names = Collections.unmodifiableList(names);

        mimeTypes = new ArrayList<>(4);
        mimeTypes.add("application/ecmascript");
        mimeTypes.add("text/ecmascript");
        mimeTypes.add("application/javascript");
        mimeTypes.add("text/javascript");
        mimeTypes = Collections.unmodifiableList(mimeTypes);

        extensions = new ArrayList<>(2);
        extensions.add("emcascript");
        extensions.add("js");
        extensions = Collections.unmodifiableList(extensions);
    }

    @Override
    public String getEngineName() {
        return "Rhino JavaScript Engine (SK)";
    }

    @Override
    public String getEngineVersion() {
        return "unknown";
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public String getLanguageName() {
        return "EMCAScript";
    }

    @Override
    public String getLanguageVersion() {
        return "1.8";
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        StringBuilder s = new StringBuilder();
        s.append(obj);
        s.append(".");
        s.append(m);
        s.append("(");

        for (int i = 0; i < args.length; ++i) {
            s.append(args[i]);
            if (i < args.length - 1) {
                s.append(",");
            }
        }

        s.append(")");

        return s.toString();
    }

    @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getOutputStatement(String str) {
        return "print(" + str.replace("\\", "\\\\")
                .replace("\"", "\\\\\"")
                .replace(";", "\\\\;") + ")";
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.ENGINE:
                return getEngineName();
            case ScriptEngine.ENGINE_VERSION:
                return getEngineVersion();
            case ScriptEngine.NAME:
                return getEngineName();
            case ScriptEngine.LANGUAGE:
                return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION:
                return getLanguageVersion();
            case "THREADING":
                return "MULTITHREADED";
            default:
                throw new IllegalArgumentException("Invalid key");
        }
    }

    @Override
    public String getProgram(String... statements) {
        StringBuilder s = new StringBuilder();
        for (String stmt : statements) {
            s.append(stmt);
            s.append(";");
        }
        return s.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new RhinoScriptEngine();
    }

}
