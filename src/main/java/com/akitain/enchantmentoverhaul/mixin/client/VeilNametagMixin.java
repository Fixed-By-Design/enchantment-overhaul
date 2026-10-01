package com.akitain.enchantmentoverhaul.mixin.client;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class VeilNametagMixin {

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at = @At("RETURN"), cancellable = true)
    private void hideVeilNametag(Avatar entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        if (!ModEnchantmentHelper.hasEnchantment(ModEnchantments.VEIL, entity.getItemBySlot(EquipmentSlot.HEAD))) return;

        Minecraft client = Minecraft.getInstance();
        Entity camera = client.getCameraEntity();
        if (camera == null) return;

        Vec3 start = camera.getEyePosition();
        Vec3 end = entity.getEyePosition();
        BlockHitResult hit = entity.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, camera));
        if (hit.getType() == HitResult.Type.BLOCK) {
            cir.setReturnValue(false);
        }
    }
}
