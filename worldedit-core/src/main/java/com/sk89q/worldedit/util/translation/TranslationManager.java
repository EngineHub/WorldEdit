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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
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

    private Map<String, String> filterTranslations(Map<String, String> translations) {
        translations.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        return translations;
    }

    private Map<String, String> parseTranslationFile(InputStream inputStream) {
        return filterTranslations(gson.fromJson(new InputStreamReader(inputStream), STRING_MAP_TYPE));
    }

    private Optional<Map<String, String>> loadTranslationFile(String filename) {
        Map<String, String> baseTranslations;

        try {
            baseTranslations = parseTranslationFile(ResourceLoader.getResourceRoot("lang/" + filename).openStream());
        } catch (IOException e) {
            // Seem to be missing base. If the user has provided a file use that.
            baseTranslations = new HashMap<>();
        }

        File localFile = worldEdit.getWorkingDirectoryFile("lang/" + filename);
        if (localFile.exists()) {
            try {
                baseTranslations.putAll(parseTranslationFile(new FileInputStream(localFile)));
            } catch (IOException e) {
                // Failed to parse custom language file. Worth printing.
                e.printStackTrace();
            }
        }

        return baseTranslations.size() == 0 ? Optional.empty() : Optional.of(baseTranslations);
    }

    private boolean tryLoadTranslations(Locale locale) {
        if (checkedLocales.contains(locale)) {
            return false;
        }
        checkedLocales.add(locale);
        // Make a copy of the default language file
        Map<String, String> baseTranslations = new HashMap<>();
        if (!locale.equals(defaultLocale)) {
            baseTranslations.putAll(getTranslationMap(defaultLocale));
        }
        Optional<Map<String, String>> langData = loadTranslationFile(locale.getLanguage() + "-" + locale.getCountry() + "/strings.json");
        if (!langData.isPresent()) {
            langData = loadTranslationFile(locale.getLanguage() + "/strings.json");
        }
        if (langData.isPresent()) {
            baseTranslations.putAll(langData.get());
            translationMap.put(locale, baseTranslations);
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
