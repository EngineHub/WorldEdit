package com.sk89q.worldedit.util.io.file;

import java.io.Closeable;
import java.nio.file.Path;

/**
 * Represents an archive opened as a directory. This must be closed after work on the Path is
 * done.
 */
public interface ArchiveDir extends Closeable {

    Path getPath();

}
