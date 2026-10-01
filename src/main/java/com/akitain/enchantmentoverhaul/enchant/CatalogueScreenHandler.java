package com.akitain.enchantmentoverhaul.enchant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;

public class CatalogueScreenHandler extends AbstractContainerMenu {

    private final Container inputInventory = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            CatalogueScreenHandler.this.slotsChanged(this);
        }
    };
    private final Container outputInventory = new SimpleContainer(1);

    private final ContainerLevelAccess context;
    private final Player player;
    private final RegistryAccess registryManager;
    private final Set<Identifier> unlockedIds;
    private final int normalBookshelves;
    private final boolean xpCostEnabled;

    private List<CatalogueEntry> entries = List.of();
    private int selectedIndex = -1;
    private int selectedLevel = 1;

    public static CatalogueScreenHandler fromData(int syncId, Inventory playerInventory, CatalogueData data) {
        return new CatalogueScreenHandler(syncId, playerInventory, ContainerLevelAccess.NULL, data.unlocked(), data.normalBookshelves(), data.xpCostEnabled());
    }

    public CatalogueScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context,
                                   List<Identifier> unlocked, int normalBookshelves, boolean xpCostEnabled) {
        super(ModScreenHandlers.CATALOGUE, syncId);
        this.context = context;
        this.player = playerInventory.player;
        this.registryManager = playerInventory.player.registryAccess();
        this.unlockedIds = Set.copyOf(unlocked);
        this.normalBookshelves = normalBookshelves;
        this.xpCostEnabled = xpCostEnabled;

        this.addSlot(new Slot(this.inputInventory, 0, 9, 60) {
            @Override
            public int getMaxStackSize() { return 1; }
            @Override
            public boolean mayPlace(ItemStack stack) { return SlotSystem.getBaseMaxSlots(stack) > 0; }
        });
        this.addSlot(new Slot(this.inputInventory, 1, 31, 60) {
            @Override
            public boolean mayPlace(ItemStack stack) { return EnchantmentCosts.isReagent(stack.getItem()); }
        });

        this.addSlot(new Slot(this.outputInventory, 0, 20, 86) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public boolean mayPickup(Player player) {
                return CatalogueScreenHandler.this.canTakeOutput(player);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                CatalogueScreenHandler.this.onOutputTaken(player);
                super.onTake(player, stack);
            }
        });

        this.addStandardInventorySlots(playerInventory, 7, 120);
    }

    @Override
    public void slotsChanged(Container inv) {
        if (inv != this.inputInventory) return;
        this.selectedIndex = -1;
        this.selectedLevel = 1;
        rebuildEntries();
        updateResult();
    }

    public void rebuildEntries() {
        ItemStack item = this.inputInventory.getItem(0);
        if (item.isEmpty() || LegendaryItems.isLegendary(item)) {
            this.entries = List.of();
            return;
        }

        List<CatalogueEntry> result = new ArrayList<>();
        var registry = registryManager.lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments existing = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (Holder<Enchantment> entry : registry.asHolderIdMap()) {
            Optional<ResourceKey<Enchantment>> keyOpt = entry.unwrapKey();
            if (keyOpt.isEmpty()) continue;

            ResourceKey<Enchantment> key = keyOpt.get();
            if (DisabledEnchantments.isDisabled(entry)) continue;
            if (!entry.value().canEnchant(item)) continue;
            if (!unlockedIds.contains(key.identifier())) continue;

            int maxLevel = entry.value().getMaxLevel();
            int current = existing.getLevel(entry);
            if (current > 0) {
                if (current >= maxLevel) continue;
                if (entry.is(EnchantmentTags.CURSE)) continue;
            } else if (conflictsWithExisting(entry, existing)) {
                continue;
            }
            result.add(new CatalogueEntry(entry, key, maxLevel, current));
        }

        this.entries = result;
    }

    private static boolean conflictsWithExisting(Holder<Enchantment> candidate, ItemEnchantments existing) {
        for (Holder<Enchantment> other : existing.keySet()) {
            if (!Enchantment.areCompatible(candidate, other)) return true;
        }
        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < 1000) {
            int index = id / 10;
            int level = (id % 10) + 1;
            if (index < entries.size()) {
                this.selectedIndex = index;
                CatalogueEntry clicked = entries.get(index);
                this.selectedLevel = Math.max(clicked.currentLevel() + 1, Math.min(level, clicked.maxLevel()));
                updateResult();
                return true;
            }
        }
        return false;
    }

    private void updateResult() {
        if (selectedIndex < 0 || selectedIndex >= entries.size()) {
            outputInventory.setItem(0, ItemStack.EMPTY);
            return;
        }

        ItemStack item = inputInventory.getItem(0);
        if (item.isEmpty() || LegendaryItems.isLegendary(item)) {
            outputInventory.setItem(0, ItemStack.EMPTY);
            return;
        }

        CatalogueEntry entry = entries.get(selectedIndex);
        int level = Math.max(entry.currentLevel() + 1, Math.min(selectedLevel, entry.maxLevel()));

        int slotsNeeded = EnchantmentCosts.slotCost(entry.entry(), level, entry.currentLevel());
        if (SlotSystem.getAvailableSlots(item) < slotsNeeded) {
            outputInventory.setItem(0, ItemStack.EMPTY);
            return;
        }

        if (!canAfford(entry, level)) {
            outputInventory.setItem(0, ItemStack.EMPTY);
            return;
        }

        ItemStack result = item.copy();
        result.enchant(entry.entry(), level);
        outputInventory.setItem(0, result);
    }

    private boolean canAfford(CatalogueEntry entry, int level) {
        if (this.player.isCreative()) return true;

        ItemStack reagent = inputInventory.getItem(1);
        ResourceKey<Enchantment> key = entry.key();
        return reagent.is(EnchantmentCosts.reagent(key))
                && reagent.getCount() >= EnchantmentCosts.reagentCost(level, entry.currentLevel(), normalBookshelves)
                && (!xpCostEnabled || this.player.experienceLevel >= EnchantmentCosts.xpCost(key, level, entry.currentLevel()));
    }

    private boolean canTakeOutput(Player player) {
        if (selectedIndex < 0 || selectedIndex >= entries.size()) return false;
        CatalogueEntry entry = entries.get(selectedIndex);
        int level = Math.max(entry.currentLevel() + 1, Math.min(selectedLevel, entry.maxLevel()));

        ItemStack item = inputInventory.getItem(0);
        if (SlotSystem.getAvailableSlots(item) < EnchantmentCosts.slotCost(entry.entry(), level, entry.currentLevel())) return false;
        return canAfford(entry, level);
    }

    private void onOutputTaken(Player player) {
        if (selectedIndex < 0 || selectedIndex >= entries.size()) return;

        CatalogueEntry entry = entries.get(selectedIndex);
        int level = Math.max(entry.currentLevel() + 1, Math.min(selectedLevel, entry.maxLevel()));
        ResourceKey<Enchantment> key = entry.key();

        if (!player.isCreative()) {
            inputInventory.getItem(1).shrink(EnchantmentCosts.reagentCost(level, entry.currentLevel(), normalBookshelves));
            if (xpCostEnabled) player.giveExperienceLevels(-EnchantmentCosts.xpCost(key, level, entry.currentLevel()));
        }

        inputInventory.setItem(0, ItemStack.EMPTY);

        this.context.execute((world, pos) ->
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.0f));

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, outputInventory.getItem(0), level);
            if (entry.entry().is(EnchantmentTags.CURSE)) {
                ModAdvancements.grantCurseAdvancement(serverPlayer);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex == 2) {
            if (!canTakeOutput(player)) return ItemStack.EMPTY;
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
            onOutputTaken(player);
        } else if (slotIndex < 2) {
            if (!this.moveItemStackTo(stack, 3, 39, true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 1, false))
                if (!this.moveItemStackTo(stack, 1, 2, false))
                    return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        return stack.getCount() == copy.getCount() ? ItemStack.EMPTY : copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.context.execute((world, pos) -> this.clearContainer(player, this.inputInventory));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, Blocks.ENCHANTING_TABLE);
    }

    public List<CatalogueEntry> getEntries() { return entries; }
    public int getSelectedIndex() { return selectedIndex; }
    public int getSelectedLevel() { return selectedLevel; }
    public int getNormalBookshelves() { return normalBookshelves; }

    public boolean isXpCostEnabled() { return xpCostEnabled; }

    public void setSelection(int index, int level) {
        this.selectedIndex = index;
        this.selectedLevel = level;
        updateResult();
    }

    public record CatalogueEntry(Holder<Enchantment> entry, ResourceKey<Enchantment> key, int maxLevel, int currentLevel) {}
}
