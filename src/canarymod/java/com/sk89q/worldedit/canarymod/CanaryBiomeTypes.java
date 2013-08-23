package com.sk89q.worldedit.canarymod;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;

public class CanaryBiomeTypes implements BiomeTypes, BiomeType {

    @Override
    public String getName() {
        return "CanaryBiomeTypes";
    }

    @Override
    public boolean has(String name) {
        try {
            CanaryBiomeType.valueOf(name.toUpperCase(Locale.ENGLISH));
            return true;
        } catch (IllegalArgumentException exc) {
            return false;
        }
    }

    @Override
    public BiomeType get(String name) throws UnknownBiomeTypeException {
        try {
            return CanaryBiomeType.valueOf(name.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exc) {
            throw new UnknownBiomeTypeException(name);
        }
    }

    @Override
    public List<BiomeType> all() {
        return Arrays.<BiomeType> asList(CanaryBiomeType.values());
    }

}
