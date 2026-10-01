package com.akitain.enchantmentoverhaul.component;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public final class ModComponents {

    // No longer applied or read; kept registered so items saved with it still load.
    public static final DataComponentType<Integer> GRINDSTONE_PENALTY = register("grindstone_penalty");
    public static final DataComponentType<Integer> HONING_LEVEL = register("honing_level");
    public static final DataComponentType<Integer> WARDING_LEVEL = register("warding_level");
    public static final DataComponentType<Integer> TEMPERING_LEVEL = register("tempering_level");
    public static final DataComponentType<Integer> GRINDING_LEVEL = register("grinding_level");

    private ModComponents() {}

    private static DataComponentType<Integer> register(String name) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, name),
                DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    }

    public static void bootstrap() {}
}
