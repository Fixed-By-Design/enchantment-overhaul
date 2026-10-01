package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.LegendaryItems;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class LegendaryNameMixin {

    @ModifyReturnValue(method = "getStyledHoverName", at = @At("RETURN"))
    private Component colorLegendaryName(Component name) {
        return LegendaryItems.isLegendary((ItemStack) (Object) this) ? name.copy().withStyle(LegendaryItems.COLOR) : name;
    }
}
