package com.sk89q.worldedit.extent.clipboard.io.sponge;


import com.sk89q.worldedit.world.DataFixer;

import javax.annotation.Nullable;

final class VersionedDataFixer {
    private final int dataVersion;
    @Nullable
    private final DataFixer fixer;

    VersionedDataFixer(int dataVersion, @Nullable DataFixer fixer) {
        this.dataVersion = dataVersion;
        this.fixer = fixer;
    }

    public boolean isActive() {
        return fixer != null;
    }

    public <T> T fixUp(DataFixer.FixType<T> type, T original) {
        if (!isActive()) {
            return original;
        }
        return fixer.fixUp(type, original, dataVersion);
    }
}
