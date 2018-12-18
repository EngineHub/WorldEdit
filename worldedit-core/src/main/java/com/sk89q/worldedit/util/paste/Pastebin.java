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

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.util.net.HttpRequest;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pastebin implements Paster {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://pastebin.com/([^/]+)$");

    private boolean mungingLinks = true;

    public boolean isMungingLinks() {
        return mungingLinks;
    }

    public void setMungingLinks(boolean mungingLinks) {
        this.mungingLinks = mungingLinks;
    }

    @Override
    public ListenableFuture<URL> paste(String content) {
        if (mungingLinks) {
            content = content.replaceAll("http://", "http_//");
        }

        return Pasters.getExecutor().submit(new PasteTask(content));
    }

    private final class PasteTask implements Callable<URL> {
        private final String content;

        private PasteTask(String content) {
            this.content = content;
        }

        @Override
        public URL call() throws IOException, InterruptedException {
            HttpRequest.Form form = HttpRequest.Form.create();
            form.add("api_option", "paste");
            form.add("api_dev_key", "4867eae74c6990dbdef07c543cf8f805");
            form.add("api_paste_code", content);
            form.add("api_paste_private", "0");
            form.add("api_paste_name", "");
            form.add("api_paste_expire_date", "1W");
            form.add("api_paste_format", "text");
            form.add("api_user_key", "");

            URL url = HttpRequest.url("http://pastebin.com/api/api_post.php");
            String result = HttpRequest.post(url)
                    .bodyForm(form)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asString("UTF-8").trim();

            Matcher m = URL_PATTERN.matcher(result);

            if (m.matches()) {
                return new URL("http://pastebin.com/raw.php?i=" + m.group(1));
            } else if (result.matches("^https?://.+")) {
                return new URL(result);
            } else {
                throw new IOException("Failed to save paste; instead, got: " + result);
            }
        }
    }
    
}
