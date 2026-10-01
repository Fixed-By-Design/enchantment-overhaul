package com.akitain.enchantmentoverhaul;

import com.akitain.enchantmentoverhaul.client.CatalogueScreen;
import com.akitain.enchantmentoverhaul.client.ChiseledBookshelfHoverState;
import com.akitain.enchantmentoverhaul.enchant.InnateMaterialProperties;
import com.akitain.enchantmentoverhaul.enchant.ModScreenHandlers;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class EnchantmentOverhaulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.CATALOGUE, CatalogueScreen::new);

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            addUpgradeLines(stack, lines);
            addInnatePropertyLine(stack, lines);
            addEnchantedBookSubtitle(stack, lines);
        });

        ClientTickEvents.END_CLIENT_TICK.register(ChiseledBookshelfHoverState::tick);
    }

    private static void addUpgradeLines(ItemStack stack, java.util.List<Component> lines) {
        for (UpgradeType type : UpgradeType.values()) {
            int level = type.currentLevel(stack);
            if (level > 0) lines.add(type.getFullname(level).withStyle(ChatFormatting.BLUE));
        }
    }

    private static void addInnatePropertyLine(ItemStack stack, java.util.List<Component> lines) {
        if (!stack.is(net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE)) return;
        String material = InnateMaterialProperties.getMaterial(stack);
        if (material == null) return;
        Component name = InnateMaterialProperties.getResistanceName(material);
        if (name == null) return;
        lines.add(Component.translatable("item.enchantment-overhaul.innate_resistance", name, InnateMaterialProperties.PERCENT_PER_PIECE)
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private static void addEnchantedBookSubtitle(ItemStack stack, java.util.List<Component> lines) {
        if (!stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) return;
        var enchantments = stack.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty() || lines.isEmpty()) return;

        for (var entry : enchantments.entrySet()) {
            String fullname = net.minecraft.world.item.enchantment.Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString();
            for (int i = lines.size() - 1; i >= 1; i--) {
                if (lines.get(i).getString().equals(fullname)) lines.remove(i);
            }
        }

        lines.add(Math.min(1, lines.size()), Component.translatable("item.minecraft.enchanted_book").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

}
