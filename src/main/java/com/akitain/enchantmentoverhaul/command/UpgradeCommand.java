package com.akitain.enchantmentoverhaul.command;

import com.akitain.enchantmentoverhaul.smithing.UpgradeType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class UpgradeCommand {

    private static final int MAX_LEVEL = 5;
    private static final SimpleCommandExceptionType ERROR_NO_ITEM = new SimpleCommandExceptionType(
            Component.translatable("commands.enchantment-overhaul.upgrade.failed.itemless"));
    private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(
            upgrade -> Component.translatable("commands.enchantment-overhaul.upgrade.failed.incompatible", upgrade));

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("upgrade")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(literal("honing").executes(context -> apply(context.getSource(), UpgradeType.HONING, MAX_LEVEL))
                                .then(argument("level", IntegerArgumentType.integer(1, MAX_LEVEL))
                                        .executes(context -> apply(context.getSource(), UpgradeType.HONING, IntegerArgumentType.getInteger(context, "level")))))
                        .then(literal("warding").executes(context -> apply(context.getSource(), UpgradeType.WARDING, MAX_LEVEL))
                                .then(argument("level", IntegerArgumentType.integer(1, MAX_LEVEL))
                                        .executes(context -> apply(context.getSource(), UpgradeType.WARDING, IntegerArgumentType.getInteger(context, "level")))))
                        .then(literal("tempering").executes(context -> apply(context.getSource(), UpgradeType.TEMPERING, MAX_LEVEL))
                                .then(argument("level", IntegerArgumentType.integer(1, MAX_LEVEL))
                                        .executes(context -> apply(context.getSource(), UpgradeType.TEMPERING, IntegerArgumentType.getInteger(context, "level")))))
                        .then(literal("grinding").executes(context -> apply(context.getSource(), UpgradeType.GRINDING, MAX_LEVEL))
                                .then(argument("level", IntegerArgumentType.integer(1, MAX_LEVEL))
                                        .executes(context -> apply(context.getSource(), UpgradeType.GRINDING, IntegerArgumentType.getInteger(context, "level")))))));
    }

    private static int apply(CommandSourceStack source, UpgradeType type, int level) throws CommandSyntaxException {
        ItemStack stack = source.getPlayerOrException().getMainHandItem();
        if (stack.isEmpty()) throw ERROR_NO_ITEM.create();
        if (!type.appliesTo(stack, source.getLevel())) throw ERROR_INCOMPATIBLE.create(type.getDescription());

        type.applyTo(stack, level);
        source.sendSuccess(() -> Component.translatable("commands.enchantment-overhaul.upgrade.success", type.getFullname(level)), true);
        return 1;
    }
}
