/*
 * This file is part of text-extras, licensed under the MIT License.
 *
 * Copyright (c) 2018-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;


@SuppressWarnings({"deprecation", "UnstableApiUsage", "unchecked"})
final class SpigotAdapter implements Adapter {
    private static final boolean BOUND = bind();

    private static boolean bind() {
        try {
            final Field factoriesField = field(Gson.class, "factories");
            final Field builderFactoriesField = field(GsonBuilder.class, "factories");
            final Field builderHierarchyFactoriesField = field(GsonBuilder.class, "hierarchyFactories");

            // WorldEdit start - use different gson depending on MC version
            final Gson gson;
            Gson tmpGson;
            try {
                final Field gsonField = field(ComponentSerializer.class, "gson");
                tmpGson = (Gson) gsonField.get(null);
            } catch (NoSuchFieldException ignored) {
                tmpGson = VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5).getGson();
            }
            gson = tmpGson;
            // WorldEdit end

            final GsonBuilder builder = GsonComponentSerializer.populate(new GsonBuilder());

            final List<TypeAdapterFactory> existingFactories = (List<TypeAdapterFactory>) factoriesField.get(gson);
            final List<TypeAdapterFactory> newFactories = new ArrayList<>();
            newFactories.addAll((List<TypeAdapterFactory>) builderFactoriesField.get(builder));
            Collections.reverse(newFactories);
            newFactories.addAll((List<TypeAdapterFactory>) builderHierarchyFactoriesField.get(builder));

            final List<TypeAdapterFactory> modifiedFactories = new ArrayList<>(existingFactories);

            // the excluder must precede all adapters that handle user-defined types
            final int index = findExcluderIndex(modifiedFactories);

            for (final TypeAdapterFactory newFactory : Lists.reverse(newFactories)) {
                modifiedFactories.add(index, newFactory);
            }

            Class<?> treeTypeAdapterClass;
            try {
                // newer gson releases
                treeTypeAdapterClass = Class.forName("com.google.gson.internal.bind.TreeTypeAdapter");
            } catch (final ClassNotFoundException e) {
                // old gson releases
                treeTypeAdapterClass = Class.forName("com.google.gson.TreeTypeAdapter");
            }

            final Method newFactoryWithMatchRawTypeMethod = treeTypeAdapterClass.getMethod("newFactoryWithMatchRawType", TypeToken.class, Object.class);
            final TypeAdapterFactory adapterComponentFactory = (TypeAdapterFactory) newFactoryWithMatchRawTypeMethod.invoke(null, TypeToken.get(AdapterComponent.class), new Serializer());
            modifiedFactories.add(index, adapterComponentFactory);

            factoriesField.set(gson, modifiedFactories);
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }

    private static Field field(final Class<?> klass, final String name) throws NoSuchFieldException {
        final Field field = klass.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static int findExcluderIndex(final List<TypeAdapterFactory> factories) {
        for (int i = 0, size = factories.size(); i < size; i++) {
            final TypeAdapterFactory factory = factories.get(i);
            if (factory instanceof Excluder) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    public void sendMessage(final List<? extends CommandSender> viewers, final Component component) {
        if (!BOUND) {
            return;
        }
        send(viewers, component, (viewer, components) -> viewer.spigot().sendMessage(components));
    }

    @Override
    public void sendActionBar(final List<? extends CommandSender> viewers, final Component component) {
        if (!BOUND) {
            return;
        }
        send(viewers, component, (viewer, components) -> viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, components));
    }

    private static void send(final List<? extends CommandSender> viewers, final Component component, final BiConsumer<Player, BaseComponent[]> consumer) {
        if (!BOUND) {
            return;
        }
        final BaseComponent[] components = {new AdapterComponent(component)};
        for (final Iterator<? extends CommandSender> it = viewers.iterator(); it.hasNext(); ) {
            final CommandSender viewer = it.next();
            if (viewer instanceof Player) {
                try {
                    consumer.accept((Player) viewer, components);
                    it.remove();
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static BaseComponent[] toBungeeCord(final Component component) {
        if (BOUND) {
            return new BaseComponent[]{new AdapterComponent(component)};
        } else {
            return ComponentSerializer.parse(GsonComponentSerializer.INSTANCE.serialize(component));
        }
    }

    public static final class AdapterComponent extends BaseComponent {
        private final Component component;

        @SuppressWarnings("deprecation")
        // weeeee
        AdapterComponent(final Component component) {
            this.component = component;
        }

        @Override
        public BaseComponent duplicate() {
            return this;
        }

        @Override
        public String toLegacyText() {
            return LegacyComponentSerializer.legacy().serialize(this.component);
        }
    }

    public static class Serializer implements JsonSerializer<AdapterComponent> {
        @Override
        public JsonElement serialize(final AdapterComponent src, final Type typeOfSrc, final JsonSerializationContext context) {
            return context.serialize(src.component);
        }
    }
}
