package com.akitain.enchantmentoverhaul.client;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.enchant.BookshelfScanner;
import com.akitain.enchantmentoverhaul.enchant.CatalogueScreenHandler;
import com.akitain.enchantmentoverhaul.enchant.CatalogueScreenHandler.CatalogueEntry;
import com.akitain.enchantmentoverhaul.enchant.EnchantmentCosts;
import com.akitain.enchantmentoverhaul.enchant.SlotSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class CatalogueScreen extends AbstractContainerScreen<CatalogueScreenHandler> {

    private static final int BG_W = 176;
    private static final int BG_H = 200;

    private static final int CAT_X = 56;
    private static final int CAT_Y = 16;
    private static final int CAT_W = 114;
    private static final int ROW_H = 19;
    private static final int VISIBLE_ROWS = 4;
    private static final int CAT_H = 86;
    private static final int SCROLLBAR_W = 7;

    private static final int BOOK_X = 3;
    private static final int BOOK_Y = 16;

    private static final int LV_BTN = 14;
    private static final int LV_GAP = 1;

    private static final int SLOT_BAR_Y = 104;

    private static final int TEXT_LIGHT = 0xFFD8C8F0;

    private static final Component INSERT_ITEM = Component.translatable("screen.enchantment-overhaul.catalogue.insert_item");
    private static final Component NO_ENCHANTMENTS = Component.translatable("screen.enchantment-overhaul.catalogue.no_enchantments");

    private static final Identifier SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
    private static final Identifier SLOT_AMETHYST = Identifier.withDefaultNamespace("container/slot/amethyst_shard");
    private static final Identifier BOOK_TEXTURE = Identifier.withDefaultNamespace("textures/entity/enchantment/enchanting_table_book.png");
    private static final Identifier BACKGROUND_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue.png");
    private static final Identifier ROW_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/row.png");
    private static final Identifier ROW_HOVER_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/row_hover.png");
    private static final Identifier ROW_SELECTED_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/row_selected.png");
    private static final Identifier ROW_DISABLED_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/row_disabled.png");
    private static final Identifier ROW_DISABLED_HOVER_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/row_disabled_hover.png");
    private static final Identifier LEVEL_SELECTED_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/level_selected.png");
    private static final Identifier LEVEL_SELECTED_AVAILABLE_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/level_selected_available.png");
    private static final Identifier LEVEL_SELECTED_UNAVAILABLE_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/level_selected_unavailable.png");
    private static final Identifier LEVEL_AVAILABLE_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/level_available.png");
    private static final Identifier LEVEL_UNAVAILABLE_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/level_unavailable.png");
    private static final Identifier SCROLLBAR_TRACK_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/scrollbar_track.png");
    private static final Identifier SCROLLBAR_THUMB_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/scrollbar_thumb.png");
    private static final Identifier SLOT_USED_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/slot_used.png");
    private static final Identifier SLOT_PENDING_ON_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/slot_pending_on.png");
    private static final Identifier SLOT_PENDING_OFF_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/slot_pending_off.png");
    private static final Identifier SLOT_FREE_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/slot_free.png");
    private static final Identifier SLOT_PENALTY_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/catalogue/slot_penalty.png");

    private static final Identifier[] SLOT_PLACEHOLDERS = { SLOT_SWORD, SLOT_AMETHYST };

    private static final Style SGA_STYLE = Style.EMPTY
            .withFont(new net.minecraft.network.chat.FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "alt")));
    private static final String SGA_CHARS = "abcdefghijklmnopqrstuvwxyz";

    private final String[] sgaRows = new String[20];

    private net.minecraft.client.model.object.book.BookModel bookModel;
    private float scrollAmount;
    private int scrollOffset;
    private boolean scrolling;
    private ItemStack lastItem = ItemStack.EMPTY;

    public CatalogueScreen(CatalogueScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, BG_W, BG_H);
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 110;
        for (int i = 0; i < sgaRows.length; i++) sgaRows[i] = randomSga(18);
    }

    @Override
    protected void init() {
        super.init();
        menu.rebuildEntries();
        bookModel = new net.minecraft.client.model.object.book.BookModel(
                net.minecraft.client.model.object.book.BookModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float deltaTicks) {
        checkItemChanged();
        super.extractRenderState(gfx, mouseX, mouseY, deltaTicks);
        drawCatalogueTooltip(gfx, mouseX, mouseY);
    }

    private void checkItemChanged() {
        ItemStack current = menu.getSlot(0).getItem();
        if (!ItemStack.matches(current, lastItem)) {
            lastItem = current.copy();
            menu.rebuildEntries();
            scrollAmount = 0;
            scrollOffset = 0;
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float deltaTicks) {
        int x = this.leftPos, y = this.topPos;

        gfx.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 0, 0, BG_W, BG_H, BG_W, BG_H);
        drawBook(gfx, x, y);
        drawSlotPlaceholders(gfx, x, y);
        drawCatalogue(gfx, x, y, mouseX, mouseY);
        drawSlotBar(gfx, x, y);

        super.extractContents(gfx, mouseX, mouseY, deltaTicks);
    }

    private void drawBook(GuiGraphicsExtractor gfx, int x, int y) {
        if (bookModel == null) return;
        int bx = x + BOOK_X;
        int by = y + BOOK_Y;
        gfx.book(bookModel, BOOK_TEXTURE, 40.0f, 0.9f, 0.1f, bx, by, bx + 50, by + 40);
    }

    private void drawSlotPlaceholders(GuiGraphicsExtractor gfx, int x, int y) {
        for (int i = 0; i < 2; i++) {
            Slot slot = menu.slots.get(i);
            if (slot.getItem().isEmpty()) {
                gfx.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_PLACEHOLDERS[i],
                        x + slot.x, y + slot.y, 16, 16);
            }
        }
    }

    private void drawCatalogue(GuiGraphicsExtractor gfx, int x, int y, int mouseX, int mouseY) {
        int cx = x + CAT_X, cy = y + CAT_Y;

        List<CatalogueEntry> entries = menu.getEntries();
        if (entries.isEmpty()) {
            Component hint = menu.getSlot(0).getItem().isEmpty() ? INSERT_ITEM : NO_ENCHANTMENTS;
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(hint, CAT_W - 8);
            int totalH = lines.size() * (font.lineHeight + 2);
            int startY = cy + (CAT_H - totalH) / 2;
            for (int i = 0; i < lines.size(); i++) {
                int lw = font.width(lines.get(i));
                gfx.text(font, lines.get(i),
                        cx + (CAT_W - lw) / 2,
                        startY + i * (font.lineHeight + 2), 0xFF808080, false);
            }
            return;
        }

        int rowW = shouldScroll() ? CAT_W - SCROLLBAR_W - 4 : CAT_W - 4;
        int end = Math.min(scrollOffset + VISIBLE_ROWS, entries.size());
        for (int i = scrollOffset; i < end; i++) {
            int row = i - scrollOffset;
            int ry = cy + 2 + row * (ROW_H + 1);
            drawRow(gfx, entries.get(i), i, cx + 2, ry, rowW, mouseX, mouseY);
        }

        drawScrollbar(gfx, cx + CAT_W - SCROLLBAR_W - 2, cy + 2, CAT_H - 4);
    }

    private boolean isLevelAffordable(CatalogueEntry entry, int level) {
        if (level <= entry.currentLevel()) return false;

        ItemStack item = menu.getSlot(0).getItem();
        int slotCost = EnchantmentCosts.slotCost(entry.entry(), level, entry.currentLevel());
        if (SlotSystem.getAvailableSlots(item) < slotCost) return false;

        if (Minecraft.getInstance().player.isCreative()) return true;

        ItemStack reagent = menu.getSlot(1).getItem();
        Item reagentItem = EnchantmentCosts.reagent(entry.key());
        int reagentCost = EnchantmentCosts.reagentCost(level, entry.currentLevel(), menu.getNormalBookshelves());
        int xpCost = EnchantmentCosts.xpCost(entry.key(), level, entry.currentLevel());
        int playerXp = Minecraft.getInstance().player.experienceLevel;

        return reagent.is(reagentItem) && reagent.getCount() >= reagentCost
                && (!menu.isXpCostEnabled() || playerXp >= xpCost);
    }

    private boolean isAnyLevelAffordable(CatalogueEntry entry) {
        for (int lv = 1; lv <= entry.maxLevel(); lv++) {
            if (isLevelAffordable(entry, lv)) return true;
        }
        return false;
    }

    private void drawRow(GuiGraphicsExtractor gfx, CatalogueEntry entry, int idx, int rx, int ry, int rw, int mx, int my) {
        boolean selected = idx == menu.getSelectedIndex();
        boolean hovered = mx >= rx && mx < rx + rw && my >= ry && my < ry + ROW_H;
        boolean affordable = isAnyLevelAffordable(entry);

        Identifier rowTexture;
        if (selected) {
            rowTexture = ROW_SELECTED_TEXTURE;
        } else if (affordable) {
            rowTexture = hovered ? ROW_HOVER_TEXTURE : ROW_TEXTURE;
        } else {
            rowTexture = hovered ? ROW_DISABLED_HOVER_TEXTURE : ROW_DISABLED_TEXTURE;
        }
        gfx.blit(RenderPipelines.GUI_TEXTURED, rowTexture, rx, ry, 0, 0, rw, ROW_H, 110, ROW_H);

        int lvX = rx + rw - 2 - entry.maxLevel() * (LV_BTN + LV_GAP);
        int sgaMaxX = lvX - 3;

        int sgaColor = selected ? 0xFFE0C0E0 : (affordable ? (hovered ? 0xFFB0A080 : 0xFF988870) : 0xFF605848);
        String sga = sgaRows[idx % sgaRows.length];
        int sgaY = ry + (ROW_H - 8) / 2;
        Component sgaText = Component.literal(trimToWidth(sga, sgaMaxX - rx - 3)).setStyle(SGA_STYLE);
        gfx.text(font, sgaText, rx + 3, sgaY, sgaColor, true);
        int selLv = selected ? menu.getSelectedLevel() : 0;

        for (int lv = 1; lv <= entry.maxLevel(); lv++) {
            boolean owned = lv <= entry.currentLevel();
            boolean lvSel = selected && lv == selLv;
            boolean lvAffordable = isLevelAffordable(entry, lv);

            Identifier lvTexture;
            int lvColor;
            if (owned) {
                lvTexture = LEVEL_UNAVAILABLE_TEXTURE;
                lvColor = 0xFF3E7A4E;
            } else if (lvSel) {
                lvTexture = LEVEL_SELECTED_TEXTURE;
                lvColor = 0xFFC0FF80;
            } else if (selected) {
                lvTexture = lvAffordable ? LEVEL_SELECTED_AVAILABLE_TEXTURE : LEVEL_SELECTED_UNAVAILABLE_TEXTURE;
                lvColor = lvAffordable ? 0xFFB890B8 : 0xFF685068;
            } else {
                lvTexture = lvAffordable ? LEVEL_AVAILABLE_TEXTURE : LEVEL_UNAVAILABLE_TEXTURE;
                lvColor = lvAffordable ? 0xFF7A6A5A : 0xFF504840;
            }

            int lvY = ry + (ROW_H - LV_BTN) / 2;
            gfx.blit(RenderPipelines.GUI_TEXTURED, lvTexture, lvX, lvY, 0, 0, LV_BTN, LV_BTN, LV_BTN, LV_BTN);

            Component r = levelName(lv);
            int tw = font.width(r);
            gfx.text(font, r, lvX + (LV_BTN - tw) / 2, lvY + 3, lvColor, true);
            lvX += LV_BTN + LV_GAP;
        }
    }

    private void drawCatalogueTooltip(GuiGraphicsExtractor gfx, int mx, int my) {
        int cx = this.leftPos + CAT_X, cy = this.topPos + CAT_Y;
        if (mx < cx || mx >= cx + CAT_W || my < cy || my >= cy + CAT_H) return;

        List<CatalogueEntry> entries = menu.getEntries();
        int idx = scrollOffset + (my - cy - 2) / (ROW_H + 1);
        if (idx < 0 || idx >= entries.size()) return;

        CatalogueEntry entry = entries.get(idx);
        ResourceKey<Enchantment> key = entry.key();
        boolean selected = idx == menu.getSelectedIndex();

        int rowW = shouldScroll() ? CAT_W - SCROLLBAR_W - 4 : CAT_W - 4;
        int rx = cx + 2;
        int lvX = rx + rowW - 2 - entry.maxLevel() * (LV_BTN + LV_GAP);
        int level;
        if (mx >= lvX) {
            int lvIdx = (mx - lvX) / (LV_BTN + LV_GAP);
            level = Math.max(entry.currentLevel() + 1, Math.min(lvIdx + 1, entry.maxLevel()));
        } else if (selected) {
            level = menu.getSelectedLevel();
        } else {
            level = entry.currentLevel() + 1;
        }

        Item reagentItem = EnchantmentCosts.reagent(key);
        int reagentCost = EnchantmentCosts.reagentCost(level, entry.currentLevel(), menu.getNormalBookshelves());
        int xpCost = EnchantmentCosts.xpCost(key, level, entry.currentLevel());
        int slotCost = EnchantmentCosts.slotCost(entry.entry(), level, entry.currentLevel());

        ItemStack item = menu.getSlot(0).getItem();
        ItemStack reagent = menu.getSlot(1).getItem();
        int playerXp = Minecraft.getInstance().player.experienceLevel;

        boolean hasReagent = reagent.is(reagentItem) && reagent.getCount() >= reagentCost;
        boolean hasXp = playerXp >= xpCost;
        boolean hasSlots = SlotSystem.getAvailableSlots(item) >= slotCost;

        MutableComponent name = entry.entry().value().description().copy().append(CommonComponents.SPACE);
        if (entry.currentLevel() > 0) name.append(levelName(entry.currentLevel())).append(" → ");
        name.append(levelName(level));
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(name.withStyle(entry.entry().is(EnchantmentTags.CURSE) ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.empty());
        tooltip.add(costLine(new ItemStack(reagentItem).getHoverName(), reagentCost, hasReagent));
        if (menu.isXpCostEnabled()) tooltip.add(costLine(label("xp_levels"), xpCost, hasXp));
        if (slotCost > 0) {
            tooltip.add(costLine(label("slots"), slotCost, hasSlots));
        } else {
            tooltip.add(Component.translatable("screen.enchantment-overhaul.catalogue.cost", label("slots")).withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("+1").withStyle(ChatFormatting.GREEN)));
        }

        if (menu.getNormalBookshelves() > 0) {
            long pct = Math.round(BookshelfScanner.reagentDiscount(menu.getNormalBookshelves()) * 100);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("screen.enchantment-overhaul.catalogue.bookshelves",
                            menu.getNormalBookshelves(), pct)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        gfx.setComponentTooltipForNextFrame(font, tooltip, mx, my);
    }

    private Component costLine(Component label, int amount, boolean has) {
        ChatFormatting color = has ? ChatFormatting.GREEN : ChatFormatting.RED;
        return Component.translatable("screen.enchantment-overhaul.catalogue.cost", label).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(amount)).withStyle(color));
    }

    private static Component label(String key) {
        return Component.translatable("screen.enchantment-overhaul.catalogue." + key);
    }

    private void drawScrollbar(GuiGraphicsExtractor gfx, int sx, int sy, int sh) {
        if (!shouldScroll()) return;
        gfx.blit(RenderPipelines.GUI_TEXTURED, SCROLLBAR_TRACK_TEXTURE, sx, sy, 0, 0, SCROLLBAR_W, sh, SCROLLBAR_W, 82);

        int thumbH = Math.max(10, sh * VISIBLE_ROWS / menu.getEntries().size());
        int thumbY = sy + (int) ((sh - thumbH) * scrollAmount);
        gfx.blit(RenderPipelines.GUI_TEXTURED, SCROLLBAR_THUMB_TEXTURE, sx, thumbY, 0, 0, SCROLLBAR_W, thumbH, SCROLLBAR_W, 82);
    }

    private void drawSlotBar(GuiGraphicsExtractor gfx, int x, int y) {
        ItemStack item = menu.getSlot(0).getItem();
        if (item.isEmpty()) return;

        int max = SlotSystem.getMaxSlots(item);
        int used = SlotSystem.getUsedSlots(item);
        int penalty = SlotSystem.getGrindstonePenalty(item);
        int totalPips = max + penalty;
        if (totalPips <= 0) return;

        int pendingCost = 0;
        if (menu.getSelectedIndex() >= 0 && menu.getSelectedIndex() < menu.getEntries().size()) {
            CatalogueEntry e = menu.getEntries().get(menu.getSelectedIndex());
            pendingCost = EnchantmentCosts.slotCost(e.entry(), menu.getSelectedLevel(), e.currentLevel());
        }

        int barY = y + SLOT_BAR_Y;
        int ph = 6;
        String countText = used + "/" + max;
        int countW = font.width(countText);
        int barLeft = x + CAT_X;
        int barRight = x + CAT_X + CAT_W - countW - 4;
        int availableW = barRight - barLeft;
        int gap = totalPips > 1 ? Math.max(1, Math.min(2, (availableW - totalPips * 4) / (totalPips - 1))) : 2;
        int pw = Math.max(4, (availableW - gap * (totalPips - 1)) / totalPips);
        int barX = barLeft;

        for (int i = 0; i < totalPips; i++) {
            int px = barX + i * (pw + gap);
            Identifier pipTexture;
            if (i < used) {
                pipTexture = SLOT_USED_TEXTURE;
            } else if (i < used + pendingCost) {
                boolean blink = (System.currentTimeMillis() / 400) % 2 == 0;
                pipTexture = blink ? SLOT_PENDING_ON_TEXTURE : SLOT_PENDING_OFF_TEXTURE;
            } else if (i < max) {
                pipTexture = SLOT_FREE_TEXTURE;
            } else {
                pipTexture = SLOT_PENALTY_TEXTURE;
            }
            gfx.blit(RenderPipelines.GUI_TEXTURED, pipTexture, px, barY, 0, 0, pw, ph, 128, ph);
        }

        int textX = barRight + 4;
        gfx.text(font, countText, textX, barY - 1, TEXT_LIGHT, true);
    }

    // --- Input ---
    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int x = this.leftPos, y = this.topPos;

        if (clickRow(mx, my, x, y)) return true;
        if (clickScroll(mx, my, x, y)) return true;

        return super.mouseClicked(click, doubled);
    }

    private boolean clickRow(double mx, double my, int x, int y) {
        int cx = x + CAT_X, cy = y + CAT_Y;
        int rowW = shouldScroll() ? CAT_W - SCROLLBAR_W - 4 : CAT_W - 4;
        if (mx < cx + 2 || mx >= cx + 2 + rowW || my < cy + 2 || my >= cy + CAT_H - 2) return false;

        List<CatalogueEntry> entries = menu.getEntries();
        int idx = scrollOffset + (int) (my - cy - 2) / (ROW_H + 1);
        if (idx < 0 || idx >= entries.size()) return false;

        CatalogueEntry entry = entries.get(idx);
        int rx = cx + 2;
        int lvX = rx + rowW - 2 - entry.maxLevel() * (LV_BTN + LV_GAP);
        int level = entry.currentLevel() + 1;
        if (mx >= lvX) {
            int lvIdx = (int) (mx - lvX) / (LV_BTN + LV_GAP);
            if (lvIdx >= 0 && lvIdx < entry.maxLevel()) level = lvIdx + 1;
        }

        if (!isLevelAffordable(entry, level)) return false;

        menu.setSelection(idx, level);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, idx * 10 + (level - 1));
        return true;
    }

    private boolean clickScroll(double mx, double my, int x, int y) {
        int sx = x + CAT_X + CAT_W - SCROLLBAR_W - 2;
        int sy = y + CAT_Y + 2;
        int sh = CAT_H - 4;
        if (mx >= sx && mx < sx + SCROLLBAR_W && my >= sy && my < sy + sh && shouldScroll()) {
            scrolling = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        if (scrolling && shouldScroll()) {
            float top = this.topPos + CAT_Y + 2;
            float sh = CAT_H - 4;
            scrollAmount = Mth.clamp((float) (click.y() - top) / sh, 0, 1);
            scrollOffset = (int) (scrollAmount * getMaxScroll());
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        scrolling = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        if (super.mouseScrolled(mx, my, hAmt, vAmt)) return true;
        if (!shouldScroll()) return false;
        int max = getMaxScroll();
        scrollOffset = Mth.clamp(scrollOffset - (int) vAmt, 0, max);
        scrollAmount = max > 0 ? (float) scrollOffset / max : 0;
        return true;
    }

    private boolean shouldScroll() { return menu.getEntries().size() > VISIBLE_ROWS; }
    private int getMaxScroll() { return Math.max(0, menu.getEntries().size() - VISIBLE_ROWS); }

    private String trimToWidth(String text, int maxWidth) {
        int w = 0;
        for (int i = 0; i < text.length(); i++) {
            w += font.width(Component.literal(String.valueOf(text.charAt(i))).setStyle(SGA_STYLE));
            if (w > maxWidth) return text.substring(0, i);
        }
        return text;
    }

    private static String randomSga(int length) {
        Random rng = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(SGA_CHARS.charAt(rng.nextInt(SGA_CHARS.length())));
        return sb.toString();
    }

    private static Component levelName(int level) {
        return Component.translatable("enchantment.level." + level);
    }
}
