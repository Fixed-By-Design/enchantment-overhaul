package com.akitain.enchantmentoverhaul.enchant;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BookshelfScanner {

    public record ScanResult(Set<ResourceKey<Enchantment>> unlocked, int normalBookshelves) {}

    public static ScanResult scan(Level level, BlockPos tablePos) {
        Set<ResourceKey<Enchantment>> unlocked = new HashSet<>();
        int normalCount = 0;

        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (!isUnobstructed(level, tablePos, offset)) continue;

            BlockPos shelfPos = tablePos.offset(offset);
            BlockState state = level.getBlockState(shelfPos);
            if (state.is(Blocks.CHISELED_BOOKSHELF)) {
                collectEnchantments(level, shelfPos, unlocked);
            } else if (state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER)) {
                normalCount++;
            }
        }

        return new ScanResult(unlocked, normalCount);
    }

    public static boolean isUnobstructed(Level level, BlockPos tablePos, BlockPos offset) {
        return level.getBlockState(tablePos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
    }

    private static void collectEnchantments(Level level, BlockPos pos, Set<ResourceKey<Enchantment>> unlocked) {
        if (!(level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity shelf)) return;

        for (int i = 0; i < ChiseledBookShelfBlockEntity.MAX_BOOKS_IN_STORAGE; i++) {
            ItemStack book = shelf.getItem(i);
            if (!book.is(Items.ENCHANTED_BOOK)) continue;

            ItemEnchantments stored = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : stored.entrySet()) {
                entry.getKey().unwrapKey().ifPresent(unlocked::add);
            }
        }
    }

    public static double reagentDiscount(int normalBookshelves) {
        int capped = Math.min(normalBookshelves, 15);
        return capped * (0.5 / 15.0);
    }
}
