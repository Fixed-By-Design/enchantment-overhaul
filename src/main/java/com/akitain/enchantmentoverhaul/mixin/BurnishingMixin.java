package com.akitain.enchantmentoverhaul.mixin;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class BurnishingMixin {

    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true)
    private void stripAllCopperOxidation(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemInHand = context.getItemInHand();
        if (!ModEnchantmentHelper.hasEnchantment(ModEnchantments.BURNISHING, itemInHand)) return;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState unoxidized = level.getBlockState(pos);
        int stages = 0;
        Optional<BlockState> previous;
        while ((previous = WeatheringCopper.getPrevious(unoxidized)).isPresent()) {
            unoxidized = previous.get();
            stages++;
        }
        if (stages == 0) return;

        Player player = context.getPlayer();
        if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemInHand);
        level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, pos, 0);
        level.setBlock(pos, unoxidized, Block.UPDATE_ALL_IMMEDIATE);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, unoxidized));
        if (player != null) itemInHand.hurtAndBreak(stages, player, context.getHand().asEquipmentSlot());
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
