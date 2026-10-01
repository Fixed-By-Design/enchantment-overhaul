package com.akitain.enchantmentoverhaul;

import com.akitain.enchantmentoverhaul.client.CatalogueScreen;
import com.akitain.enchantmentoverhaul.client.ChiseledBookshelfHoverState;
import com.akitain.enchantmentoverhaul.client.ItemTooltips;
import com.akitain.enchantmentoverhaul.enchant.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screens.MenuScreens;

public class EnchantmentOverhaulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.CATALOGUE, CatalogueScreen::new);
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> ItemTooltips.append(stack, lines));
        ClientTickEvents.END_CLIENT_TICK.register(ChiseledBookshelfHoverState::tick);
    }
}
