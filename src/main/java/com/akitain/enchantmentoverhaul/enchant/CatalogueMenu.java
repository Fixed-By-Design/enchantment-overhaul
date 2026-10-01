package com.akitain.enchantmentoverhaul.enchant;

import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class CatalogueMenu extends AbstractContainerMenu {

    public static final int ITEM_SLOT = 0;
    public static final int REAGENT_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int USE_ROW_SLOT_END = 39;

    private final Container enchantSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            CatalogueMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;
    private final Set<ResourceKey<Enchantment>> unlocked;
    private final int bookshelves;
    private final boolean xpCostEnabled;
    private ItemStack lastItem = ItemStack.EMPTY;
    private List<CatalogueEntry> entries = List.of();
    private int selectedIndex = -1;
    private int selectedLevel;
    private Runnable slotUpdateListener = () -> {};

    public CatalogueMenu(int containerId, Inventory inventory, CatalogueData data) {
        this(containerId, inventory, ContainerLevelAccess.NULL, data);
    }

    public CatalogueMenu(int containerId, Inventory inventory, ContainerLevelAccess access, CatalogueData data) {
        super(ModMenus.CATALOGUE, containerId);
        this.access = access;
        this.player = inventory.player;
        this.unlocked = data.unlocked();
        this.bookshelves = data.bookshelves();
        this.xpCostEnabled = data.xpCostEnabled();

        this.addSlot(new Slot(this.enchantSlots, ITEM_SLOT, 9, 60) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return SlotSystem.getBaseMaxSlots(stack) > 0;
            }
        });
        this.addSlot(new Slot(this.enchantSlots, REAGENT_SLOT, 31, 60) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return EnchantmentCosts.isReagent(stack.getItem());
            }
        });
        this.addSlot(new Slot(this.resultSlots, 0, 20, 86) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return CatalogueMenu.this.canTakeResult();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                CatalogueMenu.this.onTakeResult(player, stack);
                super.onTake(player, stack);
            }
        });
        this.addStandardInventorySlots(inventory, 7, 120);
    }

    public static ExtendedMenuProvider<CatalogueData> provider(ServerLevel level, BlockPos pos, Component title) {
        BookshelfScanner.ScanResult scan = BookshelfScanner.scan(level, pos);
        CatalogueData data = new CatalogueData(scan.unlocked(), scan.normalBookshelves(), level.getGameRules().get(ModGameRules.ENCHANTING_XP_COST));
        return new ExtendedMenuProvider<>() {
            @Override
            public CatalogueData getScreenOpeningData(ServerPlayer player) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                if (player instanceof ServerPlayer serverPlayer) ModAdvancements.checkEndgameBooks(serverPlayer, data.unlocked());
                return new CatalogueMenu(containerId, inventory, ContainerLevelAccess.create(level, pos), data);
            }
        };
    }

    public static int buttonId(int index, int level) {
        return index * Enchantment.MAX_LEVEL + level - 1;
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack item = this.enchantSlots.getItem(ITEM_SLOT);
        if (!ItemStack.matches(item, this.lastItem)) {
            this.lastItem = item.copy();
            this.entries = this.createEntries(item);
            this.selectedIndex = -1;
            this.slotUpdateListener.run();
        }
        this.updateResult();
    }

    private List<CatalogueEntry> createEntries(ItemStack item) {
        if (item.isEmpty() || LegendaryItems.isLegendary(item)) return List.of();

        ItemEnchantments existing = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        List<CatalogueEntry> entries = new ArrayList<>();
        for (Holder<Enchantment> enchantment : this.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap()) {
            if (this.isOffered(enchantment, item, existing)) entries.add(new CatalogueEntry(enchantment, existing.getLevel(enchantment)));
        }
        return List.copyOf(entries);
    }

    private boolean isOffered(Holder<Enchantment> enchantment, ItemStack item, ItemEnchantments existing) {
        if (!enchantment.unwrapKey().map(this.unlocked::contains).orElse(false)) return false;
        if (DisabledEnchantments.isDisabled(enchantment) || !enchantment.value().canEnchant(item)) return false;

        int currentLevel = existing.getLevel(enchantment);
        if (currentLevel == 0) return existing.keySet().stream().allMatch(other -> Enchantment.areCompatible(enchantment, other));
        return currentLevel < enchantment.value().getMaxLevel() && !enchantment.is(EnchantmentTags.CURSE);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0) return false;

        int index = id / Enchantment.MAX_LEVEL;
        int level = id % Enchantment.MAX_LEVEL + 1;
        if (index >= this.entries.size() || !this.canEnchant(this.entries.get(index), level)) return false;

        this.selectedIndex = index;
        this.selectedLevel = level;
        this.updateResult();
        return true;
    }

    public boolean canEnchant(CatalogueEntry entry, int level) {
        if (level <= entry.currentLevel() || level > entry.maxLevel()) return false;
        if (SlotSystem.getAvailableSlots(this.enchantSlots.getItem(ITEM_SLOT)) < this.slotCost(entry, level)) return false;
        return this.player.hasInfiniteMaterials() || this.canAfford(entry, level);
    }

    private boolean canAfford(CatalogueEntry entry, int level) {
        ItemStack reagent = this.enchantSlots.getItem(REAGENT_SLOT);
        return reagent.is(EnchantmentCosts.reagent(entry.key()))
                && reagent.getCount() >= this.reagentCost(entry, level)
                && this.player.experienceLevel >= this.xpCost(entry, level);
    }

    public int slotCost(CatalogueEntry entry, int level) {
        return EnchantmentCosts.slotCost(entry.enchantment(), level, entry.currentLevel());
    }

    public int reagentCost(CatalogueEntry entry, int level) {
        return EnchantmentCosts.reagentCost(level, entry.currentLevel(), this.bookshelves);
    }

    public int xpCost(CatalogueEntry entry, int level) {
        return this.xpCostEnabled ? EnchantmentCosts.xpCost(entry.key(), level, entry.currentLevel()) : 0;
    }

    private void updateResult() {
        CatalogueEntry entry = this.getSelectedEntry();
        this.resultSlots.setItem(0, entry != null && this.canEnchant(entry, this.selectedLevel) ? this.createResult(entry) : ItemStack.EMPTY);
    }

    private ItemStack createResult(CatalogueEntry entry) {
        ItemStack result = this.enchantSlots.getItem(ITEM_SLOT).copy();
        result.enchant(entry.enchantment(), this.selectedLevel);
        return result;
    }

    private boolean canTakeResult() {
        CatalogueEntry entry = this.getSelectedEntry();
        return entry != null && this.canEnchant(entry, this.selectedLevel);
    }

    private void onTakeResult(Player player, ItemStack result) {
        CatalogueEntry entry = this.getSelectedEntry();
        if (entry == null) return;

        int enchantmentLevel = this.selectedLevel;
        if (!player.hasInfiniteMaterials()) {
            this.enchantSlots.getItem(REAGENT_SLOT).shrink(this.reagentCost(entry, enchantmentLevel));
            player.giveExperienceLevels(-this.xpCost(entry, enchantmentLevel));
        }
        this.enchantSlots.setItem(ITEM_SLOT, ItemStack.EMPTY);
        this.access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F));

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, result, enchantmentLevel);
            if (entry.enchantment().is(EnchantmentTags.CURSE)) ModAdvancements.grantCurseAdvancement(serverPlayer);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex <= RESULT_SLOT) {
            if (!this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, true)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, ITEM_SLOT, RESULT_SLOT, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, original);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.enchantSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    public void registerUpdateListener(Runnable slotUpdateListener) {
        this.slotUpdateListener = slotUpdateListener;
    }

    public List<CatalogueEntry> getEntries() {
        return this.entries;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public int getSelectedLevel() {
        return this.selectedLevel;
    }

    public @Nullable CatalogueEntry getSelectedEntry() {
        return this.selectedIndex >= 0 && this.selectedIndex < this.entries.size() ? this.entries.get(this.selectedIndex) : null;
    }

    public int getBookshelves() {
        return this.bookshelves;
    }

    public boolean isXpCostEnabled() {
        return this.xpCostEnabled;
    }

    public record CatalogueEntry(Holder<Enchantment> enchantment, int currentLevel) {

        public ResourceKey<Enchantment> key() {
            return this.enchantment.unwrapKey().orElseThrow();
        }

        public int maxLevel() {
            return this.enchantment.value().getMaxLevel();
        }
    }
}
