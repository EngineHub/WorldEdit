package com.sk89q.worldedit.scripting.compat;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.net.HttpRequest;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.TopLevel;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class RemoteScript {

    private static final int MAX_REDIRECTS = 100;


    private final ContextFactory contextFactory = new ContextFactory() {
        @Override
        protected boolean hasFeature(Context cx, int featureIndex) {
            if (featureIndex == Context.FEATURE_OLD_UNDEF_NULL_THIS) {
                return true;
            }
            return super.hasFeature(cx, featureIndex);
        }

        @Override
        protected Context makeContext() {
            Context context = super.makeContext();
            context.setLanguageVersion(Context.VERSION_ES6);
            return context;
        }
    };
    private final Path cacheDir = WorldEdit.getInstance()
        .getWorkingDirectoryFile("craftscripts/.cache").toPath();
    private final URL source;
    private final String cacheFileName;
    private final Path cachePath;
    private final List<RemoteScript> dependencies;

    private volatile Scriptable cachedScope;

    public RemoteScript(String source, String cacheFileName, RemoteScript... dependencies) {
        this.source = HttpRequest.url(source);
        this.cacheFileName = cacheFileName;
        this.cachePath = cacheDir.resolve(cacheFileName);
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    private synchronized void ensureCached() throws IOException {
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
        if (!Files.exists(cachePath)) {
            boolean downloadedBabel = false;
            int redirects = 0;
            URL url = source;
            while (redirects < MAX_REDIRECTS && !downloadedBabel) {
                try (HttpRequest request = HttpRequest.get(url)) {
                    request.execute();
                    request.expectResponseCode(200, 301, 302);
                    if (request.getResponseCode() > 300) {
                        redirects++;
                        url = HttpRequest.url(request.getSingleHeaderValue("Location"));
                        continue;
                    }
                    request.saveContent(cachePath.toFile());
                    downloadedBabel = true;
                }
            }
            checkState(downloadedBabel, "Too many redirects following: %s", url);
            checkState(Files.exists(cachePath), "Failed to actually download %s", cacheFileName);
        }
    }

    protected synchronized void loadIntoScope(Context ctx, Scriptable scope) {
        try {
            ensureCached();
            try (Reader reader = Files.newBufferedReader(cachePath)) {
                ctx.evaluateReader(scope, reader, cacheFileName, 1, null);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Get a scope that the script has been evaluated in.
     *
     * @return the scope
     */
    public synchronized Scriptable getScope() {
        if (cachedScope != null) {
            return cachedScope;
        }

        // parse + execute standalone script to load it into the scope
        cachedScope = contextFactory.call(ctx -> {
            ScriptableObject scriptable = new TopLevel();
            Scriptable newScope = ctx.initStandardObjects(scriptable);
            ctx.setOptimizationLevel(9);
            for (RemoteScript dependency : dependencies) {
                dependency.loadIntoScope(ctx, newScope);
            }
            loadIntoScope(ctx, newScope);
            return newScope;
        });

        return cachedScope;
    }
}
