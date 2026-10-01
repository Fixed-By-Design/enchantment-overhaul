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
    private @Nullable UpgradeType pendingType;

    private SmithingMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(menuType, containerId, inventory, access, slotDefinition);
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void applyCustomUpgrade(CallbackInfo ci) {
        pendingType = null;

        ItemStack template = this.inputSlots.getItem(0);
        ItemStack base = this.inputSlots.getItem(1);
        ItemStack material = this.inputSlots.getItem(2);

        if (template.isEmpty() || base.isEmpty() || material.isEmpty()) return;

        UpgradeType type = SmithingTemplates.getType(template.getItem());
        if (type == null) return;

        int level = SmithingTemplates.getMaterialLevel(material.getItem());
        if (level == 0) return;
        if (!type.appliesTo(base, this.player.level())) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }
        if (type.currentLevel(base) >= level) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }

        ItemStack result = base.copy();
        type.applyTo(result, level);
        this.resultSlots.setItem(0, result);
        pendingType = type;
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void grantAdvancement(Player player, ItemStack stack, CallbackInfo ci) {
        if (pendingType != null && player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantSmithingAdvancement(serverPlayer, pendingType);
        }
        pendingType = null;
    }
}
