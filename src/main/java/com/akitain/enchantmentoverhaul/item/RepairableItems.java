package com.akitain.enchantmentoverhaul.item;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Repairable;

public final class RepairableItems {

    public static final TagKey<Item> REPAIRS_BOW = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "repairs_bow"));

    private RepairableItems() {}

    public static void bootstrap() {
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.BOW, (components, registries, item) ->
                components.set(DataComponents.REPAIRABLE, new Repairable(registries.lookupOrThrow(Registries.ITEM).getOrThrow(REPAIRS_BOW)))));
    }
}
