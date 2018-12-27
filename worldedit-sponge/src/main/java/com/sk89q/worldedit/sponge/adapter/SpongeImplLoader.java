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

package com.sk89q.worldedit.sponge.adapter;

import com.google.common.collect.Lists;
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
            Enumeration<JarEntry> entries = jar.entries();
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
        List<SpongeImplAdapter> suitableAdapters = Lists.newArrayList();
        for (String className : adapterCandidates) {
            try {
                Class<?> cls = Class.forName(className);
                if (SpongeImplAdapter.class.isAssignableFrom(cls)) {
                    suitableAdapters.add((SpongeImplAdapter) cls.newInstance());
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

        if (suitableAdapters.isEmpty()) {
            throw new AdapterLoadException(LOAD_ERROR_MESSAGE);
        } else {
            if (suitableAdapters.size() == 1) {
                return suitableAdapters.get(0);
            } else {
                return suitableAdapters.stream().sorted((o1, o2) -> {
                    if (o1.isBest() && !o2.isBest()) {
                        return -1;
                    } else if (!o1.isBest() && o2.isBest()) {
                        return 1;
                    }
                    return 0;
                }).findFirst().orElse(suitableAdapters.get(0));
            }
        }
    }
}
