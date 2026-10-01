package com.akitain.enchantmentoverhaul.enchant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import static java.util.Map.entry;

public class EnchantmentCosts {

    private static final Map<ResourceKey<Enchantment>, ReagentCost> REAGENTS = Map.ofEntries(
            entry(Enchantments.FIRE_ASPECT, cost(Items.BLAZE_POWDER, 4, 8)),
            entry(Enchantments.FLAME, cost(Items.BLAZE_POWDER, 4)),
            entry(Enchantments.CHANNELING, cost(Items.LIGHTNING_ROD, 2)),
            entry(Enchantments.FROST_WALKER, cost(Items.PACKED_ICE, 4, 8)),
            entry(Enchantments.THORNS, cost(Items.CACTUS, 8, 16, 24)),
            entry(Enchantments.FORTUNE, cost(Items.EMERALD, 8, 20, 36)),
            entry(Enchantments.LOOTING, cost(Items.ENDER_PEARL, 4, 8, 12)),
            entry(Enchantments.SILK_TOUCH, cost(Items.COBWEB, 2)),
            entry(Enchantments.LUCK_OF_THE_SEA, cost(Items.NAUTILUS_SHELL, 2, 4, 6)),
            entry(Enchantments.INFINITY, cost(Items.AMETHYST_SHARD, 16)),
            entry(Enchantments.DEPTH_STRIDER, cost(Items.PRISMARINE_SHARD, 4, 8, 12)),
            entry(Enchantments.SOUL_SPEED, cost(Items.SOUL_SAND, 4, 8, 12)),
            entry(Enchantments.SWIFT_SNEAK, cost(Items.AMETHYST_SHARD, 8, 16, 24)),
            entry(Enchantments.RIPTIDE, cost(Items.PRISMARINE_CRYSTALS, 4, 8, 12)),
            entry(Enchantments.LOYALTY, cost(Items.IRON_CHAIN, 2, 4, 6)),
            entry(Enchantments.MULTISHOT, cost(Items.FIREWORK_ROCKET, 4)),
            entry(Enchantments.PIERCING, cost(Items.ARROW, 8, 16, 24, 32)),
            entry(Enchantments.WIND_BURST, cost(Items.BREEZE_ROD, 2, 4, 6)),
            entry(Enchantments.RESPIRATION, cost(Items.PUFFERFISH, 2, 4, 6)),
            entry(Enchantments.AQUA_AFFINITY, cost(Items.PRISMARINE_CRYSTALS, 2)),
            entry(Enchantments.SWEEPING_EDGE, cost(Items.IRON_INGOT, 2, 4, 6)),
            entry(Enchantments.BREACH, cost(Items.BREEZE_ROD, 2, 4, 6, 8)),
            entry(Enchantments.KNOCKBACK, cost(Items.PISTON, 2, 4)),
            entry(Enchantments.PUNCH, cost(Items.SNOWBALL, 4, 8)),
            entry(Enchantments.LUNGE, cost(Items.SLIME_BALL, 4, 8, 12)),
            entry(Enchantments.FEATHER_FALLING, cost(Items.FEATHER, 4, 8, 12, 16)),
            entry(Enchantments.QUICK_CHARGE, cost(Items.REDSTONE, 4, 8, 12)),
            entry(Enchantments.LURE, cost(Items.TROPICAL_FISH, 2, 4, 6)),
            entry(Enchantments.MENDING, cost(Items.GHAST_TEAR, 4)),
            entry(Enchantments.BINDING_CURSE, cost(Items.IRON_CHAIN, 2)),
            entry(Enchantments.VANISHING_CURSE, cost(Items.PHANTOM_MEMBRANE, 2)),
            entry(ModEnchantments.STEP_UP, cost(Items.RABBIT_FOOT, 2)),
            entry(ModEnchantments.VENOM, cost(Items.SPIDER_EYE, 4, 8)),
            entry(ModEnchantments.LAST_STAND, cost(Items.GOLDEN_APPLE, 2, 4, 6)),
            entry(ModEnchantments.CURSE_OF_FRAGILITY, cost(Items.GLASS_PANE, 2)),
            entry(ModEnchantments.CURSE_OF_HUNGER, cost(Items.ROTTEN_FLESH, 2)),
            entry(ModEnchantments.VEIL, cost(Items.FERMENTED_SPIDER_EYE, 4)),
            entry(ModEnchantments.BURNISHING, cost(Items.HONEYCOMB, 4)),
            entry(ModEnchantments.WRAITH, cost(Items.PHANTOM_MEMBRANE, 2)),
            entry(ModEnchantments.PARRY, cost(Items.IRON_BARS, 2))
    );

