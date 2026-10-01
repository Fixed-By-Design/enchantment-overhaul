package com.akitain.enchantmentoverhaul;

import com.akitain.enchantmentoverhaul.component.ModComponents;
import com.akitain.enchantmentoverhaul.command.UpgradeCommand;
import com.akitain.enchantmentoverhaul.enchant.ModMenus;
import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import com.akitain.enchantmentoverhaul.loot.LootTableModifier;
import com.akitain.enchantmentoverhaul.smithing.SmithingTemplates;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentOverhaul implements ModInitializer {

    public static final String MOD_ID = "enchantment-overhaul";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModComponents.register();
        ModMenus.bootstrap();
        SmithingTemplates.register();
        ModGameRules.register();
        UpgradeCommand.register();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.insertBefore(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                    SmithingTemplates.HONING_TEMPLATE,
                    SmithingTemplates.WARDING_TEMPLATE,
                    SmithingTemplates.TEMPERING_TEMPLATE,
                    SmithingTemplates.GRINDING_TEMPLATE);
        });

        LootTableModifier.register();

        LOGGER.info("Enchantment Overhaul loaded");
    }
}
