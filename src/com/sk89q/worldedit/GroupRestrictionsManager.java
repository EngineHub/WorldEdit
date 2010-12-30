/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk89q.worldedit;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.io.*;

/**
 *
 * @author sk89q
 */
public class GroupRestrictionsManager {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    /**
     * Store block change limits.
     */
    private Map<String,Integer> changeLimits
            = new HashMap<String,Integer>();

    /**
     * Get a group's change limit. Returns -2 if there is no setting.
     *
     * @param group
     * @return
     */
    public int getChangeLimit(String group) {
        group = group.toLowerCase();
        if (changeLimits.containsKey(group.toLowerCase())) {
            return changeLimits.get(group);
        } else {
            return -2;
        }
    }

    /**
     * Get the highest change limit of a list of groups.
     * Returns -2 if there is no setting.
     *
     * @param group
     * @return
     */
    public int getGreatestChangeLimit(String[] groups) {
        int highestLimit = -2;
        
        for (String group : groups) {
            int changeLimit = getChangeLimit(group);
            if (changeLimit == -1) {
                return -1;
            } else if (changeLimit > highestLimit) {
                highestLimit = changeLimit;
            }
        }

        return highestLimit;
    }

    /**
     * Load group restrictions from a file.
     *
     * @param file
     * @throws IOException
     */
    public void load(String file) throws IOException {
        load(new File(file));
    }

    /**
     * Load group restrictions from a file.
     * 
     * @param file
     * @throws IOException
     */
    public void load(File file) throws IOException {
        FileReader input = null;
        Map<String,Integer> changeLimits = new HashMap<String,Integer>();

        try {
            input = new FileReader(file);
            BufferedReader buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank line
                if (line.length() == 0) {
                    continue;
                }

                // Comment
                if (line.charAt(0) == ';' || line.charAt(0) == '#' || line.equals("")) {
                    continue;
                }

                String[] parts = line.split(":");

                String groupID = parts[0].toLowerCase();
                try {
                    int changeLimit = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;
                    changeLimits.put(groupID, changeLimit);
                } catch (NumberFormatException e) {
                    logger.warning("Integer expected in"
                            + "WorldEdit group permissions line: " + line);
                }
            }

            this.changeLimits = changeLimits;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e2) {
            }
        }
    }
}
