package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV2Writer;

/**
 * Legacy Sponge Schematic writer. Do not use, pick a versioned one instead.
 */
@Deprecated
public class SpongeSchematicWriter extends SpongeSchematicV2Writer implements ClipboardWriter {
    public SpongeSchematicWriter(NBTOutputStream outputStream) {
        super(outputStream);
    }
}
