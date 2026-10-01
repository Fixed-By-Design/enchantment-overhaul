package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public class VeilDetectionMixin {

    @Shadow private double range;
    @Shadow private boolean testInvisible;

    // A non-positive range means unlimited: scaling it would hide the wearer from every check, including retaliation.
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void applyVeil(ServerLevel world, LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (attacker == null || target == null || this.range <= 0) return;

        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
        ItemEnchantments enchantments = helmet.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        boolean hasVeil = false;
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(ModEnchantments.VEIL)) { hasVeil = true; break; }
        }
        if (!hasVeil) return;

        double range = this.range;
        if (this.testInvisible) {
            double followRange = attacker.getAttributeValue(Attributes.FOLLOW_RANGE);
            range = Math.min(range, followRange);
        }

        double reducedRange = range * 0.25;
        if (attacker.distanceTo(target) > reducedRange) {
            cir.setReturnValue(false);
        }
    }
}
