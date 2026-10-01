package com.akitain.enchantmentoverhaul.enchant;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public record CatalogueData(Set<ResourceKey<Enchantment>> unlocked, int bookshelves, boolean xpCostEnabled) {

    public static final StreamCodec<ByteBuf, CatalogueData> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.ENCHANTMENT).apply(ByteBufCodecs.collection(HashSet::new)),
            CatalogueData::unlocked,
            ByteBufCodecs.VAR_INT,
            CatalogueData::bookshelves,
            ByteBufCodecs.BOOL,
            CatalogueData::xpCostEnabled,
            CatalogueData::new
    );
}
