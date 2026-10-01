package com.akitain.enchantmentoverhaul.client;

import com.akitain.enchantmentoverhaul.enchant.InnateMaterialProperties;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ItemTooltips {

    private static final Component ENCHANTED_BOOK_SUBTITLE = Component.translatable("item.minecraft.enchanted_book")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);

    private ItemTooltips() {}

    public static void append(ItemStack stack, List<Component> lines) {
        addUpgrades(stack, lines);
        addInnateResistance(stack, lines);
        addEnchantedBookSubtitle(stack, lines);
    }

    private static void addUpgrades(ItemStack stack, List<Component> lines) {
        for (UpgradeType upgrade : UpgradeType.values()) {
            int level = upgrade.currentLevel(stack);
            if (level > 0) lines.add(upgrade.getFullname(level).withStyle(ChatFormatting.BLUE));
        }
    }

    private static void addInnateResistance(ItemStack stack, List<Component> lines) {
        if (!stack.is(ItemTags.ARMOR_ENCHANTABLE)) return;
        String material = InnateMaterialProperties.getMaterial(stack);
        Component resistance = material == null ? null : InnateMaterialProperties.getResistanceName(material);
        if (resistance == null) return;
        lines.add(Component.translatable("item.enchantment-overhaul.innate_resistance", resistance, InnateMaterialProperties.PERCENT_PER_PIECE)
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private static void addEnchantedBookSubtitle(ItemStack stack, List<Component> lines) {
        ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (!stack.is(Items.ENCHANTED_BOOK) || enchantments == null || enchantments.isEmpty() || lines.isEmpty()) return;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            String fullname = Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString();
            for (int i = lines.size() - 1; i >= 1; i--) {
                if (lines.get(i).getString().equals(fullname)) lines.remove(i);
            }
        }
        lines.add(1, ENCHANTED_BOOK_SUBTITLE);
    }
}
