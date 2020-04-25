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

package com.sk89q.worldedit.scripting.compat;

import com.google.common.io.CharStreams;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

public class BabelScriptTranspiler implements ScriptTranspiler {

    private static final RemoteScript BABEL = new RemoteScript(
        "https://unpkg.com/@babel/standalone@7.9/babel.min.js",
        "babel.min.js",
        new RemoteScript(
            "https://unpkg.com/core-js-bundle@3.6.5/index.js",
            "core-js-bundle.js"
        ),
        new RemoteScript(
            "https://unpkg.com/regenerator-runtime@0.13.5/runtime.js",
            "regenerator-runtime.js"
        )
    );

    private final ContextFactory contextFactory = new ContextFactory() {
        @Override
        protected Context makeContext() {
            Context context = super.makeContext();
            context.setLanguageVersion(Context.VERSION_ES6);
            return context;
        }
    };
    private final Function executeBabel;

    public BabelScriptTranspiler() {
        Scriptable babel = BABEL.getScope();
        executeBabel = contextFactory.call(ctx -> {
            ctx.setOptimizationLevel(9);
            String execBabelSource = "function(source) {\n" +
                "return Babel.transform(source, { presets: ['env'] }).code;\n" +
                "}\n";
            return ctx.compileFunction(
                babel, execBabelSource, "<execute-babel>", 1, null
            );
        });
    }

    @Override
    public Reader transpile(Reader script) throws IOException {
        long startTranspile = System.nanoTime();
        Scriptable babel = BABEL.getScope();
        String source = CharStreams.toString(script);
        String result = (String) contextFactory.call(ctx ->
            executeBabel.call(ctx, babel, null, new Object[] { source })
        );
        System.err.println(result);
        System.err.println("Took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTranspile));
        return new StringReader(result);
    }
}
