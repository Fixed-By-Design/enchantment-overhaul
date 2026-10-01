package com.akitain.enchantmentoverhaul.smithing;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.component.ModComponents;
import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum UpgradeType {
    HONING(ModComponents.HONING_LEVEL),
    WARDING(ModComponents.WARDING_LEVEL),
    TEMPERING(ModComponents.TEMPERING_LEVEL),
    GRINDING(ModComponents.GRINDING_LEVEL);

    public static final int MAX_LEVEL = 5;

    private final DataComponentType<Integer> component;

    UpgradeType(DataComponentType<Integer> component) {
        this.component = component;
    }

    public MutableComponent getDescription() {
        return Component.translatable("upgrade." + EnchantmentOverhaul.MOD_ID + "." + name().toLowerCase(Locale.ROOT));
    }

    public MutableComponent getFullname(int level) {
        return getDescription().append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
    }

    // Eligibility is data-driven: add items to the matching enchantment-overhaul:upgradeable tag.
    // The shipped tags point at the vanilla enchantable tags, so behaviour is unchanged out of the box.
    public boolean appliesTo(ItemStack stack, @Nullable Level level) {
        if (!stack.is(upgradeableTag())) return false;
        return this != HONING || honingOnAxes(level) || !stack.is(ItemTags.AXES);
    }

    private TagKey<Item> upgradeableTag() {
        String name = switch (this) {
            case HONING -> "honable";
            case WARDING -> "wardable";
            case TEMPERING -> "temperable";
            case GRINDING -> "grindable";
        };
        return TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "upgradeable/" + name));
    }

    // Game rules only exist server-side; the client stays permissive and the server's result sync corrects it.
    private static boolean honingOnAxes(@Nullable Level level) {
        if (ModGameRules.HONING_ON_AXES == null) return true;
        if (!(level instanceof ServerLevel serverLevel)) return true;
        return serverLevel.getGameRules().get(ModGameRules.HONING_ON_AXES);
    }

    public int currentLevel(ItemStack stack) {
        return stack.getOrDefault(component, 0);
    }

    public void applyTo(ItemStack stack, int level) {
        int oldLevel = currentLevel(stack);
        stack.set(component, level);

        switch (this) {
            case HONING -> boostBaseModifier(stack, Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID,
                    sharpnessBonus(level) - sharpnessBonus(oldLevel), EquipmentSlotGroup.MAINHAND);
            case WARDING -> {}
            case GRINDING -> replaceModifier(stack, Attributes.MINING_EFFICIENCY, efficiencyBonus(level), EquipmentSlotGroup.MAINHAND, "grinding");
            case TEMPERING -> {}
        }
    }

    private static double sharpnessBonus(int level) {
        int cappedLevel = Math.min(level, 5);
        return cappedLevel <= 0 ? 0.0 : 1.0 + (cappedLevel - 1) * 0.5;
    }

    // Bonus base damage honing grants to a fired arrow. Matches vanilla Power (disabled here), which honing replaces.
    public static double honingBonus(int level) {
        return sharpnessBonus(level);
    }

    private static double efficiencyBonus(int level) {
        int cappedLevel = Math.min(level, 5);
        return cappedLevel <= 0 ? 0.0 : cappedLevel * cappedLevel + 1.0;
    }

    private void boostBaseModifier(ItemStack stack, Holder<Attribute> attribute, Identifier baseId, double bonus, EquipmentSlotGroup slot) {
        if (bonus == 0.0) return;

        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        boolean found = false;
        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            if (entry.modifier().is(baseId)) {
                double newValue = entry.modifier().amount() + bonus;
                AttributeModifier boosted = new AttributeModifier(baseId, newValue, entry.modifier().operation());
                builder.add(entry.attribute(), boosted, entry.slot(), entry.display());
                found = true;
            } else {
                builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
            }
        }

        if (!found) {
            builder.add(attribute, new AttributeModifier(baseId, bonus, AttributeModifier.Operation.ADD_VALUE), slot);
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private void replaceModifier(ItemStack stack, Holder<Attribute> attribute, double value, EquipmentSlotGroup slot, String modName) {
        Identifier modId = Identifier.withDefaultNamespace(modName);
        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            if (!entry.modifier().is(modId)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
            }
        }
        builder.add(attribute, new AttributeModifier(modId, value, AttributeModifier.Operation.ADD_VALUE), slot);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
}
