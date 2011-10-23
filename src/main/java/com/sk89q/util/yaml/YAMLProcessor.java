// $Id$
/*
 * RegionBook
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.util.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
 * <code>getBoolean("sturmeh.eats.babies", false)</code>. For lists, there are
 * methods such as <code>getStringList</code> that will return a type safe list.
 * 
 * <p>This class is currently incomplete. It is not yet possible to get a node.
 * </p>
 * 
 * @author sk89q
 */
public class YAMLProcessor extends YAMLNode {
    private Yaml yaml;
    private File file;
    private String header = null;
    
    public YAMLProcessor(File file, boolean writeDefaults, YAMLFormat format) {
        super(new HashMap<String, Object>(), writeDefaults);
        
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setDefaultFlowStyle(format.getStyle());
        Representer representer = new Representer();
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
     * @throws java.io.IOException
     */
    public void load() throws IOException {        
        FileInputStream stream = null;
        
        try {
            stream = new FileInputStream(file);
            read(yaml.load(new UnicodeReader(stream)));
        } catch (YAMLProcessorException e) {
            root = new HashMap<String, Object>();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
            }
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
                header.append("\r\n");
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
     * @return
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
        FileOutputStream stream = null;

        File parent = file.getParentFile();

        if (parent != null) {
            parent.mkdirs();
        }

        try {
            stream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            if (header != null) {
                writer.append(header);
                writer.append("\r\n");
            }
            yaml.dump(root, writer);
            return true;
        } catch (IOException e) {} finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {}
        }

        return false;
    }
    
    @SuppressWarnings("unchecked")
    private void read(Object input) throws YAMLProcessorException {
        try {
            if ( null == input ) {
                root = new HashMap<String, Object>();
            } else {
                root = (Map<String, Object>)input;
            }
        } catch (ClassCastException e) {
            throw new YAMLProcessorException("Root document must be an key-value structure");
        }
    }
    
    /**
     * This method returns an empty ConfigurationNode for using as a 
     * default in methods that select a node from a node list.
     * @return
     */
    public static YAMLNode getEmptyNode(boolean writeDefaults) {
        return new YAMLNode(new HashMap<String, Object>(), writeDefaults);
    }
}
