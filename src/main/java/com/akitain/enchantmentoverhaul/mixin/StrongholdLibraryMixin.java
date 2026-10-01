package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(StrongholdPieces.Library.class)
public abstract class StrongholdLibraryMixin extends StructurePiece {

    private StrongholdLibraryMixin() { super(null, 0, null); }

    @Unique
    private static final List<ResourceKey<Enchantment>> BOOK_POOL = List.of(
            Enchantments.FEATHER_FALLING, ModEnchantments.STEP_UP, Enchantments.KNOCKBACK,
            Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE, Enchantments.FROST_WALKER,
            Enchantments.INFINITY, Enchantments.FORTUNE
    );

    @Inject(method = "postProcess", at = @At("TAIL"))
    private void modifyLibraryGround(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot, CallbackInfo ci) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bookshelf = Blocks.BOOKSHELF.defaultBlockState();

        this.placeBlock(level, Blocks.OBSIDIAN.defaultBlockState(), 10, 0, 7, chunkBox);

        // Remove bookshelves around Z=9
        this.placeBlock(level, air, 9, 2, 9, chunkBox);
        this.placeBlock(level, air, 9, 3, 9, chunkBox);
        this.placeBlock(level, air, 10, 3, 9, chunkBox);

        // Remove bookshelves around Z=7
        this.placeBlock(level, air, 9, 1, 7, chunkBox);
        this.placeBlock(level, air, 10, 1, 7, chunkBox);
        this.placeBlock(level, air, 9, 2, 7, chunkBox);
        this.placeBlock(level, air, 10, 2, 7, chunkBox);
        this.placeBlock(level, air, 9, 3, 7, chunkBox);
        this.placeBlock(level, air, 10, 3, 7, chunkBox);

        // Remove bookshelves around Z=5
        this.placeBlock(level, air, 9, 2, 5, chunkBox);
        this.placeBlock(level, air, 9, 3, 5, chunkBox);
        this.placeBlock(level, air, 10, 3, 5, chunkBox);

        // Add bookshelves where vanilla had air
        this.placeBlock(level, bookshelf, 11, 1, 9, chunkBox);
        this.placeBlock(level, bookshelf, 11, 2, 9, chunkBox);
        this.placeBlock(level, bookshelf, 11, 2, 5, chunkBox);

        // Chiseled bookshelves with enchanted books
        placeChiseledBookshelf(level, random, 10, 1, 9, Direction.SOUTH, chunkBox);
        placeChiseledBookshelf(level, random, 12, 1, 7, Direction.WEST, chunkBox);
        placeChiseledBookshelf(level, random, 12, 1, 6, Direction.WEST, chunkBox);
        placeChiseledBookshelf(level, random, 11, 1, 5, Direction.NORTH, chunkBox);
    }

    @Unique
    private void placeChiseledBookshelf(WorldGenLevel level, RandomSource random, int x, int y, int z, Direction facing, BoundingBox chunkBox) {
        int slot = random.nextInt(ChiseledBookShelfBlockEntity.MAX_BOOKS_IN_STORAGE);
        BlockState state = Blocks.CHISELED_BOOKSHELF.defaultBlockState()
                .setValue(ChiseledBookShelfBlock.FACING, facing)
                .setValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot), true);
        this.placeBlock(level, state, x, y, z, chunkBox);

        BlockPos pos = this.getWorldPos(x, y, z);
        if (chunkBox.isInside(pos) && level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity shelf) {
            Holder<Enchantment> enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Util.getRandom(BOOK_POOL, random));
            shelf.setItemNoUpdate(slot, EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, 1)));
        }
    }
}
