package com.akitain.enchantmentoverhaul.mixin.client;

import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarRenderer.class)
public class VeilNametagMixin {

    @ModifyReturnValue(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at = @At("RETURN"))
    private boolean hideVeilNametag(boolean shown, @Local(argsOnly = true) Avatar entity) {
        return shown && !(ModEnchantmentHelper.hasEnchantment(ModEnchantments.VEIL, entity.getItemBySlot(EquipmentSlot.HEAD)) && isOccluded(entity));
    }

    @Unique
    private static boolean isOccluded(Avatar entity) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        if (camera == null) return false;
        ClipContext clip = new ClipContext(camera.getEyePosition(), entity.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, camera);
        return entity.level().clip(clip).getType() == HitResult.Type.BLOCK;
    }
}
