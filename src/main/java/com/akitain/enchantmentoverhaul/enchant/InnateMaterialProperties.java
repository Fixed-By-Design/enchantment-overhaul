package com.akitain.enchantmentoverhaul.enchant;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class InnateMaterialProperties {

    public static final int PERCENT_PER_PIECE = 5;

    public static float getDamageMultiplier(LivingEntity entity, DamageSource source) {
        int pieces = HumanoidArmor.count(entity, stack -> resists(stack, source));
        return 1.0f - pieces * PERCENT_PER_PIECE / 100.0f;
    }

    private static boolean resists(ItemStack stack, DamageSource source) {
        String material = getMaterial(stack);
        return material != null && resists(material, source);
    }

    public static String getMaterial(ItemStack stack) {
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String id = key.getPath();

        // Additional Additions rose gold armor: a gold alloy, given diamond-style explosion resistance.
        if (key.getNamespace().equals("additionaladditions") && id.startsWith("rose_gold_")) return "rose_gold";

        if (id.startsWith("netherite_")) return "netherite";
        if (id.startsWith("diamond_")) return "diamond";
        if (id.startsWith("golden_")) return "gold";
        if (id.startsWith("iron_")) return "iron";
        if (id.startsWith("chainmail_")) return "iron";
        if (id.startsWith("copper_")) return "copper";
        if (id.startsWith("leather_")) return "leather";
        return null;
    }

    public static boolean resists(String material, DamageSource source) {
        return switch (material) {
            case "netherite" -> source.is(DamageTypeTags.IS_FIRE);
            case "rose_gold" -> source.is(DamageTypeTags.IS_EXPLOSION);
            case "copper" -> isPoisonOrEffect(source);
            case "iron" -> source.is(DamageTypeTags.IS_PROJECTILE);
            case "diamond" -> source.is(DamageTypeTags.IS_EXPLOSION);
            case "gold" -> isMagicDamage(source);
            case "leather" -> source.is(DamageTypes.FALL);
            default -> false;
        };
    }

    private static boolean isMagicDamage(DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.DRAGON_BREATH);
    }

    private static boolean isPoisonOrEffect(DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC);
    }

    public static Component getResistanceName(String material) {
        String resistance = switch (material) {
            case "netherite" -> "fire";
            case "rose_gold", "diamond" -> "explosion";
            case "copper" -> "poison";
            case "iron" -> "projectile";
            case "gold" -> "magic";
            case "leather" -> "fall";
            default -> null;
        };
        return resistance == null ? null : Component.translatable("innate_resistance." + EnchantmentOverhaul.MOD_ID + "." + resistance);
    }
}
