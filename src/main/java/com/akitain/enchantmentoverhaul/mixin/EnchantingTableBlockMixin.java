package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.CatalogueMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {

    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    private void openCatalogue(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir) {
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table) {
            cir.setReturnValue(CatalogueMenu.provider(serverLevel, pos, table.getDisplayName()));
        }
    }
}
