package com.akitain.enchantmentoverhaul.smithing;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;

public class SmithingTemplates {

    private static final ChatFormatting DESC = ChatFormatting.BLUE;

    private static final Identifier SLOT_HELMET = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier SLOT_CHESTPLATE = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier SLOT_LEGGINGS = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier SLOT_BOOTS = Identifier.withDefaultNamespace("container/slot/boots");
    private static final Identifier SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
    private static final Identifier SLOT_PICKAXE = Identifier.withDefaultNamespace("container/slot/pickaxe");
    private static final Identifier SLOT_AXE = Identifier.withDefaultNamespace("container/slot/axe");
    private static final Identifier SLOT_SHOVEL = Identifier.withDefaultNamespace("container/slot/shovel");
    private static final Identifier SLOT_HOE = Identifier.withDefaultNamespace("container/slot/hoe");
    private static final Identifier SLOT_SPEAR = Identifier.withDefaultNamespace("container/slot/spear");
    private static final Identifier SLOT_INGOT = Identifier.withDefaultNamespace("container/slot/ingot");

    private static final List<Identifier> ARMOR_SLOTS = List.of(SLOT_HELMET, SLOT_CHESTPLATE, SLOT_LEGGINGS, SLOT_BOOTS);
    private static final List<Identifier> WEAPON_SLOTS = List.of(SLOT_SWORD, SLOT_AXE, SLOT_SPEAR);
    private static final List<Identifier> TOOL_SLOTS = List.of(SLOT_PICKAXE, SLOT_AXE, SLOT_SHOVEL, SLOT_HOE);
    private static final List<Identifier> ALL_EQUIPMENT_SLOTS = List.of(
            SLOT_HELMET, SLOT_CHESTPLATE, SLOT_LEGGINGS, SLOT_BOOTS,
            SLOT_SWORD, SLOT_PICKAXE, SLOT_AXE, SLOT_SHOVEL, SLOT_HOE, SLOT_SPEAR);
    private static final List<Identifier> MATERIAL_SLOTS = List.of(SLOT_INGOT);

    private static Component appliesTo(String key) {
        return Component.translatable("item." + EnchantmentOverhaul.MOD_ID + ".smithing_template." + key + ".applies_to").withStyle(DESC);
    }

    private static Component ingredients(String key) {
        return Component.translatable("item." + EnchantmentOverhaul.MOD_ID + ".smithing_template." + key + ".ingredients").withStyle(DESC);
    }

    private static Component baseSlot(String key) {
        return Component.translatable("item." + EnchantmentOverhaul.MOD_ID + ".smithing_template." + key + ".base_slot_description");
    }

    private static Component additionsSlot(String key) {
        return Component.translatable("item." + EnchantmentOverhaul.MOD_ID + ".smithing_template." + key + ".additions_slot_description");
    }

    public static final Item HONING_TEMPLATE = register("honing_template",
            appliesTo("honing"), ingredients("honing"), baseSlot("honing"), additionsSlot("honing"),
            WEAPON_SLOTS, MATERIAL_SLOTS);

    public static final Item WARDING_TEMPLATE = register("warding_template",
            appliesTo("warding"), ingredients("warding"), baseSlot("warding"), additionsSlot("warding"),
            ARMOR_SLOTS, MATERIAL_SLOTS);

    public static final Item TEMPERING_TEMPLATE = register("tempering_template",
            appliesTo("tempering"), ingredients("tempering"), baseSlot("tempering"), additionsSlot("tempering"),
            ALL_EQUIPMENT_SLOTS, MATERIAL_SLOTS);

    public static final Item GRINDING_TEMPLATE = register("grinding_template",
            appliesTo("grinding"), ingredients("grinding"), baseSlot("grinding"), additionsSlot("grinding"),
            TOOL_SLOTS, MATERIAL_SLOTS);

    private static final Map<Item, UpgradeType> TEMPLATE_TO_TYPE = Map.of(
            HONING_TEMPLATE, UpgradeType.HONING,
            WARDING_TEMPLATE, UpgradeType.WARDING,
            TEMPERING_TEMPLATE, UpgradeType.TEMPERING,
            GRINDING_TEMPLATE, UpgradeType.GRINDING
    );

    private static final Map<Item, Integer> MATERIAL_TO_LEVEL = Map.of(
            Items.COPPER_INGOT, 1,
            Items.IRON_INGOT, 2,
            Items.GOLD_INGOT, 3,
            Items.DIAMOND, 4,
            Items.NETHERITE_INGOT, 5
    );

    public static UpgradeType getType(Item template) {
        return TEMPLATE_TO_TYPE.get(template);
    }

    public static int getMaterialLevel(Item material) {
        return MATERIAL_TO_LEVEL.getOrDefault(material, 0);
    }

    private static Item register(String name, Component appliesTo, Component ingredients,
                                  Component baseSlotDesc, Component additionsSlotDesc,
                                  List<Identifier> baseSlotTextures, List<Identifier> additionsSlotTextures) {
        Identifier id = Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key,
                new SmithingTemplateItem(appliesTo, ingredients, baseSlotDesc, additionsSlotDesc,
                        baseSlotTextures, additionsSlotTextures, new Item.Properties().setId(key).rarity(Rarity.UNCOMMON)));
    }

    public static void register() {}
}
