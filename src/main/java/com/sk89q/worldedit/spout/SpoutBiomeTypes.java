package com.sk89q.worldedit.spout;

import java.util.Collections;
import java.util.List;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;

public class SpoutBiomeTypes implements BiomeTypes {

    @Override
    public boolean has(String name) {
        return false;
    }

    @Override
    public BiomeType get(String name) throws UnknownBiomeTypeException {
        throw new UnknownBiomeTypeException(name);
    }

    @Override
    public List<BiomeType> all() {
        return Collections.<BiomeType>emptyList();
    }
}
