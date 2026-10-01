package com.akitain.enchantmentoverhaul.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Chiseled bookshelves must sync their full contents so the catalogue, particles and hover label work.
// This injects into BlockEntity rather than overriding the methods on ChiseledBookShelfBlockEntity: that
// subclass does not declare them, so an override would be a new method and would clash with any other mod
// adding the same one (Easy Magic does, which crashed the game on startup).
@Mixin(BlockEntity.class)
public class BlockEntitySyncMixin {

    @Inject(method = "getUpdatePacket", at = @At("HEAD"), cancellable = true)
    private void syncBookshelfPacket(CallbackInfoReturnable<Packet<ClientGamePacketListener>> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof ChiseledBookShelfBlockEntity) {
            cir.setReturnValue(ClientboundBlockEntityDataPacket.create(self));
        }
    }

    @Inject(method = "getUpdateTag", at = @At("HEAD"), cancellable = true)
    private void syncBookshelfTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof ChiseledBookShelfBlockEntity) {
            cir.setReturnValue(self.saveCustomOnly(registries));
        }
    }
}
