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

package com.sk89q.worldedit.util.translation;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.renderer.TranslatableComponentRenderer;
import com.sk89q.worldedit.util.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;

/**
 * Handles translations for the plugin.
 *
 * <p>
 * These should be in the following format:
 * plugin.component.message[.meta]*
 * </p>
 *
 * <p>
 * Where,
 * plugin = worldedit
 * component = The part of the plugin, eg expand
 * message = A descriptor for which message, eg, expanded
 * meta = Any extra information such as plural/singular (Can have none to infinite)
 * </p>
 */
public class TranslationManager {

    private static final Gson gson = new GsonBuilder().create();
    private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    public static String makeTranslationKey(String type, String id) {
        String[] parts = id.split(":", 2);
        return type + '.' + parts[0] + '.' + parts[1].replace('/', '.');
    }

    private final Map<Locale, Map<String, String>> translationMap = new ConcurrentHashMap<>();
    private final TranslatableComponentRenderer<Locale> friendlyComponentRenderer = TranslatableComponentRenderer.from(
        (locale, key) -> {
            String translation = getTranslationMap(locale).get(key);
            if (translation == null) {
                // let it pass through (for e.g. MC messages)
                return null;
            }
            return new MessageFormat(translation, locale);
        }
    );
    private Locale defaultLocale = Locale.ENGLISH;

    private final ResourceLoader resourceLoader;

    private final Set<Locale> checkedLocales = new HashSet<>();

    public TranslationManager(ResourceLoader resourceLoader) {
        checkNotNull(resourceLoader);
        this.resourceLoader = resourceLoader;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    private Map<String, String> filterTranslations(Map<String, String> translations) {
        return translations.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().replace("'", "''")))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, String> parseTranslationFile(InputStream inputStream) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return filterTranslations(gson.fromJson(reader, STRING_MAP_TYPE));
        }
    }

    private Optional<Map<String, String>> loadTranslationFile(String filename) {
        Map<String, String> baseTranslations = new ConcurrentHashMap<>();

        try {
            URL resource = resourceLoader.getRootResource("lang/" + filename);
            if (resource != null) {
                try (InputStream stream = resource.openStream()) {
                    baseTranslations = parseTranslationFile(stream);
                }
            }
        } catch (IOException e) {
            // Seem to be missing base. If the user has provided a file use that.
        }

        Path localFile = resourceLoader.getLocalResource("lang/" + filename);
        if (Files.exists(localFile)) {
            try (InputStream stream = Files.newInputStream(localFile)) {
                baseTranslations.putAll(parseTranslationFile(stream));
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
        Map<String, String> baseTranslations = new ConcurrentHashMap<>();
        if (!locale.equals(defaultLocale)) {
            baseTranslations.putAll(getTranslationMap(defaultLocale));
        }
        Optional<Map<String, String>> langData = Optional.empty();
        if (!locale.getCountry().isEmpty()) {
            langData = loadTranslationFile(locale.getLanguage() + "-" + locale.getCountry() + "/strings.json");
        }
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
