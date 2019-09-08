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

package com.sk89q.util.yaml;

import com.sk89q.util.StringUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML configuration loader. To use this class, construct it with path to
 * a file and call its load() method. For specifying node paths in the
 * various get*() methods, they support SK's path notation, allowing you to
 * select child nodes by delimiting node names with periods.
 *
 * <p>
 * For example, given the following configuration file:</p>
 *
 * <pre>members:
 *     - Hollie
 *     - Jason
 *     - Bobo
 *     - Aya
 *     - Tetsu
 * worldguard:
 *     fire:
 *         spread: false
 *         blocks: [cloth, rock, glass]
 * sturmeh:
 *     cool: false
 *     eats:
 *         babies: true</pre>
 *
 * <p>Calling code could access sturmeh's baby eating state by using
 * {@code getBoolean("sturmeh.eats.babies", false)}. For lists, there are
 * methods such as {@code getStringList} that will return a type safe list.
 */
public class YAMLProcessor extends YAMLNode {

    public static final String LINE_BREAK = DumperOptions.LineBreak.getPlatformLineBreak().getString();
    public static final char COMMENT_CHAR = '#';
    protected final Yaml yaml;
    protected final File file;
    protected String header = null;
    protected YAMLFormat format;

    /*
     * Map from property key to comment. Comment may have multiple lines that are newline-separated.
     * Comments support based on ZerothAngel's AnnotatedYAMLConfiguration
     * Comments are only supported with YAMLFormat.EXTENDED
     */
    private final Map<String, String> comments = new HashMap<>();

    public YAMLProcessor(File file, boolean writeDefaults, YAMLFormat format) {
        super(new LinkedHashMap<>(), writeDefaults);
        this.format = format;

        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(format.getStyle());
        Representer representer = new FancyRepresenter();
        representer.setDefaultFlowStyle(format.getStyle());

        yaml = new Yaml(new SafeConstructor(), representer, options);

        this.file = file;
    }

    public YAMLProcessor(File file, boolean writeDefaults) {
        this(file, writeDefaults, YAMLFormat.COMPACT);
    }

    /**
     * Loads the configuration file.
     *
     * @throws java.io.IOException on load error
     */
    public void load() throws IOException {

        try (InputStream stream = getInputStream()) {
            if (stream == null)
                throw new IOException("Stream is null!");
            read(yaml.load(new UnicodeReader(stream)));
        } catch (YAMLProcessorException e) {
            root = new LinkedHashMap<>();
        }
    }

    /**
     * Set the header for the file as a series of lines that are terminated
     * by a new line sequence.
     *
     * @param headerLines header lines to prepend
     */
    public void setHeader(String... headerLines) {
        StringBuilder header = new StringBuilder();

        for (String line : headerLines) {
            if (header.length() > 0) {
                header.append(LINE_BREAK);
            }
            header.append(line);
        }

        setHeader(header.toString());
    }

    /**
     * Set the header for the file. A header can be provided to prepend the
     * YAML data output on configuration save. The header is
     * printed raw and so must be manually commented if used. A new line will
     * be appended after the header, however, if a header is provided.
     *
     * @param header header to prepend
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Return the set header.
     *
     * @return the header text
     */
    public String getHeader() {
        return header;
    }

    /**
     * Saves the configuration to disk. All errors are clobbered.
     *
     * @return true if it was successful
     */
    public boolean save() {

        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (OutputStream stream = getOutputStream()) {
            if (stream == null)
                return false;
            OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
            if (header != null) {
                writer.append(header);
                writer.append(LINE_BREAK);
            }
            if (comments.isEmpty() || format != YAMLFormat.EXTENDED) {
                yaml.dump(root, writer);
            } else {
                // Iterate over each root-level property and dump
                for (Entry<String, Object> entry: root.entrySet()) {
                    // Output comment, if present
                    String comment = comments.get(entry.getKey());
                    if (comment != null) {
                        writer.append(LINE_BREAK);
                        writer.append(comment);
                        writer.append(LINE_BREAK);
                    }

                    // Dump property
                    yaml.dump(Collections.singletonMap(entry.getKey(), entry.getValue()), writer);
                }
            }
            return true;
        } catch (IOException ignored) {
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private void read(Object input) throws YAMLProcessorException {
        try {
            if (null == input) {
                root = new LinkedHashMap<>();
            } else {
                root = new LinkedHashMap<>((Map<String, Object>) input);
            }
        } catch (ClassCastException e) {
            throw new YAMLProcessorException("Root document must be an key-value structure");
        }
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

    /**
     * Returns a root-level comment.
     *
     * @param key the property key
     * @return the comment or {@code null}
     */
    public String getComment(String key) {
        return comments.get(key);
    }

    public void setComment(String key, String comment) {
        if (comment != null) {
            setComment(key, comment.split("\\r?\\n"));
        } else {
            comments.remove(key);
        }
    }

    /**
     * Set a root-level comment.
     *
     * @param key the property key
     * @param comment the comment. May be {@code null}, in which case the comment
     *   is removed.
     */
    public void setComment(String key, String... comment) {
        if (comment != null && comment.length > 0) {
            for (int i = 0; i < comment.length; ++i) {
                if (!comment[i].matches("^" + COMMENT_CHAR + " ?")) {
                    comment[i] = COMMENT_CHAR + " " + comment[i];
                }
            }
            String s = StringUtil.joinString(comment, LINE_BREAK);
            comments.put(key, s);
        } else {
            comments.remove(key);
        }
    }

    /**
     * Returns root-level comments.
     *
     * @return map of root-level comments
     */
    public Map<String, String> getComments() {
        return Collections.unmodifiableMap(comments);
    }

    /**
     * Set root-level comments from a map.
     *
     * @param comments comment map
     */
    public void setComments(Map<String, String> comments) {
        this.comments.clear();
        if (comments != null) {
            this.comments.putAll(comments);
        }
    }

    /**
     * This method returns an empty ConfigurationNode for using as a
     * default in methods that select a node from a node list.
     *
     * @param writeDefaults true to write default values when a property is requested that doesn't exist
     * @return a node
     */
    public static YAMLNode getEmptyNode(boolean writeDefaults) {
        return new YAMLNode(new LinkedHashMap<>(), writeDefaults);
    }

    private static class FancyRepresenter extends Representer {
        private FancyRepresenter() {
            this.nullRepresenter = o -> representScalar(Tag.NULL, "");
        }
    }

}
