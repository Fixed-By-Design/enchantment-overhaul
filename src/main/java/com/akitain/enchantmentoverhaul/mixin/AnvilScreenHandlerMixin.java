package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.component.ModComponents;
import com.akitain.enchantmentoverhaul.enchant.LegendaryItems;
import com.akitain.enchantmentoverhaul.enchant.SlotSystem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {

    @Shadow @Nullable private String itemName;
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;
    @Shadow private boolean onlyRenaming;

    @Unique
    private static final boolean EASY_ANVILS = FabricLoader.getInstance().isModLoaded("easyanvils");

    private AnvilScreenHandlerMixin(@Nullable MenuType<?> type, int syncId, Inventory playerInventory, ContainerLevelAccess context, ItemCombinerMenuSlotDefinition forgingSlotsManager) {
        super(type, syncId, playerInventory, context, forgingSlotsManager);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void replaceAnvilLogic(CallbackInfo ci) {
        ItemStack first = this.inputSlots.getItem(0);
        ItemStack second = this.inputSlots.getItem(1);

        if (first.isEmpty()) return;

        if (LegendaryItems.isLegendary(first)) {
            clearOutput(ci);
            return;
        }

        // Enchanting stays exclusive to the Catalogue. Check the component rather
        // than the vanilla item so modded stored-enchantment items are blocked too.
        if (second.has(DataComponents.STORED_ENCHANTMENTS)) {
            clearOutput(ci);
            return;
        }

        // Vanilla uses a second copy both to repair durability and to transfer its
        // enchantments. Enchantment Overhaul intentionally removes that operation.
        if (!second.isEmpty() && second.is(first.getItem())) {
            clearOutput(ci);
            return;
        }

        // Easy Anvils owns its enhanced rename UI. Item-to-item combining remains
        // blocked by Enchantment Overhaul, even when Easy Anvils is installed.
        if (EASY_ANVILS && second.isEmpty()) return;

        ItemStack result = first.copy();
        int restoreCost = tryRestoreSlot(first, second, result);
        int repairUnits = tryRepair(first, second, result);

        // Not a mod-handled operation: let vanilla and other mods handle renames
        // and unrelated custom operations. Same-item combining was rejected above.
        if (restoreCost <= 0 && repairUnits <= 0) return;

        tryRename(first, result);

        int unitsConsumed = repairUnits > 0 ? repairUnits : 1;
        this.repairItemCountCost = unitsConsumed;
        this.onlyRenaming = false;

        result.remove(DataComponents.REPAIR_COST);
        this.cost.set(Math.max(1, restoreCost));
        this.resultSlots.setItem(0, result);
        ci.cancel();
    }

    private int tryRestoreSlot(ItemStack first, ItemStack second, ItemStack result) {
        int penalty = SlotSystem.getGrindstonePenalty(first);
        if (penalty <= 0 || second.isEmpty()) return 0;
        if (!first.isValidRepairItem(second)) return 0;

        result.set(ModComponents.GRINDSTONE_PENALTY, penalty - 1);
        return getRestoreCost(first);
    }

    private static int getRestoreCost(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (id.startsWith("netherite_")) return 10;
        if (id.startsWith("diamond_")) return 8;
        if (id.startsWith("golden_")) return 5;
        if (id.startsWith("iron_") || id.startsWith("chainmail_")) return 5;
        if (id.startsWith("copper_")) return 3;
        return 2;
    }

    private int tryRepair(ItemStack first, ItemStack second, ItemStack result) {
        if (second.isEmpty() || !first.isDamageableItem() || !first.isValidRepairItem(second)) return 0;

        int repairPerUnit = first.getMaxDamage() / 4;
        int damage = first.getDamageValue();
        if (repairPerUnit <= 0 || damage <= 0) return 0;

        int units = 0;
        while (units < second.getCount() && damage > 0) {
            damage = Math.max(0, damage - repairPerUnit);
            units++;
        }

        result.setDamageValue(damage);
        return units;
    }

    private void tryRename(ItemStack first, ItemStack result) {
        if (this.itemName == null || this.itemName.isBlank()) {
            result.remove(DataComponents.CUSTOM_NAME);
            return;
        }
        if (!this.itemName.equals(first.getHoverName().getString())) {
            result.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
        }
    }

    private void clearOutput(CallbackInfo ci) {
        this.repairItemCountCost = 0;
        this.onlyRenaming = false;
        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.cost.set(0);
        ci.cancel();
    }
}
