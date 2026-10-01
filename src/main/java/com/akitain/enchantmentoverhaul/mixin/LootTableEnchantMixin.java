package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.loot.LootEnchanter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootTable.class)
public class LootTableEnchantMixin {

    @WrapOperation(
            method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootPool;addRandomItems(Ljava/util/function/Consumer;Lnet/minecraft/world/level/storage/loot/LootContext;)V")
    )
    private void enchantGeneratedEquipment(LootPool pool, Consumer<ItemStack> output, LootContext context, Operation<Void> original) {
        Consumer<ItemStack> enchantingOutput = stack -> {
            LootEnchanter.tryEnchant(stack, context);
            output.accept(stack);
        };
        original.call(pool, enchantingOutput, context);
    }
}
