package com.sk89q.worldedit.sponge.adapter;

import com.sk89q.worldedit.util.io.Closer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads Sponge implementation adapters.
 */
public class SpongeImplLoader {

    private static final Logger log = Logger.getLogger(SpongeImplLoader.class.getCanonicalName());
    private final List<String> adapterCandidates = new ArrayList<>();
    private String customCandidate;

    private static final String SEARCH_PACKAGE = "com.sk89q.worldedit.sponge.adapter.impl";
    private static final String SEARCH_PACKAGE_DOT = SEARCH_PACKAGE + ".";
    private static final String SEARCH_PATH = SEARCH_PACKAGE.replace(".", "/");
    private static final String CLASS_SUFFIX = ".class";

    private static final String LOAD_ERROR_MESSAGE =
            "\n**********************************************\n" +
                    "** This WorldEdit version does not support your version of Sponge.\n" +
                    "** WorldEdit will not function! \n" +
                    "** \n" +
                    "** Please ensure you are running the latest version\n" +
                    "**********************************************\n";

    /**
     * Create a new instance.
     */
    public SpongeImplLoader() {
        addDefaults();
    }

    /**
     * Add default candidates, such as any defined with
     * {@code -Dworldedit.sponge.adapter}.
     */
    private void addDefaults() {
        String className = System.getProperty("worldedit.sponge.adapter");
        if (className != null) {
            customCandidate = className;
            adapterCandidates.add(className);
            log.log(Level.INFO, "-Dworldedit.sponge.adapter used to add " + className + " to the list of available Sponge adapters");
        }
    }

    /**
     * Search the given JAR for candidate implementations.
     *
     * @param file the file
     * @throws IOException thrown on I/O error
     */
    public void addFromJar(File file) throws IOException {
        Closer closer = Closer.create();
        JarFile jar = closer.register(new JarFile(file));
        try {
            Enumeration entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();

                String className = jarEntry.getName().replaceAll("[/\\\\]+", ".");

                if (!className.startsWith(SEARCH_PACKAGE_DOT) || jarEntry.isDirectory() || className.contains("$")) continue;

                int beginIndex = 0;
                int endIndex = className.length() - CLASS_SUFFIX.length();
                className = className.substring(beginIndex, endIndex);
                adapterCandidates.add(className);
            }
        } finally {
            closer.close();
        }
    }

    /**
     * Search for classes stored as separate files available via the given
     * class loader.
     *
     * @param classLoader the class loader
     * @throws IOException thrown on error
     */
    public void addFromPath(ClassLoader classLoader) throws IOException {
        Enumeration<URL> resources = classLoader.getResources(SEARCH_PATH);
        while (resources.hasMoreElements()) {
            File file = new File(resources.nextElement().getFile());
            addFromPath(file);
        }
    }

    /**
     * Search for classes stored as separate files available via the given
     * path.
     *
     * @param file the path
     */
    private void addFromPath(File file) {
        String resource = SEARCH_PACKAGE_DOT + file.getName();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    addFromPath(child);
                }
            }
        } else if (resource.endsWith(CLASS_SUFFIX)) {
            int beginIndex = 0;
            int endIndex = resource.length() - CLASS_SUFFIX.length();
            String className = resource.substring(beginIndex, endIndex);
            if (!className.contains("$")) {
                adapterCandidates.add(className);
            }
        }
    }

    /**
     * Iterate through the list of candidates and load an adapter.
     *
     * @return an adapter
     * @throws AdapterLoadException thrown if no adapter could be found
     */
    public SpongeImplAdapter loadAdapter() throws AdapterLoadException {
        for (String className : adapterCandidates) {
            try {
                Class<?> cls = Class.forName(className);
                if (SpongeImplAdapter.class.isAssignableFrom(cls)) {
                    return (SpongeImplAdapter) cls.newInstance();
                } else {
                    log.log(Level.WARNING, "Failed to load the Sponge adapter class '" + className +
                            "' because it does not implement " + SpongeImplAdapter.class.getCanonicalName());
                }
            } catch (ClassNotFoundException e) {
                log.log(Level.WARNING, "Failed to load the Sponge adapter class '" + className +
                        "' that is not supposed to be missing", e);
            } catch (IllegalAccessException e) {
                log.log(Level.WARNING, "Failed to load the Sponge adapter class '" + className +
                        "' that is not supposed to be raising this error", e);
            } catch (Throwable e) {
                if (className.equals(customCandidate)) {
                    log.log(Level.WARNING, "Failed to load the Sponge adapter class '" + className + "'", e);
                }
            }
        }

        throw new AdapterLoadException(LOAD_ERROR_MESSAGE);
    }
}
