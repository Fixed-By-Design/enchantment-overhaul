package com.akitain.enchantmentoverhaul.gamerule;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class ModGameRules {

    public static final GameRule<Boolean> MOB_GEAR_ENCHANTMENTS = register("mob_gear_enchantments", GameRuleCategory.MOBS, true);
    public static final GameRule<Boolean> HONING_ON_AXES = register("honing_on_axes", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> ENCHANTING_XP_COST = register("enchanting_xp_cost", GameRuleCategory.PLAYER, true);

    private ModGameRules() {}

    private static GameRule<Boolean> register(String name, GameRuleCategory category, boolean defaultValue) {
        return GameRuleBuilder.forBoolean(defaultValue)
                .category(category)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, name));
    }

    public static void bootstrap() {}
}
