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

import com.sk89q.worldedit.util.io.file.FileSelectionAbortedException;
import com.sk89q.worldedit.util.io.file.FileType;
import com.sk89q.worldedit.util.io.file.PathRequestType;

import java.io.File;

/**
 * File dialog utility.
 *
 * @deprecated Use {@link com.sk89q.worldedit.util.io.file.FileDialogUtil} instead
 */
@Deprecated
public final class FileDialogUtil {
    private FileDialogUtil() {
    }

    public static File showSaveDialog(String[] exts) {
        try {
            return com.sk89q.worldedit.util.io.file.FileDialogUtil.requestPath(
                PathRequestType.SAVE,
                FileType.adaptLegacyExtensions(null, exts)
            ).toFile();
        } catch (FileSelectionAbortedException e) {
            return null;
        }
    }

    public static File showOpenDialog(String[] exts) {
        try {
            return com.sk89q.worldedit.util.io.file.FileDialogUtil.requestPath(
                PathRequestType.LOAD,
                FileType.adaptLegacyExtensions(null, exts)
            ).toFile();
        } catch (FileSelectionAbortedException e) {
            return null;
        }
    }
}
