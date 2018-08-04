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

package com.sk89q.worldedit.util.formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment of text that can be styled.
 */
public class StyledFragment extends Fragment {
    
    private final List<Fragment> children = new ArrayList<>();
    private StyleSet style;
    private Fragment lastText;
    
    public StyledFragment() {
        style = new StyleSet();
    }
    
    public StyledFragment(StyleSet style) {
        this.style = style;
    }
    
    public StyledFragment(Style... styles) {
        this.style = new StyleSet(styles);
    }
    
    public StyleSet getStyle() {
        return style;
    }

    public void setStyles(StyleSet style) {
        this.style = style;
    }

    public List<Fragment> getChildren() {
        return children;
    }

    protected Fragment lastText() {
        Fragment text;
        if (!children.isEmpty()) {
            text = children.get(children.size() - 1);
            if (text == lastText) {
                return text;
            }
        }
        
        text = new Fragment();
        this.lastText = text;
        children.add(text);
        return text;
    }

    public StyledFragment createFragment(Style... styles) {
        StyledFragment fragment = new StyledFragment(styles);
        append(fragment);
        return fragment;
    }

    public StyledFragment append(StyledFragment fragment) {
        children.add(fragment);
        return this;
    }

    @Override
    public StyledFragment append(String str) {
        lastText().append(str);
        return this;
    }

    @Override
    public StyledFragment append(Object obj) {
        append(String.valueOf(obj));
        return this;
    }

    @Override
    public StyledFragment append(StringBuffer sb) {
        append(String.valueOf(sb));
        return this;
    }

    @Override
    public StyledFragment append(CharSequence s) {
        append(String.valueOf(s));
        return this;
    }

    @Override
    public StyledFragment append(boolean b) {
        append(String.valueOf(b));
        return this;
    }

    @Override
    public StyledFragment append(char c) {
        append(String.valueOf(c));
        return this;
    }

    @Override
    public StyledFragment append(int i) {
        append(String.valueOf(i));
        return this;
    }

    @Override
    public StyledFragment append(long lng) {
        append(String.valueOf(lng));
        return this;
    }

    @Override
    public StyledFragment append(float f) {
        append(String.valueOf(f));
        return this;
    }

    @Override
    public StyledFragment append(double d) {
        append(String.valueOf(d));
        return this;
    }

    @Override
    public StyledFragment newLine() {
        append("\n");
        return this;
    }
    
}
