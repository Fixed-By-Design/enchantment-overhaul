package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.ModAdvancements;
import com.akitain.enchantmentoverhaul.smithing.SmithingTemplates;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu {

    @Unique
    private @Nullable UpgradeType pendingUpgrade;

    private SmithingMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(menuType, containerId, inventory, access, slotDefinition);
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void applyUpgrade(CallbackInfo ci) {
        this.pendingUpgrade = null;
        UpgradeType upgrade = SmithingTemplates.getUpgrade(this.inputSlots.getItem(0).getItem());
        int level = SmithingTemplates.getMaterialLevel(this.inputSlots.getItem(2).getItem());
        ItemStack base = this.inputSlots.getItem(1);
        if (upgrade == null || level == 0 || base.isEmpty()) return;

        if (!upgrade.appliesTo(base, this.player.level()) || upgrade.currentLevel(base) >= level) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }
        ItemStack result = base.copy();
        upgrade.applyTo(result, level);
        this.resultSlots.setItem(0, result);
        this.pendingUpgrade = upgrade;
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void grantAdvancement(Player player, ItemStack stack, CallbackInfo ci) {
        if (this.pendingUpgrade != null && player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantSmithingAdvancement(serverPlayer, this.pendingUpgrade);
        }
        this.pendingUpgrade = null;
    }
}
