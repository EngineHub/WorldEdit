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

package com.sk89q.worldedit.util.formatting.component;

import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.TextComponent;
import com.sk89q.worldedit.util.adventure.text.event.ClickEvent;
import com.sk89q.worldedit.util.adventure.text.event.HoverEvent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.formatting.LegacyTextHelper;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public abstract class PaginationBox extends MessageBox {

    private static final int IDEAL_ROWS_FOR_PLAYER = 8;

    private String pageCommand;
    private int componentsPerPage = IDEAL_ROWS_FOR_PLAYER;
    private int currentPage = -1;

    /**
     * Creates a Paginated component.
     *
     * @param title The title
     */
    protected PaginationBox(String title) {
        this(title, null);
    }

    public abstract Component component(int number);

    @Deprecated
    public com.sk89q.worldedit.util.formatting.text.Component getComponent(int number) {
        return LegacyTextHelper.adapt(component(number));
    }
    public abstract int getComponentsSize();

    public void setComponentsPerPage(int componentsPerPage) {
        this.componentsPerPage = componentsPerPage;
    }

    public void formatForConsole() {
        this.pageCommand = null;
        this.componentsPerPage = 20;
    }

    protected final int getCurrentPage() {
        return currentPage;
    }

    /**
     * Creates a Paginated component.
     *
     * @param title The title
     * @param pageCommand The command to run to switch page, with %page% representing page number
     */
    protected PaginationBox(String title, @Nullable String pageCommand) {
        super(title, Component.empty());

        if (pageCommand != null && !pageCommand.contains("%page%")) {
            throw new IllegalArgumentException("pageCommand must contain %page% if provided.");
        }
        this.pageCommand = pageCommand;
    }
    @Deprecated
    public com.sk89q.worldedit.util.formatting.text.Component create(int page) throws InvalidComponentException {
        return LegacyTextHelper.adapt(build(page));
    }

    public Component build(int page) throws InvalidComponentException {
        if (page == 1 && getComponentsSize() == 0) {
            return builder().resetStyle().append(Component.text("No results found.")).build();
        }
        int pageCount = (int) Math.ceil(getComponentsSize() / (double) componentsPerPage);
        if (page < 1 || page > pageCount) {
            throw new InvalidComponentException(Component.translatable("worldedit.error.invalid-page"));
        }
        currentPage = page;
        final int lastComp = Math.min(page * componentsPerPage, getComponentsSize());
        for (int i = (page - 1) * componentsPerPage; i < lastComp; i++) {
            builder().append(component(i));
            if (i + 1 != lastComp) {
                builder().append(Component.newline());
            }
        }
        if (pageCount == 1) {
            return super.build();
        }
        builder().append(Component.newline());
        TextComponent pageNumberComponent = Component.text("Page ", NamedTextColor.YELLOW)
                .append(Component.text(String.valueOf(page), NamedTextColor.GOLD))
                .append(Component.text(" of "))
                .append(Component.text(String.valueOf(pageCount), NamedTextColor.GOLD));
        if (pageCommand != null) {
            TextComponent.Builder navProducer = Component.text();
            if (page > 1) {
                TextComponent prevComponent = Component.text("<<< ", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand(pageCommand.replace("%page%", String.valueOf(page - 1))))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to navigate")));
                navProducer.append(prevComponent);
            }
            navProducer.append(pageNumberComponent);
            if (page < pageCount) {
                TextComponent nextComponent = Component.text(" >>>", NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand(pageCommand.replace("%page%", String.valueOf(page + 1))))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to navigate")));
                navProducer.append(nextComponent);
            }
            builder().append(centerAndBorder(navProducer.build()));
        } else {
            builder().append(centerAndBorder(pageNumberComponent));
        }
        return super.build();
    }

    @Override
    @Deprecated
    public com.sk89q.worldedit.util.formatting.text.TextComponent create() {
        throw new IllegalStateException("Pagination components must be created with a page");
    }

    public static PaginationBox fromStrings(String header, @Nullable String pageCommand, List<String> lines) {
        return fromText(header, pageCommand, lines.stream()
            .map(Component::text)
            .collect(Collectors.toList()));
    }

    @Deprecated
    public static PaginationBox fromComponents(String header, @Nullable String pageCommand, List<com.sk89q.worldedit.util.formatting.text.Component> lines) {
        return new ListPaginationBox(header, pageCommand, lines.stream().map(LegacyTextHelper::adapt).toList());
    }

    public static PaginationBox fromText(String header, @Nullable String pageCommand, List<Component> lines) {
        return new ListPaginationBox(header, pageCommand, lines);
    }

    private static class ListPaginationBox extends PaginationBox {
        private final List<Component> lines;

        ListPaginationBox(String header, String pageCommand, List<Component> lines) {
            super(header, pageCommand);
            this.lines = lines;
        }

        @Override
        public Component component(int number) {
            return lines.get(number);
        }

        @Override
        public int getComponentsSize() {
            return lines.size();
        }
    }
}
