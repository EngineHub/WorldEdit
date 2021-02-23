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

package com.sk89q.worldedit.internal.util;

import com.sk89q.worldedit.WorldEditManifest;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class InfoEntryPoint {

    private static final String INSTALL_URL = "https://worldedit.enginehub.org/en/latest/install/";
    private static final String SUPPORT_URL = "https://discord.gg/enginehub";

    private static String getMessage(boolean html) {
        WorldEditManifest manifest = WorldEditManifest.load();

        return "To install WorldEdit, place it in the "
            + manifest.getWorldEditKind().folderName + " folder.\n"
            + "For more detailed instructions, see " + formatLink(INSTALL_URL, html) + "\n"
            + "For further help, check out our support Discord at "
            + formatLink(SUPPORT_URL, html) + "\n"
            + "\n"
            + "Version: " + manifest.getWorldEditVersion() + "\n";
    }

    private static String formatLink(String url, boolean html) {
        return html ? String.format("<a href=\"%1$s\">%1$s</a>", url) : url;
    }

    public static void main(String[] args) {
        if (System.console() != null) {
            System.err.println(getMessage(false));
        } else {
            System.setProperty("awt.useSystemAAFontSettings", "lcd");
            JOptionPane.showMessageDialog(
                null,
                new NavigableEditorPane(getMessage(true)),
                "WorldEdit",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        System.exit(1);
    }

    private static class NavigableEditorPane extends JTextPane {
        public NavigableEditorPane(String htmlBody) {
            super(new HTMLDocument());
            setEditorKit(new HTMLEditorKit());
            setText(htmlBody.replace("\n", "<br>"));
            setBackground(UIManager.getColor("Panel.background"));

            addHyperlinkListener(e -> {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            setEditable(false);
            setBorder(null);
        }
    }
}
