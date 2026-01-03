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

package com.sk89q.bukkit.util;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates that certain specified classes came from the same source as
 * a plugin.
 */
public class ClassSourceValidator {

    private static final String SEPARATOR_LINE = "*".repeat(46);
    private static final Method loadClass;
    private static Class<?> pluginClassLoaderClass;

    static {
        Method tmp;
        try {
            pluginClassLoaderClass = Class.forName("org.bukkit.plugin.java.PluginClassLoader", false,
                    Bukkit.class.getClassLoader());
            tmp = pluginClassLoaderClass.getDeclaredMethod("loadClass0",
                    String.class, boolean.class, boolean.class, boolean.class);
            tmp.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            tmp = null;
        }
        loadClass = tmp;
    }

    private final Plugin plugin;
    @Nullable
    private final ClassLoader expectedClassLoader;

    /**
     * Create a new instance.
     *
     * @param plugin The plugin
     */
    public ClassSourceValidator(Plugin plugin) {
        checkNotNull(plugin, "plugin");
        this.plugin = plugin;
        this.expectedClassLoader = plugin.getClass().getClassLoader();
        if (loadClass == null) {
            plugin.getLogger().info("Bukkit PluginClassLoader seems to have changed. Class source validation will be skipped.");
        }
    }

    /**
     * Return a map of classes that been loaded from a different source.
     *
     * @param classes A list of classes to check
     * @return The results
     */
    public Map<Class<?>, Plugin> findMismatches(List<Class<?>> classes) {
        checkNotNull(classes, "classes");

        if (expectedClassLoader == null || loadClass == null) {
            return ImmutableMap.of();
        }

        Map<Class<?>, Plugin> mismatches = new HashMap<>();

        for (Plugin target : Bukkit.getPluginManager().getPlugins()) {
            if (target == plugin) {
                continue;
            }
            ClassLoader targetLoader = target.getClass().getClassLoader();
            if (!pluginClassLoaderClass.isAssignableFrom(targetLoader.getClass())) {
                continue;
            }
            for (Class<?> testClass : classes) {
                Class<?> targetClass;
                try {
                    targetClass = (Class<?>) loadClass.invoke(targetLoader, testClass.getName(), false, false, false);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    continue;
                }
                if (targetClass.getClassLoader() != expectedClassLoader) {
                    mismatches.putIfAbsent(testClass, targetClass.getClassLoader() == targetLoader ? target : null);
                }
            }
        }

        return ImmutableMap.copyOf(mismatches);
    }

    /**
     * Reports classes that have come from a different source.
     *
     * <p>The warning is emitted to the log.</p>
     *
     * @param classes The list of classes to check
     */
    public void reportMismatches(List<Class<?>> classes) {
        if (Boolean.getBoolean("enginehub.disable.class.source.validation")) {
            return;
        }
        Map<Class<?>, Plugin> mismatches = findMismatches(classes);

        if (mismatches.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder("\n");

        builder.append(SEPARATOR_LINE).append("\n");
        builder.append("** /!\\    SEVERE WARNING    /!\\\n");
        builder.append("** \n");
        builder.append("** A plugin developer has included a portion of \n");
        builder.append("** ").append(plugin.getName()).append(" into their own plugin, so rather than using\n");
        builder.append("** the version of ").append(plugin.getName()).append(" that you downloaded, you\n");
        builder.append("** will be using a broken mix of old ").append(plugin.getName()).append(" (that came\n");
        builder.append("** with the plugin) and your downloaded version. THIS MAY\n");
        builder.append("** SEVERELY BREAK ").append(plugin.getName().toUpperCase(Locale.ROOT)).append(" AND ALL OF ITS FEATURES.\n");
        builder.append("**\n");
        builder.append("** This may have happened because the developer is using\n");
        builder.append("** the ").append(plugin.getName()).append(" API and thinks that including\n");
        builder.append("** ").append(plugin.getName()).append(" is necessary. However, it is not!\n");
        builder.append("**\n");
        builder.append("** Here are some files that have been overridden:\n");
        builder.append("** \n");
        for (Map.Entry<Class<?>, Plugin> entry : mismatches.entrySet()) {
            Plugin badPlugin = entry.getValue();
            String url = badPlugin == null
                    ? "(unknown)"
                    : badPlugin.getName() + " (" + badPlugin.getClass().getProtectionDomain().getCodeSource().getLocation() + ")";
            builder.append("** '").append(entry.getKey().getSimpleName()).append("' came from '").append(url).append("'\n");
        }
        builder.append("**\n");
        builder.append("** Please report this to the plugins' developers.\n");
        builder.append(SEPARATOR_LINE).append("\n");

        plugin.getLogger().severe(builder.toString());
    }
}
