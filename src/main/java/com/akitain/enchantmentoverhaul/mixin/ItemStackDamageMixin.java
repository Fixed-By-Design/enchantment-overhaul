package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class ItemStackDamageMixin {

    @Unique
    private static final float ARMOR_DAMAGE_CHANCE = 0.6F;

    @ModifyReturnValue(method = "processDurabilityChange", at = @At("RETURN"))
    private int applyDurabilityModifiers(int damage, @Local(argsOnly = true) ServerLevel level) {
        ItemStack self = (ItemStack) (Object) this;
        int temperingLevel = UpgradeType.TEMPERING.currentLevel(self);
        if (temperingLevel > 0) damage = temperedDamage(self, damage, unbreakingEquivalentLevel(temperingLevel), level.getRandom());
        if (ModEnchantmentHelper.hasEnchantment(ModEnchantments.CURSE_OF_FRAGILITY, self)) damage *= 2;
        return damage;
    }

    @Unique
    private static int unbreakingEquivalentLevel(int temperingLevel) {
        return Math.max(1, Math.round(Math.min(temperingLevel, UpgradeType.MAX_LEVEL) * 3.0F / UpgradeType.MAX_LEVEL));
    }

    @Unique
    private static int temperedDamage(ItemStack stack, int damage, int unbreakingLevel, RandomSource random) {
        boolean armor = stack.is(ItemTags.ARMOR_ENCHANTABLE);
        int applied = 0;
        for (int i = 0; i < damage; i++) {
            if ((armor && random.nextFloat() < ARMOR_DAMAGE_CHANCE) || random.nextInt(unbreakingLevel + 1) == 0) applied++;
        }
        return applied;
    }
}
