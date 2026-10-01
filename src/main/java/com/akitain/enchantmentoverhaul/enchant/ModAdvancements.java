package com.akitain.enchantmentoverhaul.enchant;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModAdvancements {

    private static final Set<ResourceKey<Enchantment>> ENDGAME_ENCHANTMENTS = Set.of(
            ModEnchantments.VEIL, Enchantments.SWIFT_SNEAK, Enchantments.BINDING_CURSE,
            Enchantments.WIND_BURST, Enchantments.BREACH, Enchantments.LUNGE, ModEnchantments.LAST_STAND,
            Enchantments.MENDING
    );

    public static void checkEndgameBooks(ServerPlayer player, Set<ResourceKey<Enchantment>> unlocked) {
        if (unlocked.stream().anyMatch(ENDGAME_ENCHANTMENTS::contains)) grant(player, "forbidden_knowledge", "requirement");
    }

    public static void grantSmithingAdvancement(ServerPlayer player, UpgradeType type) {
        grant(player, "hammer_time", "requirement");

        String criterion = switch (type) {
            case HONING -> "honing";
            case WARDING -> "warding";
            case TEMPERING -> "tempering";
            case GRINDING -> "grinding";
        };
        grant(player, "jack_of_all_trades", criterion);

        checkWalkingFortress(player);
    }

    public static void grantCurseAdvancement(ServerPlayer player) {
        grant(player, "deal_with_the_devil", "requirement");
    }

    public static void checkWalkingFortress(ServerPlayer player) {
        int maxedPieces = HumanoidArmor.count(player, stack -> UpgradeType.WARDING.currentLevel(stack) >= UpgradeType.MAX_LEVEL);
        if (maxedPieces == HumanoidArmor.SLOTS.size()) grant(player, "walking_fortress", "requirement");
    }

    private static void grant(ServerPlayer player, String advancement, String criterion) {
        Identifier id = Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, advancement);
        AdvancementHolder entry = player.level().getServer().getAdvancements().get(id);
        if (entry != null) {
            player.getAdvancements().award(entry, criterion);
        }
    }
}
