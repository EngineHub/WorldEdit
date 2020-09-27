package com.sk89q.worldedit.util.io.file;

import com.sk89q.worldedit.util.collection.SetWithDefault;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class FileDialogUtil {

    public static Path requestPath(PathRequestType type,
                                   SetWithDefault<FileType> fileTypes) throws FileSelectionAbortedException {
        JFileChooser dialog = new JFileChooser();

        for (FileType fileType : fileTypes.values()) {
            dialog.addChoosableFileFilter(asFileFilter(fileType));
        }

        int dialogType;
        switch (type) {
            case LOAD:
                dialogType = JFileChooser.OPEN_DIALOG;
                break;
            case SAVE:
                dialogType = JFileChooser.SAVE_DIALOG;
                break;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
        dialog.setDialogType(dialogType);
        int returnVal = dialog.showDialog(null, null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return dialog.getSelectedFile().toPath();
        }

        throw new FileSelectionAbortedException(
            TranslatableComponent.of("worldedit.error.no-file-selected")
        );
    }

    private static FileFilter asFileFilter(FileType fileType) {
        return new FileNameExtensionFilter(
            fileType.getDescription(), fileType.getExtensions().values().toArray(new String[0])
        );
    }

    private FileDialogUtil() {
    }
}
