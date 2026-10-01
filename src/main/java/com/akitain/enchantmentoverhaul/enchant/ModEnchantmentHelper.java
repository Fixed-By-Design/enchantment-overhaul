package com.akitain.enchantmentoverhaul.enchant;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ModEnchantmentHelper {

    private ModEnchantmentHelper() {}

    public static int getItemEnchantmentLevel(ResourceKey<Enchantment> enchantment, ItemStack stack) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet()) {
            if (entry.getKey().is(enchantment)) return entry.getIntValue();
        }
        return 0;
    }

    public static boolean hasEnchantment(ResourceKey<Enchantment> enchantment, ItemStack stack) {
        return getItemEnchantmentLevel(enchantment, stack) > 0;
    }
}
