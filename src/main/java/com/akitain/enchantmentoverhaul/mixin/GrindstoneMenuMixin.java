package com.akitain.enchantmentoverhaul.mixin;

import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {

    @Inject(method = "removeNonCursesFrom", at = @At("RETURN"))
    private void removeCursesToo(ItemStack item, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty()) EnchantmentHelper.updateEnchantments(result, enchantments -> enchantments.removeIf(enchantment -> true));
    }
}
