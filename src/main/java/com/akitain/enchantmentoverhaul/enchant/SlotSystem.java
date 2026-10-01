package com.akitain.enchantmentoverhaul.enchant;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.component.ModComponents;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class SlotSystem {

    // Slot budgets are data-driven: an item in enchantment-overhaul:slots/tier_N gets N slots.
    private static final int MIN_TIER = 3;
    private static final int MAX_TIER = 6;
    private static final int FALLBACK_SLOTS = 3;

    private static TagKey<Item> slotTier(int tier) {
        return TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "slots/tier_" + tier));
    }

    public static int getBaseMaxSlots(ItemStack stack) {
        if (!stack.isDamageableItem()) return 0;

        // Highest tier wins so an item listed in several tags still resolves predictably.
        for (int tier = MAX_TIER; tier >= MIN_TIER; tier--) {
            if (stack.is(slotTier(tier))) return tier;
        }

        if (stack.isEnchantable() || hasEnchantments(stack)) return FALLBACK_SLOTS;

        return 0;
    }

    public static int getGrindstonePenalty(ItemStack stack) {
        return stack.getOrDefault(ModComponents.GRINDSTONE_PENALTY, 0);
    }

    public static int getCurseBonus(ItemStack stack) {
        int bonus = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(stack).entrySet()) {
            if (entry.getKey().is(EnchantmentTags.CURSE)) bonus++;
        }
        return bonus;
    }

    public static int getUsedSlots(ItemStack stack) {
        int used = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(stack).entrySet()) {
            used += EnchantmentCosts.slotCost(entry.getKey(), entry.getIntValue());
        }
        return used;
    }

    public static int getMaxSlots(ItemStack stack) {
        return getBaseMaxSlots(stack) - getGrindstonePenalty(stack) + getCurseBonus(stack);
    }

    public static int getAvailableSlots(ItemStack stack) {
        return getMaxSlots(stack) - getUsedSlots(stack);
    }

    private static ItemEnchantments getEnchantments(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    private static boolean hasEnchantments(ItemStack stack) {
        return !getEnchantments(stack).isEmpty();
    }
}
