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

package com.sk89q.worldedit.util.formatting.component;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

import java.util.List;

public class PaginationBox extends MessageBox {

    public static final int IDEAL_ROWS_FOR_PLAYER = 8;

    private String pageCommand;
    private List<TextComponent> components;
    private int componentsPerPage = IDEAL_ROWS_FOR_PLAYER;

    /**
     * Creates a Paginated component
     *
     * @param title The title
     */
    public PaginationBox(String title) {
        this(title, null);
    }

    /**
     * Sets the components to create pages for.
     *
     * @param components The components
     */
    public void setComponents(List<TextComponent> components) {
        this.components = components;
    }

    public void setComponentsPerPage(int componentsPerPage) {
        this.componentsPerPage = componentsPerPage;
    }

    /**
     * Creates a Paginated component
     *
     * @param title The title
     * @param pageCommand The command to run to switch page, with %page% representing page number
     */
    public PaginationBox(String title, String pageCommand) {
        super(title, new TextComponentProducer());

        if (pageCommand != null && !pageCommand.contains("%page%")) {
            throw new IllegalArgumentException("pageCommand must contain %page% if provided.");
        }
        this.pageCommand = pageCommand;
    }

    public TextComponent create(int page) throws InvalidComponentException {
        if (components == null) {
            throw new IllegalStateException("You must provide components before creating.");
        }
        if (page == 1 && components.isEmpty()) {
            return getContents().reset().append("There's nothing to see here").create();
        }
        int pageCount = (int) Math.ceil(components.size() / (double) componentsPerPage);
        if (page < 1 || page > pageCount) {
            throw new InvalidComponentException("Invalid page number.");
        }
        getContents().reset();
        for (int i = (page - 1) * componentsPerPage; i < Math.min(page * componentsPerPage, components.size()); i++) {
            getContents().append(components.get(i)).newline();
        }
        TextComponent pageNumberComponent = TextComponent.of("Page ", TextColor.YELLOW)
                .append(TextComponent.of(String.valueOf(page), TextColor.GOLD))
                .append(TextComponent.of(" of "))
                .append(TextComponent.of(String.valueOf(pageCount), TextColor.GOLD));

        if (pageCommand != null) {
            if (page > 1) {
                TextComponent prevComponent = TextComponent.of("<<< ", TextColor.GOLD)
                        .clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand.replace("%page%", String.valueOf(page - 1))))
                        .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to navigate")));
                getContents().append(prevComponent);
            }
            getContents().append(pageNumberComponent);
            if (page < pageCount) {
                TextComponent nextComponent = TextComponent.of(" >>>", TextColor.GOLD)
                        .clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pageCommand.replace("%page%", String.valueOf(page + 1))))
                        .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to navigate")));
                getContents().append(nextComponent);
            }
        } else {
            getContents().append(pageNumberComponent);
        }
        return TextComponent.of("").append(Component.newline()).append(super.create());
    }

    @Override
    public TextComponent create() {
        throw new IllegalStateException("Pagination components must be created with a page");
    }
}
