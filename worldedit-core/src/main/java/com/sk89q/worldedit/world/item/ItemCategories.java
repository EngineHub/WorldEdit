/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.world.item;

/**
 * Stores a list of common {@link ItemCategory ItemCategories}.
 *
 * @see ItemCategory
 */
@SuppressWarnings("unused")
public final class ItemCategories {
    public static final ItemCategory ACACIA_LOGS = get("minecraft:acacia_logs");
    public static final ItemCategory ANVIL = get("minecraft:anvil");
    public static final ItemCategory ARMADILLO_FOOD = get("minecraft:armadillo_food");
    public static final ItemCategory ARROWS = get("minecraft:arrows");
    public static final ItemCategory AXES = get("minecraft:axes");
    public static final ItemCategory AXOLOTL_FOOD = get("minecraft:axolotl_food");
    @Deprecated public static final ItemCategory AXOLOTL_TEMPT_ITEMS = get("minecraft:axolotl_tempt_items");
    public static final ItemCategory BAMBOO_BLOCKS = get("minecraft:bamboo_blocks");
    public static final ItemCategory BANNERS = get("minecraft:banners");
    public static final ItemCategory BEACON_PAYMENT_ITEMS = get("minecraft:beacon_payment_items");
    public static final ItemCategory BEDS = get("minecraft:beds");
    public static final ItemCategory BEE_FOOD = get("minecraft:bee_food");
    public static final ItemCategory BIRCH_LOGS = get("minecraft:birch_logs");
    public static final ItemCategory BOATS = get("minecraft:boats");
    public static final ItemCategory BOOKSHELF_BOOKS = get("minecraft:bookshelf_books");
    public static final ItemCategory BREAKS_DECORATED_POTS = get("minecraft:breaks_decorated_pots");
    public static final ItemCategory BUTTONS = get("minecraft:buttons");
    public static final ItemCategory CAMEL_FOOD = get("minecraft:camel_food");
    public static final ItemCategory CANDLES = get("minecraft:candles");
    @Deprecated public static final ItemCategory CARPETS = get("minecraft:carpets");
    public static final ItemCategory CAT_FOOD = get("minecraft:cat_food");
    public static final ItemCategory CHERRY_LOGS = get("minecraft:cherry_logs");
    public static final ItemCategory CHEST_ARMOR = get("minecraft:chest_armor");
    public static final ItemCategory CHEST_BOATS = get("minecraft:chest_boats");
    public static final ItemCategory CHICKEN_FOOD = get("minecraft:chicken_food");
    public static final ItemCategory CLUSTER_MAX_HARVESTABLES = get("minecraft:cluster_max_harvestables");
    public static final ItemCategory COAL_ORES = get("minecraft:coal_ores");
    public static final ItemCategory COALS = get("minecraft:coals");
    public static final ItemCategory COMPASSES = get("minecraft:compasses");
    public static final ItemCategory COMPLETES_FIND_TREE_TUTORIAL = get("minecraft:completes_find_tree_tutorial");
    public static final ItemCategory COPPER_ORES = get("minecraft:copper_ores");
    public static final ItemCategory COW_FOOD = get("minecraft:cow_food");
    public static final ItemCategory CREEPER_DROP_MUSIC_DISCS = get("minecraft:creeper_drop_music_discs");
    public static final ItemCategory CREEPER_IGNITERS = get("minecraft:creeper_igniters");
    public static final ItemCategory CRIMSON_STEMS = get("minecraft:crimson_stems");
    public static final ItemCategory DAMPENS_VIBRATIONS = get("minecraft:dampens_vibrations");
    public static final ItemCategory DARK_OAK_LOGS = get("minecraft:dark_oak_logs");
    public static final ItemCategory DECORATED_POT_INGREDIENTS = get("minecraft:decorated_pot_ingredients");
    public static final ItemCategory DECORATED_POT_SHERDS = get("minecraft:decorated_pot_sherds");
    public static final ItemCategory DIAMOND_ORES = get("minecraft:diamond_ores");
    public static final ItemCategory DIRT = get("minecraft:dirt");
    public static final ItemCategory DOORS = get("minecraft:doors");
    public static final ItemCategory DYEABLE = get("minecraft:dyeable");
    public static final ItemCategory EMERALD_ORES = get("minecraft:emerald_ores");
    public static final ItemCategory ENCHANTABLE_ARMOR = get("minecraft:enchantable/armor");
    public static final ItemCategory ENCHANTABLE_BOW = get("minecraft:enchantable/bow");
    public static final ItemCategory ENCHANTABLE_CHEST_ARMOR = get("minecraft:enchantable/chest_armor");
    public static final ItemCategory ENCHANTABLE_CROSSBOW = get("minecraft:enchantable/crossbow");
    public static final ItemCategory ENCHANTABLE_DURABILITY = get("minecraft:enchantable/durability");
    public static final ItemCategory ENCHANTABLE_EQUIPPABLE = get("minecraft:enchantable/equippable");
    public static final ItemCategory ENCHANTABLE_FIRE_ASPECT = get("minecraft:enchantable/fire_aspect");
    public static final ItemCategory ENCHANTABLE_FISHING = get("minecraft:enchantable/fishing");
    public static final ItemCategory ENCHANTABLE_FOOT_ARMOR = get("minecraft:enchantable/foot_armor");
    public static final ItemCategory ENCHANTABLE_HEAD_ARMOR = get("minecraft:enchantable/head_armor");
    public static final ItemCategory ENCHANTABLE_LEG_ARMOR = get("minecraft:enchantable/leg_armor");
    public static final ItemCategory ENCHANTABLE_MINING = get("minecraft:enchantable/mining");
    public static final ItemCategory ENCHANTABLE_MINING_LOOT = get("minecraft:enchantable/mining_loot");
    public static final ItemCategory ENCHANTABLE_SHARP_WEAPON = get("minecraft:enchantable/sharp_weapon");
    public static final ItemCategory ENCHANTABLE_SWORD = get("minecraft:enchantable/sword");
    public static final ItemCategory ENCHANTABLE_TRIDENT = get("minecraft:enchantable/trident");
    public static final ItemCategory ENCHANTABLE_VANISHING = get("minecraft:enchantable/vanishing");
    public static final ItemCategory ENCHANTABLE_WEAPON = get("minecraft:enchantable/weapon");
    public static final ItemCategory FENCE_GATES = get("minecraft:fence_gates");
    public static final ItemCategory FENCES = get("minecraft:fences");
    public static final ItemCategory FISHES = get("minecraft:fishes");
    public static final ItemCategory FLOWERS = get("minecraft:flowers");
    public static final ItemCategory FOOT_ARMOR = get("minecraft:foot_armor");
    public static final ItemCategory FOX_FOOD = get("minecraft:fox_food");
    public static final ItemCategory FREEZE_IMMUNE_WEARABLES = get("minecraft:freeze_immune_wearables");
    public static final ItemCategory FROG_FOOD = get("minecraft:frog_food");
    @Deprecated public static final ItemCategory FURNACE_MATERIALS = get("minecraft:furnace_materials");
    public static final ItemCategory GOAT_FOOD = get("minecraft:goat_food");
    public static final ItemCategory GOLD_ORES = get("minecraft:gold_ores");
    public static final ItemCategory HANGING_SIGNS = get("minecraft:hanging_signs");
    public static final ItemCategory HEAD_ARMOR = get("minecraft:head_armor");
    public static final ItemCategory HOES = get("minecraft:hoes");
    public static final ItemCategory HOGLIN_FOOD = get("minecraft:hoglin_food");
    public static final ItemCategory HORSE_FOOD = get("minecraft:horse_food");
    public static final ItemCategory HORSE_TEMPT_ITEMS = get("minecraft:horse_tempt_items");
    public static final ItemCategory IGNORED_BY_PIGLIN_BABIES = get("minecraft:ignored_by_piglin_babies");
    public static final ItemCategory IRON_ORES = get("minecraft:iron_ores");
    public static final ItemCategory JUNGLE_LOGS = get("minecraft:jungle_logs");
    public static final ItemCategory LAPIS_ORES = get("minecraft:lapis_ores");
    public static final ItemCategory LEAVES = get("minecraft:leaves");
    public static final ItemCategory LECTERN_BOOKS = get("minecraft:lectern_books");
    public static final ItemCategory LEG_ARMOR = get("minecraft:leg_armor");
    public static final ItemCategory LLAMA_FOOD = get("minecraft:llama_food");
    public static final ItemCategory LLAMA_TEMPT_ITEMS = get("minecraft:llama_tempt_items");
    public static final ItemCategory LOGS = get("minecraft:logs");
    public static final ItemCategory LOGS_THAT_BURN = get("minecraft:logs_that_burn");
    public static final ItemCategory MANGROVE_LOGS = get("minecraft:mangrove_logs");
    public static final ItemCategory MEAT = get("minecraft:meat");
    public static final ItemCategory MUSIC_DISCS = get("minecraft:music_discs");
    public static final ItemCategory NON_FLAMMABLE_WOOD = get("minecraft:non_flammable_wood");
    public static final ItemCategory NOTEBLOCK_TOP_INSTRUMENTS = get("minecraft:noteblock_top_instruments");
    public static final ItemCategory OAK_LOGS = get("minecraft:oak_logs");
    @Deprecated public static final ItemCategory OCCLUDES_VIBRATION_SIGNALS = get("minecraft:occludes_vibration_signals");
    public static final ItemCategory OCELOT_FOOD = get("minecraft:ocelot_food");
    @Deprecated public static final ItemCategory OVERWORLD_NATURAL_LOGS = get("minecraft:overworld_natural_logs");
    public static final ItemCategory PANDA_FOOD = get("minecraft:panda_food");
    public static final ItemCategory PARROT_FOOD = get("minecraft:parrot_food");
    public static final ItemCategory PARROT_POISONOUS_FOOD = get("minecraft:parrot_poisonous_food");
    public static final ItemCategory PICKAXES = get("minecraft:pickaxes");
    public static final ItemCategory PIG_FOOD = get("minecraft:pig_food");
    public static final ItemCategory PIGLIN_FOOD = get("minecraft:piglin_food");
    public static final ItemCategory PIGLIN_LOVED = get("minecraft:piglin_loved");
    public static final ItemCategory PIGLIN_REPELLENTS = get("minecraft:piglin_repellents");
    public static final ItemCategory PLANKS = get("minecraft:planks");
    public static final ItemCategory RABBIT_FOOD = get("minecraft:rabbit_food");
    public static final ItemCategory RAILS = get("minecraft:rails");
    public static final ItemCategory REDSTONE_ORES = get("minecraft:redstone_ores");
    public static final ItemCategory SAND = get("minecraft:sand");
    public static final ItemCategory SAPLINGS = get("minecraft:saplings");
    public static final ItemCategory SHEEP_FOOD = get("minecraft:sheep_food");
    public static final ItemCategory SHOVELS = get("minecraft:shovels");
    public static final ItemCategory SIGNS = get("minecraft:signs");
    public static final ItemCategory SKULLS = get("minecraft:skulls");
    public static final ItemCategory SLABS = get("minecraft:slabs");
    public static final ItemCategory SMALL_FLOWERS = get("minecraft:small_flowers");
    public static final ItemCategory SMELTS_TO_GLASS = get("minecraft:smelts_to_glass");
    public static final ItemCategory SNIFFER_FOOD = get("minecraft:sniffer_food");
    public static final ItemCategory SOUL_FIRE_BASE_BLOCKS = get("minecraft:soul_fire_base_blocks");
    public static final ItemCategory SPRUCE_LOGS = get("minecraft:spruce_logs");
    public static final ItemCategory STAIRS = get("minecraft:stairs");
    public static final ItemCategory STONE_BRICKS = get("minecraft:stone_bricks");
    public static final ItemCategory STONE_BUTTONS = get("minecraft:stone_buttons");
    public static final ItemCategory STONE_CRAFTING_MATERIALS = get("minecraft:stone_crafting_materials");
    public static final ItemCategory STONE_TOOL_MATERIALS = get("minecraft:stone_tool_materials");
    public static final ItemCategory STRIDER_FOOD = get("minecraft:strider_food");
    public static final ItemCategory STRIDER_TEMPT_ITEMS = get("minecraft:strider_tempt_items");
    public static final ItemCategory SWORDS = get("minecraft:swords");
    public static final ItemCategory TALL_FLOWERS = get("minecraft:tall_flowers");
    public static final ItemCategory TERRACOTTA = get("minecraft:terracotta");
    @Deprecated public static final ItemCategory TOOLS = get("minecraft:tools");
    public static final ItemCategory TRAPDOORS = get("minecraft:trapdoors");
    public static final ItemCategory TRIM_MATERIALS = get("minecraft:trim_materials");
    public static final ItemCategory TRIM_TEMPLATES = get("minecraft:trim_templates");
    public static final ItemCategory TRIMMABLE_ARMOR = get("minecraft:trimmable_armor");
    public static final ItemCategory TURTLE_FOOD = get("minecraft:turtle_food");
    public static final ItemCategory VILLAGER_PLANTABLE_SEEDS = get("minecraft:villager_plantable_seeds");
    public static final ItemCategory WALLS = get("minecraft:walls");
    public static final ItemCategory WARPED_STEMS = get("minecraft:warped_stems");
    public static final ItemCategory WART_BLOCKS = get("minecraft:wart_blocks");
    public static final ItemCategory WOLF_FOOD = get("minecraft:wolf_food");
    public static final ItemCategory WOODEN_BUTTONS = get("minecraft:wooden_buttons");
    public static final ItemCategory WOODEN_DOORS = get("minecraft:wooden_doors");
    public static final ItemCategory WOODEN_FENCES = get("minecraft:wooden_fences");
    public static final ItemCategory WOODEN_PRESSURE_PLATES = get("minecraft:wooden_pressure_plates");
    public static final ItemCategory WOODEN_SLABS = get("minecraft:wooden_slabs");
    public static final ItemCategory WOODEN_STAIRS = get("minecraft:wooden_stairs");
    public static final ItemCategory WOODEN_TRAPDOORS = get("minecraft:wooden_trapdoors");
    public static final ItemCategory WOOL = get("minecraft:wool");
    public static final ItemCategory WOOL_CARPETS = get("minecraft:wool_carpets");

    private ItemCategories() {
    }

    /**
     * Gets the {@link ItemCategory} associated with the given id.
     */
    public static ItemCategory get(String id) {
        ItemCategory entry = ItemCategory.REGISTRY.get(id);
        if (entry == null) {
            return new ItemCategory(id);
        }
        return entry;
    }
}
