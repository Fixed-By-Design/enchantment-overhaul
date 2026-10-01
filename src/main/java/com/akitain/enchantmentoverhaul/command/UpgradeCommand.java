package com.akitain.enchantmentoverhaul.command;

import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class UpgradeCommand {

    private static final SimpleCommandExceptionType ERROR_NO_ITEM = new SimpleCommandExceptionType(
            Component.translatable("commands.enchantment-overhaul.upgrade.failed.itemless"));
    private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(
            upgrade -> Component.translatable("commands.enchantment-overhaul.upgrade.failed.incompatible", upgrade));

    private UpgradeCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("upgrade").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
            for (UpgradeType upgrade : UpgradeType.values()) {
                command.then(Commands.literal(upgrade.getSerializedName())
                        .executes(c -> upgrade(c.getSource(), upgrade, UpgradeType.MAX_LEVEL))
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, UpgradeType.MAX_LEVEL))
                                .executes(c -> upgrade(c.getSource(), upgrade, IntegerArgumentType.getInteger(c, "level")))));
            }
            dispatcher.register(command);
        });
    }

    private static int upgrade(CommandSourceStack source, UpgradeType upgrade, int level) throws CommandSyntaxException {
        ItemStack stack = source.getPlayerOrException().getMainHandItem();
        if (stack.isEmpty()) throw ERROR_NO_ITEM.create();
        if (!upgrade.appliesTo(stack, source.getLevel())) throw ERROR_INCOMPATIBLE.create(upgrade.getDescription());

        upgrade.applyTo(stack, level);
        source.sendSuccess(() -> Component.translatable("commands.enchantment-overhaul.upgrade.success", upgrade.getFullname(level)), true);
        return 1;
    }
}
