package com.akitain.enchantmentoverhaul;

import com.akitain.enchantmentoverhaul.command.UpgradeCommand;
import com.akitain.enchantmentoverhaul.component.ModComponents;
import com.akitain.enchantmentoverhaul.enchant.ModMenus;
import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import com.akitain.enchantmentoverhaul.loot.LootTableModifier;
import com.akitain.enchantmentoverhaul.smithing.SmithingTemplates;
import net.fabricmc.api.ModInitializer;

public class EnchantmentOverhaul implements ModInitializer {

    public static final String MOD_ID = "enchantment-overhaul";

    @Override
    public void onInitialize() {
        ModComponents.bootstrap();
        ModGameRules.bootstrap();
        ModMenus.bootstrap();
        SmithingTemplates.bootstrap();
        UpgradeCommand.register();
        LootTableModifier.register();
    }
}
