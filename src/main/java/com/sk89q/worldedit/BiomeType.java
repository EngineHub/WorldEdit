package com.sk89q.worldedit;

public class BiomeType {

    private String name;

    public BiomeType(String name) {
        this.name = name;
    }

    /**
     * Get the name of the player.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns true if equal.
     *
     * @param other
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BiomeType) {
            return ((BiomeType) obj).name.equals(name);
        }
        return false;
    }
}
