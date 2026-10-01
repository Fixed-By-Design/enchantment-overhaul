package com.akitain.enchantmentoverhaul.enchant;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModMenus {

    public static final ExtendedMenuType<CatalogueMenu, CatalogueData> CATALOGUE = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "catalogue"),
            new ExtendedMenuType<>(CatalogueMenu::new, CatalogueData.STREAM_CODEC));

    private ModMenus() {}

    public static void bootstrap() {}
}
