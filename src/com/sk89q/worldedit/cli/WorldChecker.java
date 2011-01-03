// $Id$
/*
 * WorldEdit
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

package com.sk89q.worldedit.cli;

import java.io.*;
import java.nio.*;
import java.util.regex.Pattern;

import org.jnbt.NBTInputStream;
import org.jnbt.Tag;

public class WorldChecker {
    private File worldPath;
    
    public WorldChecker(String path) {
        worldPath = new File(path);

        checkLevelDat();
        checkFiles();

        System.out.println("Done.");
    }
    
    public void checkLevelDat() {
        try {
            checkNBT(new File(worldPath, "level.dat"));
        } catch (IOException e) {
            System.out.println("BAD: level.dat: " + e.getMessage());
        }
    }
    
    public void checkFiles() {
        final Pattern chunkFilePattern = Pattern.compile("^c\\..*\\.dat$");
        
        FileFilter folderFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        };

        FileFilter chunkFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile()
                        && chunkFilePattern.matcher(f.getName()).matches();
            }
        };
        
        for (File l1 : worldPath.listFiles(folderFilter)) {
            for (File l2 : l1.listFiles(folderFilter)) {
                for (File chunkFile : l2.listFiles(chunkFilter)) {
                    checkChunkFile(chunkFile,
                            l1.getName(), l2.getName(), chunkFile.getName());
                }
            }
        }
    }
    
    public void checkChunkFile(File f, String a, String b, String c) {
        String id = a + "/" + b + "/" + c;

        try {
            checkNBT(f);
        } catch (IOException e) {
            System.out.println("BAD: " + id);
        }
    }
    
    public void checkNBT(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        NBTInputStream nbt = new NBTInputStream(stream);
        Tag tag = nbt.readTag();
    }
}
