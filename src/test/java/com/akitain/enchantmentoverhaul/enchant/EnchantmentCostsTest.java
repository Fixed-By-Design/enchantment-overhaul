package com.akitain.enchantmentoverhaul.enchant;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnchantmentCostsTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Since 26.1, item components bind after the dynamic registry lookup exists.
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
                .forEach(components -> components.apply());
    }

    @Test
    void mendingAndSilkTouchRespectApprovedMaterialsAndPrices() {
        assertSame(Items.GHAST_TEAR, EnchantmentCosts.reagent(Enchantments.MENDING));
        assertEquals(4, EnchantmentCosts.reagentCost(Enchantments.MENDING, 1, 0));
        assertEquals(2, EnchantmentCosts.reagentCost(Enchantments.MENDING, 1, 15));
        assertEquals(8, EnchantmentCosts.xpCost(Enchantments.MENDING, 1));
        assertSame(Items.COBWEB, EnchantmentCosts.reagent(Enchantments.SILK_TOUCH));
        assertEquals(2, EnchantmentCosts.reagentCost(Enchantments.SILK_TOUCH, 1, 0));
        assertEquals(1, EnchantmentCosts.reagentCost(Enchantments.SILK_TOUCH, 1, 15));
    }

    @Test
    void fortuneUpgradesChargeTheDifferenceAfterRounding() {
        assertEquals(18, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 3, 15));
        assertEquals(4, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 1, 0, 15));
        assertEquals(6, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 2, 1, 15));
        assertEquals(8, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 3, 2, 15));
        // At ten shelves, rounded cumulative costs are 6, 14 and 24.
        // Rounding the raw delta would incorrectly charge 11 rather than 10 for II -> III.
        assertEquals(10, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 3, 2, 10));
        assertEquals(0, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 2, 3, 15));
    }

    @Test
    void bookshelfLimitsAndZeroLevelAreWellDefined() {
        assertEquals(8, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 1, -1));
        assertEquals(4, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 1, 100));
        assertEquals(0, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 0, 15));
        assertEquals(0, BookshelfScanner.reagentDiscount(-1));
        assertEquals(0.5, BookshelfScanner.reagentDiscount(100));
        assertEquals(1.0 / 3, BookshelfScanner.reagentDiscount(10), 0.000001);
    }

    @Test
    void reagentSlotAcceptsNewMaterialsAndLapisFallback() {
        for (var item : new net.minecraft.world.item.Item[] {
                Items.GHAST_TEAR, Items.ENDER_PEARL, Items.AMETHYST_SHARD,
                Items.PRISMARINE_CRYSTALS, Items.REDSTONE, Items.IRON_INGOT,
                Items.COBWEB, Items.LAPIS_LAZULI}) {
            assertTrue(EnchantmentCosts.isReagent(item), item.toString());
        }
        assertFalse(EnchantmentCosts.isReagent(Items.HEART_OF_THE_SEA));
        assertFalse(EnchantmentCosts.isReagent(Items.ECHO_SHARD));
        assertFalse(EnchantmentCosts.isReagent(Items.SPECTRAL_ARROW));
        assertFalse(EnchantmentCosts.isReagent(Items.STRING));
    }

    @Test
    void unknownEnchantmentsKeepLapisAndTheOriginalPriceCurve() {
        var key = ResourceKey.create(Registries.ENCHANTMENT,
                Identifier.fromNamespaceAndPath("test", "unknown_enchantment"));
        assertSame(Items.LAPIS_LAZULI, EnchantmentCosts.reagent(key));
        assertEquals(10, EnchantmentCosts.baseReagentCost(key, 5));
        assertEquals(5, EnchantmentCosts.reagentCost(key, 5, 15));
        assertEquals(3, EnchantmentCosts.reagentCost(key, 5, 2, 15));
    }

    @Test
    void datapackRaisedLevelsContinueTheLastIncrement() {
        assertEquals(52, EnchantmentCosts.baseReagentCost(Enchantments.FORTUNE, 4));
        assertEquals(8, EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 4, 3, 15));
    }

    @Test
    void everySupportedLevelFitsOneStackAndEveryUpgradePathHasTheSameTotal() throws Exception {
        for (var owner : new Class<?>[] {Enchantments.class, ModEnchantments.class}) {
            for (var field : owner.getFields()) {
                if (!ResourceKey.class.equals(field.getType())) continue;
                @SuppressWarnings("unchecked")
                var key = (ResourceKey<Enchantment>) field.get(null);
                // Registry data lives in the Minecraft jar or this mod's resources.
                String path = "/data/" + key.identifier().getNamespace() + "/enchantment/"
                        + key.identifier().getPath() + ".json";
                try (var stream = getClass().getResourceAsStream(path)) {
                    assertNotNull(stream, path);
                    var json = com.google.gson.JsonParser.parseReader(
                            new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                    int max = json.getAsJsonObject().get("max_level").getAsInt();
                    int stackLimit = new ItemStack(EnchantmentCosts.reagent(key)).getMaxStackSize();
                    for (int shelves = 0; shelves <= 15; shelves++) {
                        int total = 0;
                        for (int level = 1; level <= max; level++) {
                            int delta = EnchantmentCosts.reagentCost(key, level, level - 1, shelves);
                            assertTrue(delta > 0, key + " must charge for each upgrade");
                            total += delta;
                            assertEquals(EnchantmentCosts.reagentCost(key, level, shelves), total,
                                    key + " staged upgrades must equal direct application");
                            assertTrue(total <= stackLimit, key + " must fit the reagent slot");
                            assertEquals(total - EnchantmentCosts.reagentCost(key, 1, shelves),
                                    EnchantmentCosts.reagentCost(key, level, 1, shelves));
                        }
                    }
                }
            }
        }
    }
}
