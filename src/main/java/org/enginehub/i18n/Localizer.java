// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.enginehub.i18n;

import java.io.Closeable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

/**
 * Handles the localization of strings in a way that is extremely convenient
 * for developers, and also allows for contextual locales for only portions of code.
 *
 * <p>This class is designed to globally be used in a static manner by several different
 * portions of code at the same time. To differentiate between projects or groups of
 * messages, bundles are separated by a group name. While this class is intended be used
 * statically, it can also be used as an instance, be utilized as an instance to set
 * the locale of a certain block of code.</p>
 *
 * <p>It is strongly recommended that projects have their own project-specific class
 * to translate messages, and a shortcut method such as "_" can be utilized to
 * reduce verbosity.</p>
 *
 * <p>Instances of this class are fundamentally not thread-safe. Do not ever use it
 * from multiple threads.</p>
 */
public class Localizer implements Closeable {

    /**
     * Stores the locale for the current context.
     */
    public static final ThreadLocal<Locale> localLocale = new ThreadLocal<Locale>() {
        @Override
        public Locale initialValue() {
            return Locale.US;
        }
    };

    private static WeakHashMap<String, ResourceBundle> bundles
            = new WeakHashMap<String, ResourceBundle>();

    private Locale lastLocale;
    private final Locale locale;

    /**
     * Create an instance where the current thread's locale is set to the given locale,
     * which can be undone by calling {@link #close()} (it is strongly recommended to
     * use this function within a try {} finally {} block.
     * @param locale the locale to use
     */
    public Localizer(Locale locale) {
        this.locale = locale;
        if (locale != null) {
            this.lastLocale = localLocale.get();
            localLocale.set(locale);
        }
    }

    /**
     * This must be called from the same thread where this object was made.
     */
    @Override
    public void close() {
        if (lastLocale == null) {
            localLocale.set(lastLocale);
            lastLocale = null;
        }
    }

    /**
     * Register a resource bundle with this class.
     *
     * <p>Please note that this class only keeps week references to bundles, and it is
     * imperative that you keep your own reference to your bundle or the garbage
     * collector will discard it.</p>
     *
     * @param group the group name (i.e. worldedit)
     * @param bundle the bundle to register
     */
    public synchronized static void register(String group, ResourceBundle bundle) {
        bundles.put(group, bundle);
    }

    /**
     * Get the translated form of a message.
     *
     * @param group the group
     * @param key the key of the message
     * @param message the message used if a translation is not found
     * @param objects the list of objects to format the string with
     * @return the translated string
     */
    private synchronized static String _(String group, String key,
                                         String message, Object ... objects) {
        ResourceBundle bundle = bundles.get(group);

        if (bundle != null) {
            try {
                message = bundle.getString(key);
            } catch (MissingResourceException e) {
                // Looks like the key is untranslated
            }
        }

        return String.format(message, objects);
    }

}