    private static final int[] XP_BY_LEVEL = {0, 2, 4, 7, 10};

    private record ReagentCost(Item item, List<Integer> cumulativeCosts) {
        int baseCost(int level) {
            if (level <= 0) return 0;
            int size = cumulativeCosts.size();
            if (level <= size) return cumulativeCosts.get(level - 1);
            // Datapacks can raise vanilla max levels. Continue the final increment
            // instead of making those extra levels free or charging less than before.
            int last = cumulativeCosts.get(size - 1);
            int increment = size > 1 ? last - cumulativeCosts.get(size - 2) : last;
            return last + (level - size) * increment;
        }
    }

    private static ReagentCost cost(Item item, int... cumulativeCosts) {
        return new ReagentCost(item, Arrays.stream(cumulativeCosts).boxed().toList());
    }

    public static Item reagent(ResourceKey<Enchantment> key) {
        ReagentCost cost = REAGENTS.get(key);
        return cost == null ? Items.LAPIS_LAZULI : cost.item();
    }

    public static int baseReagentCost(ResourceKey<Enchantment> key, int level) {
        ReagentCost cost = REAGENTS.get(key);
        return cost == null ? Math.max(0, level) * 2 : cost.baseCost(level);
    }

    public static int reagentCost(ResourceKey<Enchantment> key, int level, int normalBookshelves) {
        int shelves = Math.clamp(normalBookshelves, 0, BookshelfScanner.MAX_DISCOUNT_BOOKSHELVES);
        int denominator = BookshelfScanner.MAX_DISCOUNT_BOOKSHELVES * 2;
        // Round cumulative prices before subtracting them for upgrades, so paying
        // in stages costs exactly as much as applying the final level directly.
        return Math.ceilDiv(baseReagentCost(key, level) * (denominator - shelves), denominator);
    }

    public static int xpCost(ResourceKey<Enchantment> key, int level) {
        if (key.equals(Enchantments.MENDING)) return 8;
        if (level >= XP_BY_LEVEL.length) return XP_BY_LEVEL[XP_BY_LEVEL.length - 1];
        return XP_BY_LEVEL[level];
    }

    public static boolean isReagent(Item item) {
        return item == Items.LAPIS_LAZULI || REAGENTS.values().stream().anyMatch(cost -> cost.item() == item);
    }

    public static int slotCost(Holder<Enchantment> enchantment, int level) {
        if (level <= 0 || enchantment.is(EnchantmentTags.CURSE)) return 0;
        return enchantment.is(Enchantments.MENDING) ? 3 : level;
    }

    public static int slotCost(Holder<Enchantment> enchantment, int newLevel, int oldLevel) {
        return Math.max(0, slotCost(enchantment, newLevel) - slotCost(enchantment, oldLevel));
    }

    public static int reagentCost(ResourceKey<Enchantment> key, int newLevel, int oldLevel, int normalBookshelves) {
        if (oldLevel <= 0) return reagentCost(key, newLevel, normalBookshelves);
        return Math.max(0, reagentCost(key, newLevel, normalBookshelves) - reagentCost(key, oldLevel, normalBookshelves));
    }

    public static int xpCost(ResourceKey<Enchantment> key, int newLevel, int oldLevel) {
        if (oldLevel <= 0) return xpCost(key, newLevel);
        return Math.max(0, xpCost(key, newLevel) - xpCost(key, oldLevel));
    }
}
