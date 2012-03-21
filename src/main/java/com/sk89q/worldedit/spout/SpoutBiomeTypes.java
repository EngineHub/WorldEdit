package com.sk89q.worldedit.spout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;
import org.spout.api.generator.biome.BiomeGenerator;
import org.spout.api.generator.biome.BiomeType;

public class SpoutBiomeTypes implements BiomeTypes {
    private final Map<String, SpoutBiomeType> types = new HashMap<String, SpoutBiomeType>();

    @Override
    public boolean has(String name) {
        return types.containsKey(name.toLowerCase());
    }

    @Override
    public SpoutBiomeType get(String name) throws UnknownBiomeTypeException {
        if (!has(name)) {
            throw new UnknownBiomeTypeException(name);
        } else {
            return types.get(name.toLowerCase());
        }
    }

    public void registerBiomeTypes(BiomeGenerator generator) {
        for (BiomeType type : generator.getBiomes()) {
            final SpoutBiomeType weType = new SpoutBiomeType(type);
            if (!types.containsKey(weType.getName())) {
                types.put(weType.getName(), weType);
            }
        }
    }

    @Override
    public List<com.sk89q.worldedit.BiomeType> all() {
        return new ArrayList<com.sk89q.worldedit.BiomeType>(types.values());
    }
}
