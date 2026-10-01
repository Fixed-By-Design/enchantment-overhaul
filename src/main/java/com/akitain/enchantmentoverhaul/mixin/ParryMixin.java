package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Parry: blocking within a short window right after raising an enchanted shield pushes the attacker
// away and costs the shield no durability. Holding the shield up past the window blocks as usual.
@Mixin(LivingEntity.class)
public abstract class ParryMixin {

    @Unique
    private static final int PARRY_WINDOW_TICKS = 15;
    @Unique
    private static final double PARRY_KNOCKBACK = 1.0;

    @Shadow @Nullable public abstract ItemStack getItemBlockingWith();

    @Shadow public abstract int getTicksUsingItem();

    @Inject(method = "blockUsingItem", at = @At("HEAD"))
    private void knockBackParriedAttacker(ServerLevel level, LivingEntity attacker, CallbackInfo ci) {
        if (!isParrying(getItemBlockingWith())) return;
        LivingEntity defender = (LivingEntity) (Object) this;
        attacker.knockback(PARRY_KNOCKBACK, defender.getX() - attacker.getX(), defender.getZ() - attacker.getZ());
    }

    @WrapWithCondition(method = "applyItemBlocking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V"))
    private boolean skipParryDurabilityLoss(BlocksAttacks blocksAttacks, Level level, ItemStack item, LivingEntity user, InteractionHand hand, float damage) {
        return !isParrying(item);
    }

    // The window opens once the shield actually starts blocking and lasts PARRY_WINDOW_TICKS.
    @Unique
    private boolean isParrying(@Nullable ItemStack stack) {
        if (stack == null || !ModEnchantmentHelper.hasEnchantment(ModEnchantments.PARRY, stack)) return false;
        BlocksAttacks blocksAttacks = stack.get(DataComponents.BLOCKS_ATTACKS);
        return blocksAttacks != null && getTicksUsingItem() <= blocksAttacks.blockDelayTicks() + PARRY_WINDOW_TICKS;
    }
}
