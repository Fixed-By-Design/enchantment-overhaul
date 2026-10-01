package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.BookshelfScanner;
import com.akitain.enchantmentoverhaul.gamerule.ModGameRules;
import com.akitain.enchantmentoverhaul.enchant.CatalogueData;
import com.akitain.enchantmentoverhaul.enchant.CatalogueMenu;
import com.akitain.enchantmentoverhaul.enchant.ModAdvancements;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void openCatalogue(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        BookshelfScanner.ScanResult scan = BookshelfScanner.scan(world, pos);
        List<Identifier> unlocked = scan.unlocked().stream()
                .map(ResourceKey::identifier)
                .toList();
        int bookshelves = scan.normalBookshelves();
        boolean xpCostEnabled = serverPlayer.level().getGameRules().get(ModGameRules.ENCHANTING_XP_COST);
        ModAdvancements.checkEndgameBooks(serverPlayer, scan.unlocked());
        Component title = world.getBlockEntity(pos) instanceof EnchantingTableBlockEntity entity
                ? entity.getDisplayName()
                : Component.translatable("container.enchant");

        serverPlayer.openMenu(new ExtendedMenuProvider<CatalogueData>() {
            @Override
            public CatalogueData getScreenOpeningData(ServerPlayer p) {
                return new CatalogueData(unlocked, bookshelves, xpCostEnabled);
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return new CatalogueMenu(syncId, inv, ContainerLevelAccess.create(world, pos), unlocked, bookshelves, xpCostEnabled);
            }
        });

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
