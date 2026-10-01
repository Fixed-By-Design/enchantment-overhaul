package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class StepUpMixin {

    @Unique
    private static final Identifier STEP_UP_ID = Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "step_up");

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void applyStepUp(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        AttributeInstance attr = self.getAttribute(Attributes.STEP_HEIGHT);
        if (attr == null) return;

        boolean shouldApply = !self.isShiftKeyDown()
                && ModEnchantmentHelper.hasEnchantment(ModEnchantments.STEP_UP, self.getItemBySlot(EquipmentSlot.FEET));

        if (shouldApply && attr.getModifier(STEP_UP_ID) == null) {
            attr.addTransientModifier(new AttributeModifier(STEP_UP_ID, 0.4, AttributeModifier.Operation.ADD_VALUE));
        } else if (!shouldApply && attr.getModifier(STEP_UP_ID) != null) {
            attr.removeModifier(STEP_UP_ID);
        }
    }
}
