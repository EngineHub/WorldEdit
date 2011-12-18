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

package com.sk89q.worldedit.util;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import com.sk89q.util.StringUtil;

public class FileDialogUtil {
    public static File showSaveDialog(String[] exts) {
        JFileChooser dialog = new JFileChooser();

        if (exts != null) {
            dialog.setFileFilter(new ExtensionFilter(exts));
        }

        int returnVal = dialog.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return dialog.getSelectedFile();
        }

        return null;
    }

    public static File showOpenDialog(String[] exts) {
        JFileChooser dialog = new JFileChooser();

        if (exts != null) {
            dialog.setFileFilter(new ExtensionFilter(exts));
        }

        int returnVal = dialog.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return dialog.getSelectedFile();
        }

        return null;
    }

    private static class ExtensionFilter extends FileFilter {
        private Set<String> exts;
        private String desc;

        public ExtensionFilter(String[] exts) {
            this.exts = new HashSet<String>(Arrays.asList(exts));

            desc = StringUtil.joinString(exts, ",");
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String path = f.getPath();
            int index = path.lastIndexOf('.');
            if (index == -1 || index == path.length() - 1) {
                return false;
            } else {
                return exts.contains(path.indexOf(index + 1));
            }
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }
}
