package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Honing on a bow or crossbow replaces the disabled Power enchantment: the fired arrow gets extra base damage
// scaled by the weapon's honing level. Runs for both weapons since crossbow reuses this createProjectile.
@Mixin(ProjectileWeaponItem.class)
public class HoningProjectileMixin {

    @Inject(method = "createProjectile", at = @At("RETURN"))
    private void addHoningDamage(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit, CallbackInfoReturnable<Projectile> cir) {
        int honing = UpgradeType.HONING.currentLevel(weapon);
        if (honing <= 0) return;
        if (cir.getReturnValue() instanceof AbstractArrow arrow) {
            double base = ((AbstractArrowBaseDamageAccessor) arrow).eo$getBaseDamage();
            arrow.setBaseDamage(base + UpgradeType.honingDamageBonus(honing));
        }
    }
}
