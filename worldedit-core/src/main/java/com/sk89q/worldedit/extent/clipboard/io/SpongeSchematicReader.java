package com.sk89q.worldedit.extent.clipboard.io;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.sponge.ReaderUtil;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV1Reader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV2Reader;
import com.sk89q.worldedit.internal.Constants;

import java.io.IOException;
import java.util.OptionalInt;

/**
 * Legacy multi-version reader. Do not use, pick a versioned one instead.
 */
@Deprecated
public class SpongeSchematicReader extends NBTSchematicReader {
    private final NBTInputStream inputStream;

    public SpongeSchematicReader(NBTInputStream inputStream) {
        this.inputStream = inputStream;
    }

    private CompoundTag getBaseTag() throws IOException {
        NamedTag rootTag = inputStream.readNamedTag();

        return (CompoundTag) rootTag.getTag();
    }

    @Override
    public Clipboard read() throws IOException {
        CompoundTag schematicTag = getBaseTag();
        int version = ReaderUtil.getSchematicVersion(schematicTag);
        return switch (version) {
            case 1 -> SpongeSchematicV1Reader.doRead(schematicTag);
            case 2 -> SpongeSchematicV2Reader.doRead(schematicTag);
            default -> throw new IllegalStateException("Unsupported schematic version: " + version);
        };
    }

    @Override
    public OptionalInt getDataVersion() {
        try {
            CompoundTag schematicTag = getBaseTag();
            int version = ReaderUtil.getSchematicVersion(schematicTag);
            return switch (version) {
                case 1 -> OptionalInt.of(Constants.DATA_VERSION_MC_1_13_2);
                case 2 -> {
                    int dataVersion = requireTag(schematicTag.getValue(), "DataVersion", IntTag.class)
                        .getValue();
                    if (dataVersion < 0) {
                        yield OptionalInt.empty();
                    }
                    yield  OptionalInt.of(dataVersion);
                }
                default -> OptionalInt.empty();
            };
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
