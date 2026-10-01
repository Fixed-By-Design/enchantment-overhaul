package com.akitain.enchantmentoverhaul.smithing;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.Nullable;

public final class SmithingTemplates {

    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    private static final Identifier EMPTY_SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    private static final Identifier EMPTY_SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
    private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.withDefaultNamespace("container/slot/pickaxe");
    private static final Identifier EMPTY_SLOT_AXE = Identifier.withDefaultNamespace("container/slot/axe");
    private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.withDefaultNamespace("container/slot/shovel");
    private static final Identifier EMPTY_SLOT_HOE = Identifier.withDefaultNamespace("container/slot/hoe");
    private static final Identifier EMPTY_SLOT_SPEAR = Identifier.withDefaultNamespace("container/slot/spear");
    private static final Identifier EMPTY_SLOT_INGOT = Identifier.withDefaultNamespace("container/slot/ingot");

    public static final Item HONING_TEMPLATE = register(UpgradeType.HONING,
            List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_AXE, EMPTY_SLOT_SPEAR));
    public static final Item WARDING_TEMPLATE = register(UpgradeType.WARDING,
            List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS));
    public static final Item TEMPERING_TEMPLATE = register(UpgradeType.TEMPERING,
            List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS,
                    EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_SHOVEL, EMPTY_SLOT_HOE, EMPTY_SLOT_SPEAR));
    public static final Item GRINDING_TEMPLATE = register(UpgradeType.GRINDING,
            List.of(EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_SHOVEL, EMPTY_SLOT_HOE));

    private static final Map<Item, UpgradeType> UPGRADE_BY_TEMPLATE = Map.of(
            HONING_TEMPLATE, UpgradeType.HONING,
            WARDING_TEMPLATE, UpgradeType.WARDING,
            TEMPERING_TEMPLATE, UpgradeType.TEMPERING,
            GRINDING_TEMPLATE, UpgradeType.GRINDING
    );

    private static final Map<Item, Integer> LEVEL_BY_MATERIAL = Map.of(
            Items.COPPER_INGOT, 1,
            Items.IRON_INGOT, 2,
            Items.GOLD_INGOT, 3,
            Items.DIAMOND, 4,
            Items.NETHERITE_INGOT, 5
    );

    private SmithingTemplates() {}

    private static Item register(UpgradeType upgrade, List<Identifier> baseSlotEmptyIcons) {
        String name = upgrade.getSerializedName();
        String descriptionId = "item." + EnchantmentOverhaul.MOD_ID + ".smithing_template." + name + ".";
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, name + "_template"));
        return Registry.register(BuiltInRegistries.ITEM, key, new SmithingTemplateItem(
                Component.translatable(descriptionId + "applies_to").withStyle(DESCRIPTION_FORMAT),
                Component.translatable(descriptionId + "ingredients").withStyle(DESCRIPTION_FORMAT),
                Component.translatable(descriptionId + "base_slot_description"),
                Component.translatable(descriptionId + "additions_slot_description"),
                baseSlotEmptyIcons,
                List.of(EMPTY_SLOT_INGOT),
                new Item.Properties().setId(key).rarity(Rarity.UNCOMMON)));
    }

    public static @Nullable UpgradeType getUpgrade(Item template) {
        return UPGRADE_BY_TEMPLATE.get(template);
    }

    public static int getMaterialLevel(Item material) {
        return LEVEL_BY_MATERIAL.getOrDefault(material, 0);
    }

    public static void bootstrap() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.insertBefore(
                Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, HONING_TEMPLATE, WARDING_TEMPLATE, TEMPERING_TEMPLATE, GRINDING_TEMPLATE));
    }
}
