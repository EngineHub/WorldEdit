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

package com.sk89q.worldedit.bukkit.adapter;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.Closer;
import org.bukkit.Bukkit;

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
 * Loads Bukkit implementation adapters.
 */
public class BukkitImplLoader {

    private static final Logger log = Logger.getLogger(BukkitImplLoader.class.getCanonicalName());
    private final List<String> adapterCandidates = new ArrayList<String>();
    private String customCandidate;

    private static final String SEARCH_PACKAGE = "com.sk89q.worldedit.bukkit.adapter.impl";
    private static final String SEARCH_PACKAGE_DOT = SEARCH_PACKAGE + ".";
    private static final String SEARCH_PATH = SEARCH_PACKAGE.replace(".", "/");
    private static final String CLASS_SUFFIX = ".class";

    private static final String LOAD_ERROR_MESSAGE =
            "Failed to find an adapter for Bukkit!\n\n" +
            "This version of WorldEdit (%s) does not fully support your version of Bukkit (%s).\n\n" +
                    "What this means:\n" +
                    "1) Block operations will work, but chests will be empty, signs will be blank, and so on.\n" +
                    "2) You won't be able to save and load chests, signs, etc. with .schematic files.\n" +
                    "3) You won't be able to work with entities properly.\n" +
                    "4) Undo will will not be able to restore chests, signs, and etc.\n\n" +
                    "Possible solutions:\n" +
                    "1) If this is a new version of Minecraft, please wait for us to update. " +
                    "You can also put in a ticket at http://youtrack.sk89q.com (check for an existing ticket first).\n" +
                    "2) If you are using an older version of Minecraft, you may need to downgrade WorldEdit.\n" +
                    "3) If you are using an older version of WorldEdit, you may need to update your WorldEdit.\n" +
                    "4) If you are not using CraftBukkit, then report this issue to http://youtrack.sk89q.com " +
                    "(check for an existing ticket first).\n" +
                    "5) If you are developing WorldEdit, you can force an adapter with " +
                    "-Dworldedit.bukkit.adapter=the_class_name.\n\n" +
                    "Can I ignore this error? Yes! Just be aware of the undo issue.\n" +
                    "Am I using CraftBukkit? %s.\n";

    /**
     * Create a new instance.
     */
    public BukkitImplLoader() {
        addDefaults();
    }

    /**
     * Add default candidates, such as any defined with
     * {@code -Dworldedit.bukkit.adapter}.
     */
    private void addDefaults() {
        String className = System.getProperty("worldedit.bukkit.adapter");
        if (className != null) {
            customCandidate = className;
            adapterCandidates.add(className);
            log.log(Level.INFO, "-Dworldedit.bukkit.adapter used to add " + className + " to the list of available Bukkit adapters");
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

                if (!className.startsWith(SEARCH_PACKAGE_DOT) || jarEntry.isDirectory()) continue;

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
            adapterCandidates.add(className);
        }
    }

    /**
     * Iterate through the list of candidates and load an adapter.
     *
     * @return an adapter
     * @throws AdapterLoadException thrown if no adapter could be found
     */
    public BukkitImplAdapter loadAdapter() throws AdapterLoadException {
        for (String className : adapterCandidates) {
            try {
                Class<?> cls = Class.forName(className);
                if (BukkitImplAdapter.class.isAssignableFrom(cls)) {
                    return (BukkitImplAdapter) cls.newInstance();
                } else {
                    log.log(Level.WARNING, "Failed to load the Bukkit adapter class '" + className +
                            "' because it does not implement " + BukkitImplAdapter.class.getCanonicalName());
                }
            } catch (ClassNotFoundException e) {
                log.log(Level.WARNING, "Failed to load the Bukkit adapter class '" + className +
                        "' that is not supposed to be missing", e);
            } catch (IllegalAccessException e) {
                log.log(Level.WARNING, "Failed to load the Bukkit adapter class '" + className +
                        "' that is not supposed to be raising this error", e);
            } catch (Throwable e) {
                if (className.equals(customCandidate)) {
                    log.log(Level.WARNING, "Failed to load the Bukkit adapter class '" + className + "'", e);
                }
            }
        }

        String weVersion = WorldEdit.getVersion();
        String bukkitVersion = Bukkit.getBukkitVersion() + " implemented by " + Bukkit.getName() + " " + Bukkit.getVersion();
        String usingCraftBukkit =
                Bukkit.getName().equals("CraftBukkit")
                ? "Probably (if you got it from dl.bukkit.org, then yes)"
                : "No! You are using " + Bukkit.getName();

        throw new AdapterLoadException(
                String.format(LOAD_ERROR_MESSAGE, weVersion, bukkitVersion, usingCraftBukkit));
    }

}
