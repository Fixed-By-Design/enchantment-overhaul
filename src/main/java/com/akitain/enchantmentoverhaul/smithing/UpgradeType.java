package com.akitain.enchantmentoverhaul.smithing;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.component.ModComponents;
import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public enum UpgradeType implements StringRepresentable {
    HONING("honing", ModComponents.HONING_LEVEL, "honable"),
    WARDING("warding", ModComponents.WARDING_LEVEL, "wardable"),
    TEMPERING("tempering", ModComponents.TEMPERING_LEVEL, "temperable"),
    GRINDING("grinding", ModComponents.GRINDING_LEVEL, "grindable");

    public static final int MAX_LEVEL = 5;
    private static final Identifier GRINDING_MODIFIER_ID = Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "grinding");
    // Items ground before the modifier was namespaced still carry this id; it is replaced on the next upgrade.
    private static final Identifier LEGACY_GRINDING_MODIFIER_ID = Identifier.withDefaultNamespace("grinding");

    private final String name;
    private final DataComponentType<Integer> component;
    private final TagKey<Item> upgradeable;

    UpgradeType(String name, DataComponentType<Integer> component, String upgradeableTag) {
        this.name = name;
        this.component = component;
        this.upgradeable = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "upgradeable/" + upgradeableTag));
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public MutableComponent getDescription() {
        return Component.translatable("upgrade." + EnchantmentOverhaul.MOD_ID + "." + this.name);
    }

    public MutableComponent getFullname(int level) {
        return this.getDescription().append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
    }

    // Eligibility is data-driven through the enchantment-overhaul:upgradeable/* item tags.
    public boolean appliesTo(ItemStack stack, @Nullable Level level) {
        return stack.is(this.upgradeable) && (this != HONING || !stack.is(ItemTags.AXES) || honingOnAxes(level));
    }

    // Game rules only exist server-side; the client stays permissive and the server's result sync corrects it.
    private static boolean honingOnAxes(@Nullable Level level) {
        return !(level instanceof ServerLevel serverLevel) || serverLevel.getGameRules().get(ModGameRules.HONING_ON_AXES);
    }

    public int currentLevel(ItemStack stack) {
        return stack.getOrDefault(this.component, 0);
    }

    public void applyTo(ItemStack stack, int level) {
        int previousLevel = this.currentLevel(stack);
        stack.set(this.component, level);
        switch (this) {
            case HONING -> addBaseAttackDamage(stack, honingDamageBonus(level) - honingDamageBonus(previousLevel));
            case GRINDING -> setMiningEfficiencyBonus(stack, grindingEfficiencyBonus(level));
            default -> {}
        }
    }

    // Same curve as Sharpness and Power, which honing replaces.
    public static double honingDamageBonus(int level) {
        int capped = Math.min(level, MAX_LEVEL);
        return capped <= 0 ? 0.0 : 1.0 + (capped - 1) * 0.5;
    }

    // Same curve as Efficiency, which grinding replaces.
    private static double grindingEfficiencyBonus(int level) {
        int capped = Math.min(level, MAX_LEVEL);
        return capped <= 0 ? 0.0 : capped * capped + 1.0;
    }

    private static void addBaseAttackDamage(ItemStack stack, double bonus) {
        if (bonus == 0.0) return;

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        boolean boosted = false;
        for (ItemAttributeModifiers.Entry entry : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            AttributeModifier modifier = entry.modifier();
            if (modifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                modifier = new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, modifier.amount() + bonus, modifier.operation());
                boosted = true;
            }
            builder.add(entry.attribute(), modifier, entry.slot(), entry.display());
        }
        if (!boosted) {
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, bonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void setMiningEfficiencyBonus(ItemStack stack, double bonus) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.modifier().is(GRINDING_MODIFIER_ID) || entry.modifier().is(LEGACY_GRINDING_MODIFIER_ID)) continue;
            builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
        }
        builder.add(Attributes.MINING_EFFICIENCY, new AttributeModifier(GRINDING_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
}
