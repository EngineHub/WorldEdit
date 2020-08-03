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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.CodeSource;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassSourceValidator.class);
    private static final String SEPARATOR_LINE = Strings.repeat("*", 46);

    private final Plugin plugin;
    @Nullable
    private final CodeSource expectedCodeSource;

    /**
     * Create a new instance.
     *
     * @param plugin The plugin
     */
    public ClassSourceValidator(Plugin plugin) {
        checkNotNull(plugin, "plugin");
        this.plugin = plugin;
        this.expectedCodeSource = plugin.getClass().getProtectionDomain().getCodeSource();
    }

    /**
     * Return a map of classes that been loaded from a different source.
     *
     * @param classes A list of classes to check
     * @return The results
     */
    public Map<Class<?>, CodeSource> findMismatches(List<Class<?>> classes) {
        checkNotNull(classes, "classes");

        if (expectedCodeSource == null) {
            return ImmutableMap.of();
        }

        Map<Class<?>, CodeSource> mismatches = new HashMap<>();

        for (Class<?> testClass : classes) {
            CodeSource testSource = testClass.getProtectionDomain().getCodeSource();
            if (!expectedCodeSource.equals(testSource)) {
                mismatches.put(testClass, testSource);
            }
        }

        return mismatches;
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
        Map<Class<?>, CodeSource> mismatches = findMismatches(classes);

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
        for (Map.Entry<Class<?>, CodeSource> entry : mismatches.entrySet()) {
            CodeSource codeSource = entry.getValue();
            String url = codeSource != null ? codeSource.getLocation().toExternalForm() : "(unknown)";
            builder.append("** '").append(entry.getKey().getSimpleName()).append("' came from '").append(url).append("'\n");
        }
        builder.append("**\n");
        builder.append("** Please report this to the plugins' developers.\n");
        builder.append(SEPARATOR_LINE).append("\n");

        LOGGER.error(builder.toString());
    }
}
