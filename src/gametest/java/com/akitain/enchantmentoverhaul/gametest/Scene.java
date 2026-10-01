package com.akitain.enchantmentoverhaul.gametest;

import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import org.apache.commons.lang3.function.FailableFunction;

final class Scene {

    static final int HOTBAR_START = 27;

    final ClientGameTestContext context;
    final TestServerContext server;
    final String player;
    final BlockPos origin;
    final BlockPos enchantingTable;
    final BlockPos anvil;
    final BlockPos grindstone;
    final BlockPos smithingTable;

    Scene(ClientGameTestContext context, TestServerContext server) {
        this.context = context;
        this.server = server;
        this.player = context.computeOnClient(minecraft -> minecraft.getUser().getName());
        this.origin = server.computeOnServer(minecraftServer -> player(minecraftServer).blockPosition());
        this.enchantingTable = this.origin.offset(0, 0, 3);
        this.anvil = this.origin.offset(-3, 0, 0);
        this.grindstone = this.origin.offset(0, 0, -3);
        this.smithingTable = this.origin.offset(3, 0, 0);
    }

    static ServerPlayer player(MinecraftServer server) {
        return server.getPlayerList().getPlayers().getFirst();
    }

    void build() {
        this.context.getInput().resizeWindow(1920, 1080);
        this.context.runOnClient(minecraft -> {
            minecraft.options.guiScale().set(4);
            minecraft.options.hideGui = true;
            minecraft.resizeGui();
        });
        command("gamemode survival " + this.player);
        command("gamerule advance_time false");
        command("time set noon");
        buildEnchantingRoom();
        setBlock(this.anvil, "anvil");
        setBlock(this.grindstone, "grindstone[face=floor]");
        setBlock(this.smithingTable, "smithing_table");
        command("tp %s %d.5 %d %d.5 0 20".formatted(this.player, this.origin.getX(), this.origin.getY(), this.origin.getZ()));
        this.context.waitTicks(20);
    }

    private void buildEnchantingRoom() {
        setBlock(this.enchantingTable, "enchanting_table");
        setBlock(this.enchantingTable.offset(-2, 0, 0), chiseledBookshelf("east", "minecraft:fortune", "minecraft:silk_touch", "minecraft:mending",
                "minecraft:vanishing_curse", "enchantment-overhaul:curse_of_fragility", "minecraft:unbreaking"));
        setBlock(this.enchantingTable.offset(2, 0, 0), chiseledBookshelf("west", "minecraft:efficiency", "enchantment-overhaul:veil"));
        for (int x = -2; x <= 2; x++) {
            setBlock(this.enchantingTable.offset(x, 0, 2), "bookshelf");
            setBlock(this.enchantingTable.offset(x, 1, 2), "bookshelf");
        }
        setBlock(this.enchantingTable.offset(-2, 1, 0), "bookshelf");
        setBlock(this.enchantingTable.offset(2, 1, 0), "bookshelf");
        setBlock(this.enchantingTable.offset(-2, 0, 1), "bookshelf");
        setBlock(this.enchantingTable.offset(2, 0, 1), "bookshelf");
        setBlock(this.enchantingTable.offset(-2, 1, 1), "bookshelf");
    }

    private static String chiseledBookshelf(String facing, String... enchantments) {
        StringBuilder state = new StringBuilder("chiseled_bookshelf[facing=" + facing);
        StringBuilder items = new StringBuilder();
        for (int slot = 0; slot < enchantments.length; slot++) {
            state.append(",slot_").append(slot).append("_occupied=true");
            if (slot > 0) items.append(',');
            items.append("{Slot:%db,id:\"minecraft:enchanted_book\",count:1,components:{\"minecraft:stored_enchantments\":{\"%s\":1}}}".formatted(slot, enchantments[slot]));
        }
        return state.append("]{Items:[").append(items).append("]}").toString();
    }

    void command(String command) {
        this.server.runCommand(command);
    }

    void setBlock(BlockPos pos, String block) {
        command("setblock %d %d %d %s".formatted(pos.getX(), pos.getY(), pos.getZ(), block));
    }

    void resetInventory(String... items) {
        command("clear " + this.player);
        for (String item : items) command("give " + this.player + " " + item);
        this.context.waitTicks(5);
    }

    <T> T onServer(FailableFunction<MinecraftServer, T, RuntimeException> function) {
        return this.server.computeOnServer(function);
    }

    void open(BlockPos block, Predicate<Minecraft> opened) {
        this.context.getInput().lookAt(block);
        this.context.waitTick();
        this.context.getInput().pressKey(options -> options.keyUse);
        this.context.waitFor(opened);
        this.context.waitTicks(5);
    }

    void open(BlockPos block) {
        open(block, minecraft -> minecraft.screen instanceof AbstractContainerScreen<?>);
    }

    void close() {
        this.context.getInput().pressKey(256);
        this.context.waitTicks(5);
    }

    void quickMove(int slot) {
        this.context.runOnClient(minecraft -> {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) minecraft.screen;
            minecraft.gameMode.handleContainerInput(screen.getMenu().containerId, slot, 0, ContainerInput.QUICK_MOVE, minecraft.player);
        });
        this.context.waitTicks(5);
    }

    int hotbarSlot(int menuInventoryStart, int hotbarIndex) {
        return menuInventoryStart + HOTBAR_START + hotbarIndex;
    }

    void hover(int imageWidth, int imageHeight, int x, int y) {
        double[] position = this.context.computeOnClient(minecraft -> {
            int left = (minecraft.screen.width - imageWidth) / 2;
            int top = (minecraft.screen.height - imageHeight) / 2;
            double scale = (double) minecraft.getWindow().getScreenWidth() / minecraft.getWindow().getGuiScaledWidth();
            return new double[] {(left + x + 0.5) * scale, (top + y + 0.5) * scale};
        });
        this.context.getInput().setCursorPos(position[0], position[1]);
        this.context.waitTicks(3);
    }

    void click(int imageWidth, int imageHeight, int x, int y) {
        hover(imageWidth, imageHeight, x, y);
        this.context.getInput().pressMouse(0);
        this.context.waitTicks(5);
    }

    void screenshot(String name) {
        this.context.runOnClient(minecraft -> {
            minecraft.getToastManager().clear();
            minecraft.gui.getChat().clearMessages(false);
        });
        this.context.waitTick();
        this.context.takeScreenshot(name);
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
