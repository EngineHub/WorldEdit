package com.sk89q.worldedit;

import java.util.List;

public interface BiomeTypes {

    /**
     * Returns if a biome with the given name is available.
     *
     * @param name
     * @return
     */
    boolean has(String name);

    /**
     * Returns the biome type for the given name
     *
     * @return
     */
    BiomeType get(String name) throws UnknownBiomeTypeException;

    /**
     * Returns a list of all available biome types.
     *
     * @return
     */
    List<BiomeType> all();
}
