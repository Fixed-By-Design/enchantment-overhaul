package com.akitain.enchantmentoverhaul.mixin.client;

import com.akitain.enchantmentoverhaul.enchant.BookshelfScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableParticleMixin {

    @Inject(method = "animateTick", at = @At("TAIL"))
    private void addChiseledBookshelfParticles(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) != 0) continue;
            if (!holdsBooks(level.getBlockState(pos.offset(offset))) || !BookshelfScanner.isUnobstructed(level, pos, offset)) continue;

            level.addParticle(
                    ParticleTypes.ENCHANT,
                    pos.getX() + 0.5,
                    pos.getY() + 2.0,
                    pos.getZ() + 0.5,
                    offset.getX() + random.nextFloat() - 0.5,
                    offset.getY() - random.nextFloat() - 1.0F,
                    offset.getZ() + random.nextFloat() - 0.5
            );
        }
    }

    @Unique
    private static boolean holdsBooks(BlockState state) {
        return state.is(Blocks.CHISELED_BOOKSHELF) && ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.stream().anyMatch(state::getValue);
    }
}
