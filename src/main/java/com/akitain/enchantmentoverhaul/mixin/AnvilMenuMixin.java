package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.LegendaryItems;
import net.minecraft.core.component.DataComponents;
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
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow @Nullable private String itemName;
    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;
    @Shadow private boolean onlyRenaming;

    @Unique
    private static final boolean EASY_ANVILS = FabricLoader.getInstance().isModLoaded("easyanvils");
    @Unique
    private static final int REPAIR_XP_COST = 1;

    private AnvilMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(menuType, containerId, inventory, access, slotDefinition);
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
        int repairUnits = tryRepair(first, second, result);

        // Not a repair: let vanilla and other mods handle renames and unrelated
        // custom operations. Same-item combining was rejected above.
        if (repairUnits <= 0) return;

        tryRename(first, result);
        this.repairItemCountCost = repairUnits;
        this.onlyRenaming = false;
        result.remove(DataComponents.REPAIR_COST);
        this.cost.set(REPAIR_XP_COST);
        this.resultSlots.setItem(0, result);
        ci.cancel();
    }

    @Unique
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

    @Unique
    private void tryRename(ItemStack first, ItemStack result) {
        if (this.itemName == null || this.itemName.isBlank()) {
            result.remove(DataComponents.CUSTOM_NAME);
            return;
        }
        if (!this.itemName.equals(first.getHoverName().getString())) {
            result.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
        }
    }

    @Unique
    private void clearOutput(CallbackInfo ci) {
        this.repairItemCountCost = 0;
        this.onlyRenaming = false;
        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.cost.set(0);
        ci.cancel();
    }
}
