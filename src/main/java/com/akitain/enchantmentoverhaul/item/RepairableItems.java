package com.akitain.enchantmentoverhaul.item;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Repairable;

// Vanilla repairs these by combining two copies, which the anvil no longer allows. Each one gets an
// enchantment-overhaul:repairs_<item> tag holding its repair materials instead.
public final class RepairableItems {

    private static final List<Item> ITEMS = List.of(
            Items.BOW,
            Items.CROSSBOW,
            Items.TRIDENT,
            Items.FISHING_ROD,
            Items.SHEARS,
            Items.FLINT_AND_STEEL,
            Items.CARROT_ON_A_STICK,
            Items.WARPED_FUNGUS_ON_A_STICK,
            Items.BRUSH
    );

    private RepairableItems() {}

    public static TagKey<Item> repairMaterials(Item item) {
        String path = "repairs_" + BuiltInRegistries.ITEM.getKey(item).getPath();
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, path));
    }

    public static void bootstrap() {
        DefaultItemComponentEvents.MODIFY.register(context -> context.modify(ITEMS, (components, registries, item) ->
                components.set(DataComponents.REPAIRABLE, new Repairable(registries.lookupOrThrow(Registries.ITEM).getOrThrow(repairMaterials(item))))));
    }
}
