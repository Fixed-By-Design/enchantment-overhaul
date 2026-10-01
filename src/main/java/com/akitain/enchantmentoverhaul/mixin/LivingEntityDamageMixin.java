package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.HumanoidArmor;
import com.akitain.enchantmentoverhaul.enchant.InnateMaterialProperties;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin {

    @Unique
    private static final int MAX_WARDING_LEVELS = 20;
    @Unique
    private static final float WARDING_REDUCTION_PER_LEVEL = 0.04f * 4.0f / 5.0f;
    @Unique
    private static final float LAST_STAND_HEALTH_THRESHOLD = 0.2f;
    @Unique
    private static final float LAST_STAND_REDUCTION_PER_LEVEL = 0.1f;

    // Target getDamageAfterArmorAbsorb, not actuallyHurt: Player overrides actuallyHurt without calling super,
    // so injecting there never runs for players. Both Player and LivingEntity route through this shared method.
    @ModifyVariable(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float applyCustomResistances(float amount, DamageSource source, float damage) {
        LivingEntity self = (LivingEntity) (Object) this;
        return amount
                * InnateMaterialProperties.getDamageMultiplier(self, source)
                * wardingMultiplier(self)
                * lastStandMultiplier(self);
    }

    @Unique
    private static float wardingMultiplier(LivingEntity entity) {
        int levels = Math.min(HumanoidArmor.sum(entity, UpgradeType.WARDING::currentLevel), MAX_WARDING_LEVELS);
        return 1.0f - levels * WARDING_REDUCTION_PER_LEVEL;
    }

    @Unique
    private static float lastStandMultiplier(LivingEntity entity) {
        if (entity.getHealth() > entity.getMaxHealth() * LAST_STAND_HEALTH_THRESHOLD) return 1.0f;
        int level = ModEnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LAST_STAND, entity.getItemBySlot(EquipmentSlot.CHEST));
        return 1.0f - level * LAST_STAND_REDUCTION_PER_LEVEL;
    }
}
