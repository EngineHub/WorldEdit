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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest.Builder signRequestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create("https://paste.enginehub.org/signed_paste_v2"))
                        .header("x-paste-meta-from", "EngineHub");

                if (metadata.name != null) {
                    signRequestBuilder.header("x-paste-meta-name", metadata.name);
                }
                if (metadata.author != null) {
                    signRequestBuilder.header("x-paste-meta-author", metadata.author);
                }
                if (metadata.extension != null) {
                    signRequestBuilder.header("x-paste-meta-extension", metadata.extension);
                }

                HttpResponse<String> signResponse = client.send(signRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());

                if (signResponse.statusCode() != 200) {
                    throw new IOException("Failed to initiate paste upload: " + signResponse.statusCode() + " " + signResponse.body());
                }

                SignedPasteResponse response = GSON.fromJson(
                        signResponse.body(),
                        TypeToken.get(SignedPasteResponse.class).getType()
                );

                HttpRequest.Builder uploadRequestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(response.uploadUrl))
                        .method("PUT", HttpRequest.BodyPublishers.ofString(content));

                for (Map.Entry<String, String> entry : response.headers.entrySet()) {
                    uploadRequestBuilder.header(entry.getKey(), entry.getValue());
                }

                // If this succeeds, it will not return any data aside from a 204 status.
                HttpResponse<String> uploadResponse = client.send(uploadRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());

                if (uploadResponse.statusCode() != 200 && uploadResponse.statusCode() != 204) {
                    throw new IOException("Failed to upload paste: " + uploadResponse.statusCode() + " " + uploadResponse.body());
                }

                return URI.create(response.viewUrl).toURL();
            }
        }
    }

    private static final class SignedPasteResponse {
        String viewUrl;
        String uploadUrl;
        Map<String, String> headers;
    }
}
