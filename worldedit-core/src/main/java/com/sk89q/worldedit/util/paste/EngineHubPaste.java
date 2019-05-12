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

package com.sk89q.worldedit.util.paste;

import com.sk89q.worldedit.util.net.HttpRequest;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EngineHubPaste implements Paster {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://.+$");

    @Override
    public Callable<URL> paste(String content) {
        return new PasteTask(content);
    }

    private static final class PasteTask implements Callable<URL> {
        private final String content;

        private PasteTask(String content) {
            this.content = content;
        }

        @Override
        public URL call() throws IOException, InterruptedException {
            HttpRequest.Form form = HttpRequest.Form.create();
            form.add("content", content);
            form.add("from", "enginehub");

            URL url = HttpRequest.url("http://paste.enginehub.org/paste");
            String result = HttpRequest.post(url)
                    .bodyForm(form)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();

            Object object = JSONValue.parse(result);
            if (object instanceof Map) {
                @SuppressWarnings("unchecked")
                String urlString = String.valueOf(((Map<Object, Object>) object).get("url"));
                Matcher m = URL_PATTERN.matcher(urlString);

                if (m.matches()) {
                    return new URL(urlString);
                }
            }

            throw new IOException("Failed to save paste; instead, got: " + result);
        }
    }

}
