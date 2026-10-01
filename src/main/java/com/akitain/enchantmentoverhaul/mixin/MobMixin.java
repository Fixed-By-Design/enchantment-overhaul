package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Mob.class)
public class MobMixin {

    @Inject(method = "populateDefaultEquipmentEnchantments", at = @At("HEAD"), cancellable = true)
    protected void overrideMobEnchantments(ServerLevelAccessor world, RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        ci.cancel();

        if (!world.getLevel().getGameRules().get(ModGameRules.MOB_GEAR_ENCHANTMENTS)) return;

        Mob self = (Mob) (Object) this;
        List<EquipmentSlot> equipped = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (!self.getItemBySlot(slot).isEmpty()) equipped.add(slot);
        }
        if (equipped.isEmpty()) return;

        Difficulty difficulty = world.getLevel().getDifficulty();
        float regional = localDifficulty.getSpecialMultiplier();

        float enchantChance = switch (difficulty) {
            case PEACEFUL -> 0f;
            case EASY -> 0.10f + regional * 0.10f;
            case NORMAL -> 0.20f + regional * 0.15f;
            case HARD -> 0.35f + regional * 0.25f;
        };

        float upgradeChance = switch (difficulty) {
            case PEACEFUL -> 0f;
            case EASY -> 0.05f + regional * 0.07f;
            case NORMAL -> 0.12f + regional * 0.10f;
            case HARD -> 0.22f + regional * 0.18f;
        };

        if (random.nextFloat() < enchantChance) {
            EquipmentSlot slot = equipped.get(random.nextInt(equipped.size()));
            ItemStack stack = self.getItemBySlot(slot);
            EnchantmentHelper.enchantItemFromProvider(stack, world.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, localDifficulty, random);
            self.setItemSlot(slot, stack);
        }

        if (random.nextFloat() < upgradeChance) {
            EquipmentSlot slot = equipped.get(random.nextInt(equipped.size()));
            ItemStack stack = self.getItemBySlot(slot);
            List<UpgradeType> applicable = new ArrayList<>();
            for (UpgradeType type : UpgradeType.values()) {
                if (type.appliesTo(stack, world.getLevel())) applicable.add(type);
            }
            if (!applicable.isEmpty()) {
                UpgradeType upgrade = applicable.get(random.nextInt(applicable.size()));
                upgrade.applyTo(stack, upgrade.currentLevel(stack) + 1);
                self.setItemSlot(slot, stack);
            }
        }
    }
}
