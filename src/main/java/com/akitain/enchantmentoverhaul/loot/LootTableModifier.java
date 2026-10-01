package com.akitain.enchantmentoverhaul.loot;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import com.akitain.enchantmentoverhaul.smithing.SmithingTemplates;
import java.util.List;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class LootTableModifier {

    private record BookLoot(ResourceKey<LootTable> table, int emptyWeight, int bookWeight, List<ResourceKey<Enchantment>> enchantments) {}

    private record TemplateLoot(ResourceKey<LootTable> table, int emptyWeight, int templateWeight, List<Item> templates) {}

    private static final List<BookLoot> BOOK_LOOT = List.of(
            // Easy (15% chance)
            new BookLoot(BuiltInLootTables.VILLAGE_TEMPLE, 85, 5, List.of(
                    Enchantments.FEATHER_FALLING, ModEnchantments.STEP_UP, Enchantments.KNOCKBACK)),
            new BookLoot(BuiltInLootTables.VILLAGE_WEAPONSMITH, 85, 5, List.of(
                    Enchantments.KNOCKBACK)),
            new BookLoot(BuiltInLootTables.VILLAGE_ARMORER, 85, 5, List.of(
                    Enchantments.FEATHER_FALLING, ModEnchantments.STEP_UP)),
            new BookLoot(BuiltInLootTables.VILLAGE_FISHER, 85, 5, List.of(
                    Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE)),
            new BookLoot(BuiltInLootTables.VILLAGE_PLAINS_HOUSE, 95, 2, List.of(
                    Enchantments.FEATHER_FALLING, Enchantments.KNOCKBACK, ModEnchantments.STEP_UP)),
            new BookLoot(BuiltInLootTables.VILLAGE_DESERT_HOUSE, 95, 2, List.of(
                    Enchantments.FEATHER_FALLING, Enchantments.KNOCKBACK, ModEnchantments.STEP_UP)),
            new BookLoot(BuiltInLootTables.VILLAGE_SAVANNA_HOUSE, 95, 2, List.of(
                    Enchantments.FEATHER_FALLING, Enchantments.KNOCKBACK, ModEnchantments.STEP_UP)),
            new BookLoot(BuiltInLootTables.VILLAGE_SNOWY_HOUSE, 95, 2, List.of(
                    Enchantments.FEATHER_FALLING, Enchantments.KNOCKBACK, ModEnchantments.STEP_UP)),
            new BookLoot(BuiltInLootTables.VILLAGE_TAIGA_HOUSE, 95, 2, List.of(
                    Enchantments.FEATHER_FALLING, Enchantments.KNOCKBACK, ModEnchantments.STEP_UP)),
            new BookLoot(BuiltInLootTables.IGLOO_CHEST, 85, 15, List.of(
                    Enchantments.FROST_WALKER)),
            new BookLoot(BuiltInLootTables.SHIPWRECK_TREASURE, 85, 5, List.of(
                    Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE)),
            new BookLoot(BuiltInLootTables.SHIPWRECK_SUPPLY, 90, 5, List.of(
                    Enchantments.LUCK_OF_THE_SEA, ModEnchantments.BURNISHING)),

            // Medium (25% chance)
            new BookLoot(BuiltInLootTables.SIMPLE_DUNGEON, 75, 12, List.of(
                    Enchantments.KNOCKBACK, Enchantments.THORNS, Enchantments.PUNCH)),
            new BookLoot(BuiltInLootTables.RUINED_PORTAL, 75, 12, List.of(
                    ModEnchantments.CURSE_OF_FRAGILITY, Enchantments.VANISHING_CURSE)),
            new BookLoot(BuiltInLootTables.UNDERWATER_RUIN_SMALL, 80, 8, List.of(
                    Enchantments.AQUA_AFFINITY, Enchantments.RESPIRATION, ModEnchantments.BURNISHING)),
            new BookLoot(BuiltInLootTables.DESERT_PYRAMID, 75, 12, List.of(
                    Enchantments.CHANNELING, ModEnchantments.CURSE_OF_FRAGILITY, Enchantments.FIRE_ASPECT)),
            new BookLoot(BuiltInLootTables.JUNGLE_TEMPLE, 75, 12, List.of(
                    Enchantments.THORNS, Enchantments.VANISHING_CURSE, Enchantments.LOOTING, ModEnchantments.CURSE_OF_HUNGER)),
            new BookLoot(BuiltInLootTables.UNDERWATER_RUIN_BIG, 75, 8, List.of(
                    Enchantments.DEPTH_STRIDER, Enchantments.AQUA_AFFINITY, Enchantments.RESPIRATION, Enchantments.RIPTIDE, Enchantments.LOYALTY)),
            new BookLoot(BuiltInLootTables.ABANDONED_MINESHAFT, 75, 12, List.of(
                    Enchantments.SILK_TOUCH, Enchantments.FORTUNE, ModEnchantments.BURNISHING)),
            new BookLoot(BuiltInLootTables.PILLAGER_OUTPOST, 75, 6, List.of(
                    Enchantments.MULTISHOT, Enchantments.PIERCING, Enchantments.QUICK_CHARGE, Enchantments.SWEEPING_EDGE, Enchantments.PUNCH,
                    ModEnchantments.PARRY)),

            // Hard (35% chance)
            new BookLoot(BuiltInLootTables.BURIED_TREASURE, 65, 17, List.of(
                    Enchantments.RIPTIDE, Enchantments.LOYALTY, Enchantments.DEPTH_STRIDER)),
            new BookLoot(BuiltInLootTables.NETHER_BRIDGE, 65, 12, List.of(
                    Enchantments.FIRE_ASPECT, Enchantments.FLAME, ModEnchantments.VENOM, Enchantments.LOOTING)),
            new BookLoot(BuiltInLootTables.WOODLAND_MANSION, 65, 12, List.of(
                    Enchantments.SWEEPING_EDGE, Enchantments.LOOTING, ModEnchantments.CURSE_OF_HUNGER, Enchantments.SILK_TOUCH, Enchantments.THORNS, ModEnchantments.WRAITH)),
            new BookLoot(BuiltInLootTables.BASTION_TREASURE, 65, 35, List.of(
                    Enchantments.SOUL_SPEED)),
            new BookLoot(BuiltInLootTables.BASTION_BRIDGE, 75, 12, List.of(
                    Enchantments.SOUL_SPEED)),
            new BookLoot(BuiltInLootTables.BASTION_HOGLIN_STABLE, 75, 12, List.of(
                    Enchantments.SOUL_SPEED, Enchantments.FIRE_ASPECT)),
            new BookLoot(BuiltInLootTables.BASTION_OTHER, 80, 10, List.of(
                    Enchantments.SOUL_SPEED)),
            new BookLoot(BuiltInLootTables.STRONGHOLD_LIBRARY, 65, 17, List.of(
                    Enchantments.INFINITY, Enchantments.FORTUNE)),
            new BookLoot(BuiltInLootTables.STRONGHOLD_CORRIDOR, 75, 12, List.of(
                    Enchantments.FORTUNE)),
            new BookLoot(BuiltInLootTables.STRONGHOLD_CROSSING, 75, 12, List.of(
                    Enchantments.INFINITY)),

            // Endgame (50% chance)
            new BookLoot(BuiltInLootTables.ANCIENT_CITY, 50, 17, List.of(
                    ModEnchantments.VEIL, ModEnchantments.WRAITH, Enchantments.SWIFT_SNEAK, Enchantments.BINDING_CURSE)),
            new BookLoot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE, 50, 12, List.of(
                    Enchantments.WIND_BURST, Enchantments.BREACH, Enchantments.LUNGE, ModEnchantments.LAST_STAND,
                    ModEnchantments.PARRY)),
            new BookLoot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE, 50, 12, List.of(
                    Enchantments.WIND_BURST, Enchantments.BREACH, Enchantments.LUNGE, ModEnchantments.LAST_STAND,
                    ModEnchantments.PARRY)),
            new BookLoot(BuiltInLootTables.END_CITY_TREASURE, 50, 50, List.of(
                    Enchantments.MENDING))
    );

    private static final List<TemplateLoot> TEMPLATE_LOOT = List.of(
            new TemplateLoot(BuiltInLootTables.VILLAGE_WEAPONSMITH, 80, 20, List.of(SmithingTemplates.HONING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.PILLAGER_OUTPOST, 75, 25, List.of(SmithingTemplates.HONING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.STRONGHOLD_LIBRARY, 70, 30, List.of(SmithingTemplates.HONING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.SIMPLE_DUNGEON, 80, 20, List.of(SmithingTemplates.HONING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.VILLAGE_ARMORER, 80, 20, List.of(SmithingTemplates.WARDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.BASTION_TREASURE, 70, 30, List.of(SmithingTemplates.WARDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.BURIED_TREASURE, 75, 25, List.of(SmithingTemplates.WARDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.JUNGLE_TEMPLE, 75, 25, List.of(SmithingTemplates.WARDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.VILLAGE_TOOLSMITH, 80, 10, List.of(SmithingTemplates.TEMPERING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.DESERT_PYRAMID, 75, 25, List.of(SmithingTemplates.TEMPERING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.SHIPWRECK_TREASURE, 80, 20, List.of(SmithingTemplates.TEMPERING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.ABANDONED_MINESHAFT, 75, 12, List.of(SmithingTemplates.TEMPERING_TEMPLATE, SmithingTemplates.GRINDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.SIMPLE_DUNGEON, 80, 20, List.of(SmithingTemplates.GRINDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.DESERT_PYRAMID, 75, 25, List.of(SmithingTemplates.GRINDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.RUINED_PORTAL, 75, 25, List.of(SmithingTemplates.GRINDING_TEMPLATE)),
            new TemplateLoot(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_RARE, 70, 15, List.of(SmithingTemplates.GRINDING_TEMPLATE))
    );

    // Vanilla ships several of these tables through its built-in datapack, so the table source is not checked.
    public static void register() {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            HolderLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
            for (BookLoot loot : BOOK_LOOT) {
                if (loot.table.equals(key)) table.withPool(bookPool(enchantments, loot));
            }
            for (TemplateLoot loot : TEMPLATE_LOOT) {
                if (loot.table.equals(key)) table.withPool(templatePool(loot));
            }
        });
    }

    private static LootPool.Builder bookPool(HolderLookup<Enchantment> enchantments, BookLoot loot) {
        LootPool.Builder pool = LootPool.lootPool().add(EmptyLootItem.emptyItem().setWeight(loot.emptyWeight));
        for (ResourceKey<Enchantment> enchantment : loot.enchantments) {
            pool.add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                    .apply(new SetEnchantmentsFunction.Builder().withEnchantment(enchantments.getOrThrow(enchantment), ConstantValue.exactly(1)))
                    .setWeight(loot.bookWeight));
        }
        return pool;
    }

    private static LootPool.Builder templatePool(TemplateLoot loot) {
        LootPool.Builder pool = LootPool.lootPool().add(EmptyLootItem.emptyItem().setWeight(loot.emptyWeight));
        for (Item template : loot.templates) {
            pool.add(LootItem.lootTableItem(template).setWeight(loot.templateWeight));
        }
        return pool;
    }
}
