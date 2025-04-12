/*
 * This file is part of text, licensed under the MIT License.
 *
 * Copyright (c) 2017-2020 KyoriPowered
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

package com.sk89q.worldedit.util.formatting.text.serializer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;

import java.lang.reflect.Type;

// This class was copied and manually "relocated" from text3 to update the click/hover events for 1.21.5.
public class StyleSerializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
    public static final StyleSerializer INSTANCE = new StyleSerializer();

    private static final TextDecoration[] DECORATIONS = TextDecoration.values();

    static final boolean PRE_1215;
    static final String COLOR = "color";
    static final String INSERTION = "insertion";
    static final String CLICK_EVENT;
    static final String CLICK_EVENT_ACTION = "action";
    static final String CLICK_EVENT_VALUE = "value";
    static final String CLICK_EVENT_URL = "url";
    static final String CLICK_EVENT_COMMAND = "command";
    static final String CLICK_EVENT_PAGE = "page";
    static final String HOVER_EVENT;
    static final String HOVER_EVENT_ACTION = "action";
    static final String HOVER_EVENT_VALUE = "value";

    static {
        boolean tmp = false;
        try {
            tmp = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.USER_COMMANDS)
                    .getDataVersion() < Constants.DATA_VERSION_MC_1_21_5;
        } catch (Exception ignored) {
        }
        PRE_1215 = tmp;
        CLICK_EVENT = PRE_1215 ? "clickEvent" : "click_event";
        HOVER_EVENT = PRE_1215 ? "hoverEvent" : "hover_event";
    }

    @Override
    public Style deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject object = json.getAsJsonObject();
        return this.deserialize(object, context);
    }

    private Style deserialize(final JsonObject json, final JsonDeserializationContext context) throws JsonParseException {
        final Style.Builder style = Style.builder();

        if (json.has(COLOR)) {
            final TextColorWrapper color = context.deserialize(json.get(COLOR), TextColorWrapper.class);
            if (color.color != null) {
                style.color(color.color);
            } else if (color.decoration != null) {
                // I know. Setting a decoration from the color is weird. This is, unfortunately, something we need to support.
                style.decoration(color.decoration, true);
            }
        }

        for (int i = 0, length = DECORATIONS.length; i < length; i++) {
            final TextDecoration decoration = DECORATIONS[i];
            final String name = TextDecoration.NAMES.name(decoration);
            if (json.has(name)) {
                style.decoration(decoration, json.get(name).getAsBoolean());
            }
        }

        if (json.has(INSERTION)) {
            style.insertion(json.get(INSERTION).getAsString());
        }

        if (json.has(CLICK_EVENT)) {
            final JsonObject clickEvent = json.getAsJsonObject(CLICK_EVENT);
            if (clickEvent != null) {
                final /* @Nullable */ JsonPrimitive rawAction = clickEvent.getAsJsonPrimitive(CLICK_EVENT_ACTION);
                final ClickEvent./*@Nullable*/ Action action = rawAction == null ? null : context.deserialize(rawAction, ClickEvent.Action.class);
                if (action != null && action.readable()) {
                    final /* @Nullable */ JsonPrimitive rawValue = clickEvent.getAsJsonPrimitive(clickActionToKey(action));
                    final /* @Nullable */ String value = rawValue == null ? null : rawValue.getAsString();
                    if (value != null) {
                        style.clickEvent(ClickEvent.of(action, value));
                    }
                }
            }
        }

        if (json.has(HOVER_EVENT)) {
            final JsonObject hoverEvent = json.getAsJsonObject(HOVER_EVENT);
            if (hoverEvent != null) {
                final /* @Nullable */ JsonPrimitive rawAction = hoverEvent.getAsJsonPrimitive(HOVER_EVENT_ACTION);
                final HoverEvent./*@Nullable*/ Action action = rawAction == null ? null : context.deserialize(rawAction, HoverEvent.Action.class);
                if (action != null && action.readable()) {
                    if (!PRE_1215 && action != HoverEvent.Action.SHOW_TEXT) {
                        // entity/item are technically readable but text3 can't deserialize the NBT contents
                        throw new IllegalArgumentException("Don't know how to serialize " + hoverEvent);
                    }
                    final /* @Nullable */ JsonElement rawValue = hoverEvent.get(HOVER_EVENT_VALUE);
                    final /* @Nullable */ Component value = rawValue == null ? null : context.deserialize(rawValue, Component.class);
                    if (value != null) {
                        style.hoverEvent(HoverEvent.of(action, value));
                    }
                }
            }
        }

        return style.build();
    }

    @Override
    public JsonElement serialize(final Style src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject json = new JsonObject();

        final /* @Nullable */ TextColor color = src.color();
        if (color != null) {
            json.add(COLOR, context.serialize(color));
        }

        for (int i = 0, length = DECORATIONS.length; i < length; i++) {
            final TextDecoration decoration = DECORATIONS[i];
            final TextDecoration.State state = src.decoration(decoration);
            if (state != TextDecoration.State.NOT_SET) {
                final String name = TextDecoration.NAMES.name(decoration);
                json.addProperty(name, state == TextDecoration.State.TRUE);
            }
        }

        final /* @Nullable */ String insertion = src.insertion();
        if (insertion != null) {
            json.add(INSERTION, context.serialize(insertion));
        }

        final /* @Nullable */ ClickEvent clickEvent = src.clickEvent();
        if (clickEvent != null) {
            final JsonObject eventJson = new JsonObject();
            eventJson.add(CLICK_EVENT_ACTION, context.serialize(clickEvent.action()));
            if (!PRE_1215 && clickEvent.action() == ClickEvent.Action.CHANGE_PAGE) {
                eventJson.addProperty(CLICK_EVENT_PAGE, Integer.valueOf(clickEvent.value()));
            } else {
                eventJson.addProperty(clickActionToKey(clickEvent.action()), clickEvent.value());
            }
            json.add(CLICK_EVENT, eventJson);
        }

        final /* @Nullable */ HoverEvent hoverEvent = src.hoverEvent();
        if (hoverEvent != null) {
            if (!PRE_1215 && hoverEvent.action() != HoverEvent.Action.SHOW_TEXT) {
                throw new IllegalArgumentException("Don't know how to serialize " + hoverEvent);
            }
            final JsonObject eventJson = new JsonObject();
            eventJson.add(HOVER_EVENT_ACTION, context.serialize(hoverEvent.action()));
            eventJson.add(HOVER_EVENT_VALUE, context.serialize(hoverEvent.value()));
            json.add(HOVER_EVENT, eventJson);
        }

        return json;
    }

    private static String clickActionToKey(ClickEvent.Action action) {
        if (PRE_1215) {
            return CLICK_EVENT_VALUE;
        }
        return switch (action) {
            case OPEN_URL -> CLICK_EVENT_URL;
            case CHANGE_PAGE -> CLICK_EVENT_PAGE;
            case RUN_COMMAND, SUGGEST_COMMAND -> CLICK_EVENT_COMMAND;
            case COPY_TO_CLIPBOARD -> CLICK_EVENT_VALUE;
            default -> throw new IllegalArgumentException("Can't convert action " + action + " to serialization key.");
        };
    }
}
