package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.DisabledEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {

    @Inject(method = "generateEnchantmentBookTypesOnlyMaxLevel", at = @At("HEAD"), cancellable = true)
    private static void levelOneBooks(CreativeModeTab.Output output, HolderLookup<Enchantment> enchantments, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
        addEnabledBooks(output, enchantments, visibility);
        ci.cancel();
    }

    @Inject(method = "generateEnchantmentBookTypesAllLevels", at = @At("HEAD"), cancellable = true)
    private static void levelOneBooksSearch(CreativeModeTab.Output output, HolderLookup<Enchantment> enchantments, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
        addEnabledBooks(output, enchantments, visibility);
        ci.cancel();
    }

    @Unique
    private static void addEnabledBooks(CreativeModeTab.Output output, HolderLookup<Enchantment> enchantments, CreativeModeTab.TabVisibility visibility) {
        enchantments.listElements()
                .filter(entry -> !DisabledEnchantments.isDisabled(entry))
                .map(entry -> EnchantmentHelper.createBook(new EnchantmentInstance(entry, 1)))
                .forEach(stack -> output.accept(stack, visibility));
    }
}
