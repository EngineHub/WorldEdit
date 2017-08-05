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
package com.sk89q.worldedit.util;

import com.sk89q.worldedit.WorldEdit;

public final class Java8Detector {

    private static int major;

    public static void notifyIfNot8() {

            try {
                Class.forName("Runtime.Version");
                major = 9;
            } catch (ClassNotFoundException exception){
                String[] ver = System.getProperty("java.version").split("\\.");
                int major = Integer.parseInt(ver[1]);
            }
        
        if (major <= 7) {
                // Implicitly java 7 because we compile against 7, so this won't
                // even launch on 6.
                WorldEdit.logger.warning(
                        "WorldEdit has detected you are using Java 7"
                                + " (based on detected version "
                                + version(major)
                                + ").");
                WorldEdit.logger.warning(
                        "WorldEdit will stop supporting Java less than version 8 in the future,"
                                + " due to Java 7 being EOL since April 2015."
                                + " Please update your server to Java 8.");
            }

    }

    /**
     *
     * Retrieves simplified version string from the major version of Java, that the user is using.
     *
     */
    static String version(int major){
        if(major >= 9){
            return String.valueOf(major);
        }else if(major >= 0){
            return "1" + String.valueOf(major);
        }else{
            return "?";
        }
    }

    private Java8Detector() {
    }

}
