package com.akitain.enchantmentoverhaul.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public class EnchantmentMenuMixin {

    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void disableEnchanting(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    private void disableEnchantmentGeneration(Container inventory, CallbackInfo ci) {
        ci.cancel();
    }
}
