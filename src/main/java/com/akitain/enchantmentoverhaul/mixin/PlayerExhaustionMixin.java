package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.HumanoidArmor;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class PlayerExhaustionMixin {

    @Unique
    private static final float HUNGER_PER_CURSED_PIECE = 0.3f;

    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float applyCurseOfHunger(float exhaustion) {
        int cursedPieces = HumanoidArmor.count((Player) (Object) this,
                stack -> ModEnchantmentHelper.hasEnchantment(ModEnchantments.CURSE_OF_HUNGER, stack));
        return exhaustion * (1.0f + HUNGER_PER_CURSED_PIECE * cursedPieces);
    }
}
