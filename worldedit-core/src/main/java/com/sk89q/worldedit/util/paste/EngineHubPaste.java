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

package com.sk89q.worldedit.util.paste;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.util.net.HttpRequest;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

public class EngineHubPaste implements Paster {

    private static final Gson GSON = new Gson();

    @Override
    public Callable<URL> paste(String content, PasteMetadata metadata) {
        return new PasteTask(content, metadata);
    }

    private static final class PasteTask implements Callable<URL> {
        private final String content;
        private final PasteMetadata metadata;

        private PasteTask(String content, PasteMetadata metadata) {
            this.content = content;
            this.metadata = metadata;
        }

        @Override
        public URL call() throws IOException, InterruptedException {
            URL initialUrl = HttpRequest.url("https://paste.enginehub.org/signed_paste");

            HttpRequest requestBuilder = HttpRequest.get(initialUrl);

            requestBuilder.header("x-paste-meta-from", "EngineHub");
            if (metadata.name != null) {
                requestBuilder.header("x-paste-meta-name", metadata.name);
            }
            if (metadata.author != null) {
                requestBuilder.header("x-paste-meta-author", metadata.author);
            }
            if (metadata.extension != null) {
                requestBuilder.header("x-paste-meta-extension", metadata.extension);
            }

            SignedPasteResponse response = GSON.fromJson(requestBuilder
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asString("UTF-8"), TypeToken.get(SignedPasteResponse.class).getType());

            HttpRequest.Form form = HttpRequest.Form.create();
            for (Map.Entry<String, String> entry : response.uploadFields.entrySet()) {
                form.add(entry.getKey(), entry.getValue());
            }
            form.add("file", content);

            URL url = HttpRequest.url(response.uploadUrl);
            // If this succeeds, it will not return any data aside from a 204 status.
            HttpRequest.post(url)
                    .bodyMultipartForm(form)
                    .execute()
                    .expectResponseCode(200, 204);

            return new URL(response.viewUrl);
        }
    }

    private static final class SignedPasteResponse {
        String viewUrl;
        String uploadUrl;
        Map<String, String> uploadFields;
    }
}
