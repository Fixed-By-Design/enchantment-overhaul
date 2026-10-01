package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(Mob.class)
public class MobMixin {

    @Inject(method = "populateDefaultEquipmentEnchantments", at = @At("HEAD"), cancellable = true)
    private void enchantSpawnEquipment(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        ci.cancel();
        if (!level.getLevel().getGameRules().get(ModGameRules.MOB_GEAR_ENCHANTMENTS)) return;

        Mob self = (Mob) (Object) this;
        List<EquipmentSlot> equipped = EquipmentSlot.VALUES.stream().filter(slot -> !self.getItemBySlot(slot).isEmpty()).toList();
        if (equipped.isEmpty()) return;

        float regional = difficulty.getSpecialMultiplier();
        if (random.nextFloat() < enchantChance(level.getDifficulty(), regional)) {
            EquipmentSlot slot = Util.getRandom(equipped, random);
            ItemStack stack = self.getItemBySlot(slot);
            EnchantmentHelper.enchantItemFromProvider(stack, level.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, random);
            self.setItemSlot(slot, stack);
        }
        if (random.nextFloat() < upgradeChance(level.getDifficulty(), regional)) {
            EquipmentSlot slot = Util.getRandom(equipped, random);
            ItemStack stack = self.getItemBySlot(slot);
            List<UpgradeType> applicable = Arrays.stream(UpgradeType.values()).filter(type -> type.appliesTo(stack, level.getLevel())).toList();
            if (applicable.isEmpty()) return;
            UpgradeType upgrade = Util.getRandom(applicable, random);
            upgrade.applyTo(stack, upgrade.currentLevel(stack) + 1);
            self.setItemSlot(slot, stack);
        }
    }

    @Unique
    private static float enchantChance(Difficulty difficulty, float regional) {
        return switch (difficulty) {
            case PEACEFUL -> 0.0F;
            case EASY -> 0.10F + regional * 0.10F;
            case NORMAL -> 0.20F + regional * 0.15F;
            case HARD -> 0.35F + regional * 0.25F;
        };
    }

    @Unique
    private static float upgradeChance(Difficulty difficulty, float regional) {
        return switch (difficulty) {
            case PEACEFUL -> 0.0F;
            case EASY -> 0.05F + regional * 0.07F;
            case NORMAL -> 0.12F + regional * 0.10F;
            case HARD -> 0.22F + regional * 0.18F;
        };
    }
}
