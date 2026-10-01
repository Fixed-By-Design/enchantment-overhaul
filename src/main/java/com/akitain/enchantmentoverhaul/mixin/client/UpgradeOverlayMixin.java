package com.akitain.enchantmentoverhaul.mixin.client;

import com.akitain.enchantmentoverhaul.enchant.InnateMaterialProperties;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EquipmentLayerRenderer.class)
public class UpgradeOverlayMixin {

    private static final String MOD = "enchantment-overhaul";

    @Unique
    private TextureAtlas upgradeAtlas;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void captureAtlas(EquipmentAssetManager loader, TextureAtlas atlas, CallbackInfo ci) {
        this.upgradeAtlas = atlas;
    }

    @Inject(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V", at = @At("TAIL"))
    private <S> void renderUpgradeOverlays(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> assetKey,
                                            Model<? super S> model, S state, ItemStack stack, PoseStack matrices,
                                            SubmitNodeCollector queue, int light, Identifier textureId,
                                            int outlineColor, int initialOrder, CallbackInfo ci) {
        if (upgradeAtlas == null) return;

        String material = getPaletteName(stack);
        if (material == null) return;

        String dir = layerType == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                ? "trims/entity/humanoid_leggings" : "trims/entity/humanoid";

        int order = initialOrder;

        if (UpgradeType.WARDING.currentLevel(stack) > 0) {
            renderOverlay(dir + "/warding_" + material, model, state, matrices, queue, light, outlineColor, order++);
        }

        if (UpgradeType.TEMPERING.currentLevel(stack) > 0) {
            renderOverlay(dir + "/tempering_" + material, model, state, matrices, queue, light, outlineColor, order);
        }
    }

    @Unique
    private <S> void renderOverlay(String spritePath, Model<? super S> model, S state, PoseStack matrices,
                                    SubmitNodeCollector queue, int light, int outlineColor, int order) {
        TextureAtlasSprite sprite = upgradeAtlas.getSprite(Identifier.fromNamespaceAndPath(MOD, spritePath));
        if (sprite == null) return;

        queue.order(order)
                .submitModel(model, state, matrices,
                        Sheets.armorTrimsSheet(false),
                        light, OverlayTexture.NO_OVERLAY, -1, sprite, outlineColor, null);
    }

    @Unique
    private static String getPaletteName(ItemStack stack) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (id.startsWith("chainmail_")) return "chainmail";

        String mat = InnateMaterialProperties.getMaterial(stack);
        if (mat == null) return null;
        return switch (mat) {
            case "copper" -> "copper";
            case "iron" -> "iron";
            case "gold", "rose_gold" -> "gold";
            case "diamond" -> "diamond";
            case "netherite" -> "netherite";
            case "leather" -> "leather";
            default -> null;
        };
    }
}
