package com.sk89q.worldedit;

public interface BiomeType {

    public static final BiomeType UNKNOWN = new BiomeType() {
        public String getName() {
            return "Unknown";
        }
    };

    /**
     * Get the name of this biome type.
     *
     * @return String
     */
    public String getName();
}
