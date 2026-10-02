package com.akitain.enchantmentoverhaul.gametest;

import static com.akitain.enchantmentoverhaul.gametest.Scene.check;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.client.CatalogueScreen;
import com.akitain.enchantmentoverhaul.component.ModComponents;
import com.akitain.enchantmentoverhaul.enchant.CatalogueMenu;
import com.akitain.enchantmentoverhaul.enchant.CatalogueMenu.CatalogueEntry;
import com.akitain.enchantmentoverhaul.enchant.EnchantmentCosts;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantmentHelper;
import com.akitain.enchantmentoverhaul.enchant.ModEnchantments;
import com.akitain.enchantmentoverhaul.enchant.SlotSystem;
import com.akitain.enchantmentoverhaul.smithing.SmithingTemplates;
import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EnchantmentOverhaulClientGameTest implements FabricClientGameTest {

    private static final int CATALOGUE_WIDTH = 176;
    private static final int CATALOGUE_HEIGHT = 200;
    private static final int CONTAINER_INVENTORY_START = 3;
    private static final int SMITHING_INVENTORY_START = 4;
    private static final int LOOT_ROLLS = 2000;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().adjustSettings(settings -> settings.setAllowCommands(true)).create()) {
            singleplayer.getClientLevel().waitForChunksRender();
            Scene scene = new Scene(context, singleplayer.getServer());
            scene.build();

            catalogueEnchantsAndCharges(scene);
            catalogueDocumentationScreenshot(scene);
            tooltipsAreTranslated(scene);
            upgradeCommandAppliesUpgrades(scene);
            structureLootRollsEveryPool(scene);
            veilOnlyShortensRangedDetection(scene);
            equipmentEffectsApply(scene);
            anvilRepairsWithMaterials(scene);
            grindstoneKeepsSlots(scene);
            roseGoldGetsDiamondSlots(scene);
            smithingTableUpgrades(scene);
        }
    }

    private static void catalogueEnchantsAndCharges(Scene scene) {
        scene.resetInventory("diamond_pickaxe", "emerald 64");
        scene.command("experience set " + scene.player + " 30 levels");
        scene.open(scene.enchantingTable, EnchantmentOverhaulClientGameTest::isCatalogue);
        scene.screenshot("catalogue_empty");
        scene.quickMove(scene.hotbarSlot(CONTAINER_INVENTORY_START, 0));
        scene.quickMove(scene.hotbarSlot(CONTAINER_INVENTORY_START, 1));

        List<CatalogueEntry> entries = scene.context.computeOnClient(minecraft -> catalogue(minecraft).getEntries());
        int bookshelves = scene.context.computeOnClient(minecraft -> catalogue(minecraft).getBookshelves());
        check(bookshelves == 15, "expected 15 bookshelves around the table, got " + bookshelves);

        int fortune = indexOf(entries, Enchantments.FORTUNE);
        scene.click(CATALOGUE_WIDTH, CATALOGUE_HEIGHT, levelButtonX(entries, fortune, 3), rowCenterY(fortune));
        scene.screenshot("catalogue_fortune_selected");
        scene.quickMove(CatalogueMenu.RESULT_SLOT);
        scene.close();

        int emeralds = 64 - EnchantmentCosts.reagentCost(Enchantments.FORTUNE, 3, 0, bookshelves);
        int levels = 30 - EnchantmentCosts.xpCost(Enchantments.FORTUNE, 3, 0);
        scene.onServer(server -> {
            ServerPlayer player = Scene.player(server);
            ItemStack pickaxe = find(player, Items.DIAMOND_PICKAXE);
            check(ModEnchantmentHelper.getItemEnchantmentLevel(Enchantments.FORTUNE, pickaxe) == 3, "pickaxe should carry Fortune III: " + pickaxe);
            check(count(player, Items.EMERALD) == emeralds, "expected " + emeralds + " emeralds left, got " + count(player, Items.EMERALD));
            check(player.experienceLevel == levels, "expected " + levels + " levels left, got " + player.experienceLevel);
            return null;
        });
    }

    private static void catalogueDocumentationScreenshot(Scene scene) {
        scene.resetInventory("diamond_pickaxe", "ghast_tear 16");
        scene.open(scene.enchantingTable, EnchantmentOverhaulClientGameTest::isCatalogue);
        scene.quickMove(scene.hotbarSlot(CONTAINER_INVENTORY_START, 0));
        scene.quickMove(scene.hotbarSlot(CONTAINER_INVENTORY_START, 1));
        List<CatalogueEntry> entries = scene.context.computeOnClient(minecraft -> catalogue(minecraft).getEntries());
        int mending = indexOf(entries, Enchantments.MENDING);
        scene.hover(CATALOGUE_WIDTH, CATALOGUE_HEIGHT, levelButtonX(entries, mending, 1), rowCenterY(mending));
        scene.screenshot("docs_catalogue_ui");
        scene.close();
    }

    private static void tooltipsAreTranslated(Scene scene) {
        ItemStack honedSword = new ItemStack(Items.DIAMOND_SWORD);
        UpgradeType.HONING.applyTo(honedSword, 3);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack boots = new ItemStack(Items.COPPER_BOOTS);
        ItemStack fortuneBook = scene.context.computeOnClient(minecraft -> book(minecraft, Map.of(Enchantments.FORTUNE, 2)));
        ItemStack doubleBook = scene.context.computeOnClient(minecraft -> book(minecraft, Map.of(Enchantments.FORTUNE, 2, Enchantments.MENDING, 1)));

        expectLine(scene, honedSword, "Honing III");
        expectLine(scene, chestplate, "Fire Resistance (5% per piece)");
        expectLine(scene, boots, "Poison Resistance (5% per piece)");
        List<String> book = tooltip(scene, fortuneBook);
        check(book.get(0).equals("Fortune II Book") && book.get(1).equals("Enchanted Book") && !book.contains("Fortune II"),
                "a book shows its enchantment as its name and a subtitle: " + book);
        String doubleBookText = String.join("\n", tooltip(scene, doubleBook));
        check(doubleBookText.contains("Fortune II") && doubleBookText.contains("Mending"), "a book should show every stored enchantment: " + doubleBookText);

        switchLanguage(scene, "fr_fr");
        expectLine(scene, honedSword, "Affûtage III");
        expectLine(scene, chestplate, "Résistance au feu (5 % par pièce)");
        switchLanguage(scene, "en_us");
    }

    private static void upgradeCommandAppliesUpgrades(Scene scene) {
        scene.resetInventory("diamond_sword", "book");
        runCommandInSlot(scene, 0, "upgrade honing 3");
        runCommandInSlot(scene, 1, "upgrade honing 3");
        scene.onServer(server -> {
            ServerPlayer player = Scene.player(server);
            check(UpgradeType.HONING.currentLevel(find(player, Items.DIAMOND_SWORD)) == 3, "/upgrade should hone the held sword");
            check(UpgradeType.HONING.currentLevel(find(player, Items.BOOK)) == 0, "/upgrade should refuse items outside the honable tag");
            return null;
        });
    }

    private static void structureLootRollsEveryPool(Scene scene) {
        check(rollCount(scene, BuiltInLootTables.SIMPLE_DUNGEON, stack -> stack.is(SmithingTemplates.GRINDING_TEMPLATE)) > 0,
                "dungeons should drop grinding templates");
        check(rollCount(scene, BuiltInLootTables.DESERT_PYRAMID, stack -> stack.is(SmithingTemplates.GRINDING_TEMPLATE)) > 0,
                "desert pyramids should drop grinding templates");
        check(rollCount(scene, BuiltInLootTables.VILLAGE_WEAPONSMITH, stack -> stack.isDamageableItem() && stack.isEnchanted()) > 0,
                "chest equipment should sometimes come enchanted");
    }

    private static void veilOnlyShortensRangedDetection(Scene scene) {
        scene.command("difficulty normal");
        scene.command("item replace entity " + scene.player + " armor.head with diamond_helmet[enchantments={\"enchantment-overhaul:veil\":1}]");
        scene.onServer(server -> {
            ServerPlayer player = Scene.player(server);
            Zombie zombie = new Zombie(player.level());
            TargetingConditions unranged = TargetingConditions.forCombat().ignoreLineOfSight();
            TargetingConditions ranged = TargetingConditions.forCombat().ignoreLineOfSight().range(35);

            zombie.setPos(player.getX() + 20, player.getY(), player.getZ());
            check(unranged.test(player.level(), zombie, player), "Veil must not hide its wearer from unranged checks such as retaliation");
            check(!ranged.test(player.level(), zombie, player), "Veil should hide its wearer beyond a quarter of the detection range");
            zombie.setPos(player.getX() + 5, player.getY(), player.getZ());
            check(ranged.test(player.level(), zombie, player), "Veil should not hide its wearer up close");
            return null;
        });
        scene.command("item replace entity " + scene.player + " armor.head with air");
        scene.command("difficulty peaceful");
    }

    private static void equipmentEffectsApply(Scene scene) {
        scene.onServer(server -> {
            ServerPlayer player = Scene.player(server);
            DamageSource generic = player.damageSources().generic();
            DamageSource explosion = player.damageSources().explosion(null, null);

            equipArmor(player, slot -> withComponent(Items.STICK, ModComponents.WARDING_LEVEL, 5));
            expectDamage(player, generic, 3.6F, "four Warding V pieces");
            equipArmor(player, slot -> new ItemStack(Items.DIAMOND_HORSE_ARMOR));
            expectDamage(player, explosion, 8.0F, "four diamond pieces against an explosion");
            expectDamage(player, generic, 10.0F, "four diamond pieces against generic damage");
            equipArmor(player, slot -> ItemStack.EMPTY);

            player.setItemSlot(EquipmentSlot.CHEST, enchanted(server, Items.STICK, ModEnchantments.LAST_STAND, 3));
            expectDamage(player, generic, 10.0F, "Last Stand at full health");
            player.setHealth(3.0F);
            expectDamage(player, generic, 7.0F, "Last Stand III at low health");
            player.setHealth(player.getMaxHealth());

            player.setItemSlot(EquipmentSlot.HEAD, enchanted(server, Items.LEATHER_HELMET, ModEnchantments.CURSE_OF_HUNGER, 1));
            player.setItemSlot(EquipmentSlot.CHEST, enchanted(server, Items.LEATHER_CHESTPLATE, ModEnchantments.CURSE_OF_HUNGER, 1));
            check(Math.abs(exhaustionFrom(player, 1.0F) - 1.6F) < 1.0E-4F, "two Curse of Hunger pieces should add 60% exhaustion");
            equipArmor(player, slot -> ItemStack.EMPTY);

            ItemStack fragile = enchanted(server, Items.IRON_SWORD, ModEnchantments.CURSE_OF_FRAGILITY, 1);
            fragile.hurtAndBreak(1, player.level(), player, item -> {});
            check(fragile.getDamageValue() == 2, "Curse of Fragility should double durability loss");

            checkBurnishing(server, player);
            checkGrindingModifierMigration(server, player);
            player.setItemSlot(EquipmentSlot.FEET, enchanted(server, Items.LEATHER_BOOTS, ModEnchantments.STEP_UP, 1));
            return null;
        });
        scene.context.waitTicks(5);
        scene.onServer(server -> {
            ServerPlayer player = Scene.player(server);
            check(player.getAttributeValue(Attributes.STEP_HEIGHT) == 1.0, "Step-Up should raise step height to a full block");
            player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            return null;
        });
        scene.context.waitTicks(5);
        scene.onServer(server -> {
            check(Scene.player(server).getAttributeValue(Attributes.STEP_HEIGHT) == 0.6, "Step-Up should wear off with the boots");
            return null;
        });
    }

    private static void checkBurnishing(MinecraftServer server, ServerPlayer player) {
        BlockPos copper = player.blockPosition().offset(2, 0, -2);
        player.level().setBlockAndUpdate(copper, Blocks.OXIDIZED_COPPER.defaultBlockState());
        ItemStack axe = enchanted(server, Items.IRON_AXE, ModEnchantments.BURNISHING, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, axe);
        axe.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(copper), Direction.UP, copper, false)));
        check(player.level().getBlockState(copper).is(Blocks.COPPER_BLOCK), "Burnishing should strip every oxidation stage at once");
        check(axe.getDamageValue() == 3, "Burnishing should cost one durability per stage");
        player.level().setBlockAndUpdate(copper, Blocks.AIR.defaultBlockState());
    }

    private static void checkGrindingModifierMigration(MinecraftServer server, ServerPlayer player) {
        ItemStack pickaxe = withComponent(Items.IRON_PICKAXE, ModComponents.GRINDING_LEVEL, 1);
        AttributeModifier legacy = new AttributeModifier(Identifier.withDefaultNamespace("grinding"), 2, AttributeModifier.Operation.ADD_VALUE);
        pickaxe.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder().add(Attributes.MINING_EFFICIENCY, legacy, EquipmentSlotGroup.MAINHAND).build());
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "upgrade grinding 3");

        List<ItemAttributeModifiers.Entry> modifiers = pickaxe.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers();
        check(modifiers.size() == 1, "re-grinding should replace the legacy modifier: " + modifiers);
        AttributeModifier modifier = modifiers.getFirst().modifier();
        check(modifier.is(Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "grinding")) && modifier.amount() == 10.0,
                "Grinding III should add 10 mining efficiency under the mod namespace: " + modifier);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    private static void anvilRepairsWithMaterials(Scene scene) {
        Map<Item, Item> materials = Map.of(
                Items.BOW, Items.STRING,
                Items.CROSSBOW, Items.STRING,
                Items.TRIDENT, Items.PRISMARINE_SHARD,
                Items.FISHING_ROD, Items.STRING,
                Items.SHEARS, Items.IRON_INGOT,
                Items.FLINT_AND_STEEL, Items.FLINT,
                Items.CARROT_ON_A_STICK, Items.CARROT,
                Items.WARPED_FUNGUS_ON_A_STICK, Items.WARPED_FUNGUS,
                Items.BRUSH, Items.FEATHER);
        scene.onServer(server -> {
            materials.forEach((tool, material) -> check(new ItemStack(tool).isValidRepairItem(new ItemStack(material)), tool + " should repair with " + material));
            return null;
        });

        scene.resetInventory("bow[damage=300]", "string 4");
        scene.command("experience set " + scene.player + " 5 levels");
        scene.open(scene.anvil);
        scene.quickMove(scene.hotbarSlot(CONTAINER_INVENTORY_START, 0));
        scene.quickMove(scene.hotbarSlot(CONTAINER_INVENTORY_START, 1));
        scene.context.getInput().setCursorPos(0, 0);
        scene.screenshot("docs_anvil_repair");
        scene.quickMove(2);
        scene.close();
        scene.onServer(server -> {
            ServerPlayer player = Scene.player(server);
            check(find(player, Items.BOW).getDamageValue() == 0, "four strings should fully repair a bow");
            check(count(player, Items.STRING) == 0, "the repair should consume one string per quarter of durability");
            check(player.experienceLevel == 4, "a repair should cost one level");
            return null;
        });
    }

    private static void grindstoneKeepsSlots(Scene scene) {
        scene.resetInventory("iron_sword[enchantments={\"minecraft:knockback\":1,\"minecraft:vanishing_curse\":1}]");
        scene.open(scene.grindstone);
        scene.quickMove(scene.hotbarSlot(CONTAINER_INVENTORY_START, 0));
        scene.quickMove(2);
        scene.close();
        scene.onServer(server -> {
            ItemStack sword = find(Scene.player(server), Items.IRON_SWORD);
            check(sword.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty(), "the grindstone should remove curses too: " + sword);
            check(!sword.has(ModComponents.GRINDSTONE_PENALTY), "the grindstone should not cost a slot");
            check(SlotSystem.getMaxSlots(withComponent(Items.DIAMOND_PICKAXE, ModComponents.GRINDSTONE_PENALTY, 2)) == 5,
                    "items penalized by older versions should get their slots back");
            return null;
        });
    }

    private static void roseGoldGetsDiamondSlots(Scene scene) {
        if (!FabricLoader.getInstance().isModLoaded("additionaladditions")) return;
        scene.onServer(server -> {
            for (String piece : List.of("sword", "pickaxe", "spear", "helmet", "boots")) {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("additionaladditions", "rose_gold_" + piece));
                check(SlotSystem.getBaseMaxSlots(new ItemStack(item)) == 5, "rose gold " + piece + " should get diamond's five slots");
            }
            return null;
        });
    }

    private static void smithingTableUpgrades(Scene scene) {
        scene.resetInventory("enchantment-overhaul:grinding_template", "iron_pickaxe", "diamond");
        scene.open(scene.smithingTable);
        for (int i = 0; i < 3; i++) scene.quickMove(scene.hotbarSlot(SMITHING_INVENTORY_START, i));
        scene.screenshot("smithing_grinding");
        scene.quickMove(3);
        scene.close();
        scene.onServer(server -> {
            ItemStack pickaxe = find(Scene.player(server), Items.IRON_PICKAXE);
            check(UpgradeType.GRINDING.currentLevel(pickaxe) == 4, "a diamond should grind an iron pickaxe to level IV: " + pickaxe);
            return null;
        });
    }

    private static boolean isCatalogue(Minecraft minecraft) {
        return minecraft.screen instanceof CatalogueScreen;
    }

    private static CatalogueMenu catalogue(Minecraft minecraft) {
        return ((CatalogueScreen) minecraft.screen).getMenu();
    }

    private static int indexOf(List<CatalogueEntry> entries, ResourceKey<Enchantment> enchantment) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).key().equals(enchantment)) return i;
        }
        throw new AssertionError(enchantment.identifier() + " should be offered by the catalogue");
    }

    private static int rowCenterY(int row) {
        return 16 + 2 + row * 20 + 9;
    }

    private static int levelButtonX(List<CatalogueEntry> entries, int row, int level) {
        int rowWidth = entries.size() > 4 ? 103 : 110;
        return 58 + rowWidth - 2 - entries.get(row).maxLevel() * 15 + (level - 1) * 15 + 7;
    }

    private static List<String> tooltip(Scene scene, ItemStack stack) {
        return scene.context.computeOnClient(minecraft -> stack.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL)
                .stream().map(Component::getString).toList());
    }

    private static void expectLine(Scene scene, ItemStack stack, String line) {
        List<String> lines = tooltip(scene, stack);
        check(lines.contains(line), stack + " tooltip should contain \"" + line + "\": " + lines);
    }

    private static void switchLanguage(Scene scene, String language) {
        CompletableFuture<Void> reload = scene.context.computeOnClient(minecraft -> {
            minecraft.options.languageCode = language;
            minecraft.getLanguageManager().setSelected(language);
            return minecraft.reloadResourcePacks();
        });
        scene.context.waitFor(minecraft -> reload.isDone(), 2400);
        scene.context.waitTicks(20);
    }

    private static ItemStack book(Minecraft minecraft, Map<ResourceKey<Enchantment>, Integer> enchantments) {
        HolderLookup.RegistryLookup<Enchantment> registry = minecraft.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.forEach((key, level) -> stored.set(registry.getOrThrow(key), level));
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
        return book;
    }

    private static void runCommandInSlot(Scene scene, int slot, String command) {
        scene.context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(slot));
        scene.context.waitTicks(2);
        scene.context.runOnClient(minecraft -> minecraft.player.connection.sendCommand(command));
        scene.context.waitTicks(5);
    }

    private static int rollCount(Scene scene, ResourceKey<LootTable> key, java.util.function.Predicate<ItemStack> matches) {
        return scene.onServer(server -> {
            LootTable table = server.reloadableRegistries().getLootTable(key);
            int count = 0;
            for (int i = 0; i < LOOT_ROLLS; i++) {
                LootParams params = new LootParams.Builder(server.overworld()).withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST);
                for (ItemStack stack : table.getRandomItems(params)) {
                    if (matches.test(stack)) count++;
                }
            }
            return count;
        });
    }

    private static void equipArmor(ServerPlayer player, java.util.function.Function<EquipmentSlot, ItemStack> stack) {
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            player.setItemSlot(slot, stack.apply(slot));
        }
    }

    private static void expectDamage(LivingEntity entity, DamageSource source, float expected, String setup) {
        try {
            Method absorb = LivingEntity.class.getDeclaredMethod("getDamageAfterArmorAbsorb", DamageSource.class, float.class);
            absorb.setAccessible(true);
            float damage = (float) absorb.invoke(entity, source, 10.0F);
            check(Math.abs(damage - expected) < 1.0E-3F, setup + " should turn 10 damage into " + expected + ", got " + damage);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static float exhaustionFrom(ServerPlayer player, float exhaustion) {
        try {
            Field level = FoodData.class.getDeclaredField("exhaustionLevel");
            level.setAccessible(true);
            level.setFloat(player.getFoodData(), 0.0F);
            player.causeFoodExhaustion(exhaustion);
            return level.getFloat(player.getFoodData());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static ItemStack withComponent(Item item, net.minecraft.core.component.DataComponentType<Integer> component, int value) {
        ItemStack stack = new ItemStack(item);
        stack.set(component, value);
        return stack;
    }

    private static ItemStack enchanted(MinecraftServer server, Item item, ResourceKey<Enchantment> enchantment, int level) {
        Holder<Enchantment> holder = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        ItemStack stack = new ItemStack(item);
        stack.enchant(holder, level);
        return stack;
    }

    private static ItemStack find(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(item)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static int count(ServerPlayer player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }
}
