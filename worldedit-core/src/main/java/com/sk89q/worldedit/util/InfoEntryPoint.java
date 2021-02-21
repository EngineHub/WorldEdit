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

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.WorldEditManifest;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;

public class InfoEntryPoint {

    private static final String INSTALL_URL = "https://worldedit.enginehub.org/en/latest/install/";
    private static final String SUPPORT_URL = "https://discord.gg/enginehub";

    private static String getMessage(boolean hyperlinks) {
        WorldEditManifest manifest = WorldEditManifest.load();

        return "WorldEdit is not installed by running the jar file. Check out the install instructions here, "
            + (hyperlinks ? "<a href=\"" + INSTALL_URL +"\">" + INSTALL_URL + "</a>" : INSTALL_URL)
            + "\nFor further help, check out our support Discord at "
            + (hyperlinks ? "<a href=\"" + SUPPORT_URL +"\">" + SUPPORT_URL + "</a>" : SUPPORT_URL)
            + "\n\nVersion: "
            + manifest.getWorldEditVersion();
    }

    public static void main(String[] args) {
        if (System.console() != null) {
            System.out.println(getMessage(false));
        } else {
            JOptionPane.showMessageDialog(
                null,
                new MessageWithLink(getMessage(true)),
                "WorldEdit",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private static class MessageWithLink extends JEditorPane {
        public MessageWithLink(String htmlBody) {
            super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody.replace("\n", "<br />") + "</body></html>");
            addHyperlinkListener(e -> {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
            setEditable(false);
            setBorder(null);
        }

        static String getStyle() {
            // for copying style
            JLabel label = new JLabel();
            Font font = label.getFont();
            Color color = label.getBackground();

            // create some css from the label's font
            return new StringBuilder()
                .append("font-family:").append(font.getFamily()).append(";")
                .append("font-weight:").append(font.isBold() ? "bold" : "normal").append(";")
                .append("font-size:").append(font.getSize()).append("pt;")
                .append("background-color: rgb(").append(color.getRed()).append(",").append(color.getGreen()).append(",").append(color.getBlue()).append(");")
                .toString();
        }
    }
}
