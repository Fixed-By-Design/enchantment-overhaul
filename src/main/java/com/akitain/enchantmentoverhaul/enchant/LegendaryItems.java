package com.akitain.enchantmentoverhaul.enchant;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public final class LegendaryItems {

    public static final ChatFormatting COLOR = ChatFormatting.GOLD;

    private LegendaryItems() {}

    public static boolean isLegendary(ItemStack stack) {
        return ModEnchantmentHelper.hasEnchantment(ModEnchantments.VENOM, stack)
                && ModEnchantmentHelper.hasEnchantment(Enchantments.FIRE_ASPECT, stack);
    }

    public static Component tooltip() {
        return Component.translatable("item.enchantment-overhaul.legendary.tooltip")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}
