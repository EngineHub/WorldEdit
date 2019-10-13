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

package com.sk89q.worldedit.util.translation;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.renderer.FriendlyComponentRenderer;
import com.sk89q.worldedit.util.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Handles translations for the plugin.
 *
 * These should be in the following format:
 * plugin.component.message[.meta]*
 *
 * Where,
 * plugin = worldedit
 * component = The part of the plugin, eg expand
 * message = A descriptor for which message, eg, expanded
 * meta = Any extra information such as plural/singular (Can have none to infinite)
 */
public class TranslationManager {

    private static final Gson gson = new GsonBuilder().create();
    private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private final Map<Locale, Map<String, String>> translationMap = new HashMap<>();
    private final FriendlyComponentRenderer<Locale> friendlyComponentRenderer = FriendlyComponentRenderer.from(
            (locale, key) -> new MessageFormat(getTranslationMap(locale).getOrDefault(key, key), locale));
    private Locale defaultLocale = Locale.ENGLISH;

    private final WorldEdit worldEdit;

    private final Set<Locale> checkedLocales = new HashSet<>();

    public TranslationManager(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    private Map<String, String> parseTranslationFile(File file) throws IOException {
        return gson.fromJson(Files.toString(file, StandardCharsets.UTF_8), STRING_MAP_TYPE);
    }

    private Map<String, String> parseTranslationFile(URL file) throws IOException {
        return gson.fromJson(Resources.toString(file, StandardCharsets.UTF_8), STRING_MAP_TYPE);
    }

    private Optional<Map<String, String>> loadTranslationFile(String filename) {
        File localFile = worldEdit.getWorkingDirectoryFile("lang/" + filename);
        if (localFile.exists()) {
            try {
                return Optional.of(parseTranslationFile(localFile));
            } catch (IOException e) {
                return Optional.empty();
            }
        } else {
            try {
                return Optional.of(parseTranslationFile(ResourceLoader.getResourceRoot("lang/" + filename)));
            } catch (IOException e) {
                return Optional.empty();
            }
        }
    }

    private boolean tryLoadTranslations(Locale locale) {
        if (checkedLocales.contains(locale)) {
            return false;
        }
        checkedLocales.add(locale);
        Optional<Map<String, String>> langData = loadTranslationFile(locale.getLanguage() + "-" + locale.getCountry() + "/strings.json");
        if (!langData.isPresent()) {
            langData = loadTranslationFile(locale.getLanguage() + "/strings.json");
        }
        if (langData.isPresent()) {
            translationMap.put(locale, langData.get());
            return true;
        }
        if (locale.equals(defaultLocale)) {
            translationMap.put(Locale.ENGLISH, loadTranslationFile("strings.json").orElseThrow(
                    () -> new RuntimeException("Failed to load WorldEdit strings!")
            ));
            return true;
        }
        return false;
    }

    private Map<String, String> getTranslationMap(Locale locale) {
        Map<String, String> translations = translationMap.get(locale);
        if (translations == null) {
            if (tryLoadTranslations(locale)) {
                return getTranslationMap(locale);
            }
            if (!locale.equals(defaultLocale)) {
                translations = getTranslationMap(defaultLocale);
            }
        }

        return translations;
    }

    public Component convertText(Component component, Locale locale) {
        return friendlyComponentRenderer.render(component, locale);
    }
}
