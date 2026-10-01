package com.akitain.enchantmentoverhaul.client;

import com.akitain.enchantmentoverhaul.EnchantmentOverhaul;
import com.akitain.enchantmentoverhaul.enchant.BookshelfScanner;
import com.akitain.enchantmentoverhaul.enchant.CatalogueMenu;
import com.akitain.enchantmentoverhaul.enchant.CatalogueMenu.CatalogueEntry;
import com.akitain.enchantmentoverhaul.enchant.EnchantmentCosts;
import com.akitain.enchantmentoverhaul.enchant.SlotSystem;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CatalogueScreen extends AbstractContainerScreen<CatalogueMenu> {

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 200;
    private static final int LIST_X = 56;
    private static final int LIST_Y = 16;
    private static final int LIST_WIDTH = 114;
    private static final int LIST_HEIGHT = 86;
    private static final int ROW_HEIGHT = 19;
    private static final int ROW_SPACING = 20;
    private static final int ROW_TEXTURE_WIDTH = 110;
    private static final int VISIBLE_ROWS = 4;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_HEIGHT = LIST_HEIGHT - 4;
    private static final int LEVEL_BUTTON_SIZE = 14;
    private static final int LEVEL_BUTTON_SPACING = 15;
    private static final int SLOT_BAR_Y = 104;
    private static final int PIP_HEIGHT = 6;
    private static final int PIP_TEXTURE_WIDTH = 128;

    private static final Identifier BACKGROUND = texture("catalogue");
    private static final Identifier BOOK_TEXTURE = Identifier.withDefaultNamespace("textures/entity/enchantment/enchanting_table_book.png");
    private static final Identifier ROW = texture("catalogue/row");
    private static final Identifier ROW_HOVER = texture("catalogue/row_hover");
    private static final Identifier ROW_SELECTED = texture("catalogue/row_selected");
    private static final Identifier ROW_DISABLED = texture("catalogue/row_disabled");
    private static final Identifier ROW_DISABLED_HOVER = texture("catalogue/row_disabled_hover");
    private static final Identifier LEVEL_SELECTED = texture("catalogue/level_selected");
    private static final Identifier LEVEL_SELECTED_AVAILABLE = texture("catalogue/level_selected_available");
    private static final Identifier LEVEL_SELECTED_UNAVAILABLE = texture("catalogue/level_selected_unavailable");
    private static final Identifier LEVEL_AVAILABLE = texture("catalogue/level_available");
    private static final Identifier LEVEL_UNAVAILABLE = texture("catalogue/level_unavailable");
    private static final Identifier SCROLLBAR_TRACK = texture("catalogue/scrollbar_track");
    private static final Identifier SCROLLBAR_THUMB = texture("catalogue/scrollbar_thumb");
    private static final Identifier SLOT_USED = texture("catalogue/slot_used");
    private static final Identifier SLOT_PENDING_ON = texture("catalogue/slot_pending_on");
    private static final Identifier SLOT_PENDING_OFF = texture("catalogue/slot_pending_off");
    private static final Identifier SLOT_FREE = texture("catalogue/slot_free");
    private static final Identifier SLOT_PENALTY = texture("catalogue/slot_penalty");
    private static final Identifier[] EMPTY_SLOT_SPRITES = {
            Identifier.withDefaultNamespace("container/slot/sword"),
            Identifier.withDefaultNamespace("container/slot/amethyst_shard")
    };

    private static final Component INSERT_ITEM = Component.translatable("screen.enchantment-overhaul.catalogue.insert_item");
    private static final Component NO_ENCHANTMENTS = Component.translatable("screen.enchantment-overhaul.catalogue.no_enchantments");
    private static final Component XP_LEVELS = Component.translatable("screen.enchantment-overhaul.catalogue.xp_levels");
    private static final Component SLOTS = Component.translatable("screen.enchantment-overhaul.catalogue.slots");

    private BookModel bookModel;
    private float scrollOffs;
    private int startIndex;
    private boolean scrolling;

    public CatalogueScreen(CatalogueMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 110;
        menu.registerUpdateListener(this::containerChanged);
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(EnchantmentOverhaul.MOD_ID, "textures/gui/container/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();
        this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    private void containerChanged() {
        this.scrollOffs = 0.0F;
        this.startIndex = 0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        graphics.book(this.bookModel, BOOK_TEXTURE, 40.0F, 0.9F, 0.1F, this.leftPos + 3, this.topPos + 16, this.leftPos + 53, this.topPos + 56);
        this.extractEmptySlotSprites(graphics);
        this.extractEntries(graphics, mouseX, mouseY);
        this.extractScrollbar(graphics, mouseX, mouseY);
        this.extractSlotBar(graphics);
    }

    private void extractEmptySlotSprites(GuiGraphicsExtractor graphics) {
        for (int i = 0; i < EMPTY_SLOT_SPRITES.length; i++) {
            var slot = this.menu.getSlot(i);
            if (!slot.hasItem()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_SLOT_SPRITES[i], this.leftPos + slot.x, this.topPos + slot.y, 16, 16);
            }
        }
    }

    private void extractEntries(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<CatalogueEntry> entries = this.menu.getEntries();
        if (entries.isEmpty()) {
            this.extractHint(graphics);
            return;
        }

        int endIndex = Math.min(this.startIndex + VISIBLE_ROWS, entries.size());
        for (int index = this.startIndex; index < endIndex; index++) {
            this.extractEntry(graphics, entries.get(index), index, this.rowTop(index - this.startIndex), mouseX, mouseY);
        }
    }

    private void extractHint(GuiGraphicsExtractor graphics) {
        Component hint = this.menu.getSlot(CatalogueMenu.ITEM_SLOT).hasItem() ? NO_ENCHANTMENTS : INSERT_ITEM;
        List<FormattedCharSequence> lines = this.font.split(hint, LIST_WIDTH - 8);
        int lineHeight = this.font.lineHeight + 2;
        int top = this.topPos + LIST_Y + (LIST_HEIGHT - lines.size() * lineHeight) / 2;
        for (int i = 0; i < lines.size(); i++) {
            int x = this.leftPos + LIST_X + (LIST_WIDTH - this.font.width(lines.get(i))) / 2;
            graphics.text(this.font, lines.get(i), x, top + i * lineHeight, 0xFF808080, false);
        }
    }

    private void extractEntry(GuiGraphicsExtractor graphics, CatalogueEntry entry, int index, int top, int mouseX, int mouseY) {
        int left = this.rowLeft();
        int width = this.rowWidth();
        boolean selected = index == this.menu.getSelectedIndex();
        boolean hovered = mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + ROW_HEIGHT;
        boolean affordable = this.isAnyLevelAffordable(entry);

        Identifier row = selected ? ROW_SELECTED : affordable ? (hovered ? ROW_HOVER : ROW) : (hovered ? ROW_DISABLED_HOVER : ROW_DISABLED);
        graphics.blit(RenderPipelines.GUI_TEXTURED, row, left, top, 0, 0, width, ROW_HEIGHT, ROW_TEXTURE_WIDTH, ROW_HEIGHT);
        if (hovered && affordable) graphics.requestCursor(CursorTypes.POINTING_HAND);

        int buttonsLeft = this.levelButtonsLeft(entry);
        int nameColor = selected ? 0xFFE0C0E0 : affordable ? (hovered ? 0xFFB0A080 : 0xFF988870) : 0xFF605848;
        EnchantmentNames.getInstance().initSeed(entry.key().identifier().hashCode());
        FormattedText name = EnchantmentNames.getInstance().getRandomName(this.font, buttonsLeft - left - 6);
        graphics.textWithWordWrap(this.font, name, left + 3, top + (ROW_HEIGHT - 8) / 2, buttonsLeft - left - 6, nameColor, true);

        for (int level = 1; level <= entry.maxLevel(); level++) {
            int x = buttonsLeft + (level - 1) * LEVEL_BUTTON_SPACING;
            int y = top + (ROW_HEIGHT - LEVEL_BUTTON_SIZE) / 2;
            this.extractLevelButton(graphics, entry, level, selected, x, y);
        }
    }

    private void extractLevelButton(GuiGraphicsExtractor graphics, CatalogueEntry entry, int level, boolean rowSelected, int x, int y) {
        boolean affordable = this.menu.canEnchant(entry, level);
        Identifier texture;
        int color;
        if (level <= entry.currentLevel()) {
            texture = LEVEL_UNAVAILABLE;
            color = 0xFF3E7A4E;
        } else if (rowSelected && level == this.menu.getSelectedLevel()) {
            texture = LEVEL_SELECTED;
            color = 0xFFC0FF80;
        } else if (rowSelected) {
            texture = affordable ? LEVEL_SELECTED_AVAILABLE : LEVEL_SELECTED_UNAVAILABLE;
            color = affordable ? 0xFFB890B8 : 0xFF685068;
        } else {
            texture = affordable ? LEVEL_AVAILABLE : LEVEL_UNAVAILABLE;
            color = affordable ? 0xFF7A6A5A : 0xFF504840;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE, LEVEL_BUTTON_SIZE);
        Component label = levelName(level);
        graphics.text(this.font, label, x + (LEVEL_BUTTON_SIZE - this.font.width(label)) / 2, y + 3, color, true);
    }

    private void extractScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!this.isScrollBarActive()) return;

        int x = this.scrollbarLeft();
        int y = this.scrollbarTop();
        graphics.blit(RenderPipelines.GUI_TEXTURED, SCROLLBAR_TRACK, x, y, 0, 0, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);

        int thumbHeight = Math.max(10, SCROLLBAR_HEIGHT * VISIBLE_ROWS / this.menu.getEntries().size());
        int thumbY = y + (int) ((SCROLLBAR_HEIGHT - thumbHeight) * this.scrollOffs);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SCROLLBAR_THUMB, x, thumbY, 0, 0, SCROLLBAR_WIDTH, thumbHeight, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);

        if (this.isOverScrollbar(mouseX, mouseY)) {
            graphics.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
        }
    }

    private void extractSlotBar(GuiGraphicsExtractor graphics) {
        ItemStack item = this.menu.getSlot(CatalogueMenu.ITEM_SLOT).getItem();
        if (item.isEmpty()) return;

        int max = SlotSystem.getMaxSlots(item);
        int used = SlotSystem.getUsedSlots(item);
        int pips = max + SlotSystem.getGrindstonePenalty(item);
        if (pips <= 0) return;

        CatalogueEntry selected = this.menu.getSelectedEntry();
        int pending = selected == null ? 0 : this.menu.slotCost(selected, this.menu.getSelectedLevel());

        String count = used + "/" + max;
        int left = this.leftPos + LIST_X;
        int right = left + LIST_WIDTH - this.font.width(count) - 4;
        int y = this.topPos + SLOT_BAR_Y;
        int gap = pips > 1 ? Mth.clamp((right - left - pips * 4) / (pips - 1), 1, 2) : 2;
        int pipWidth = Math.max(4, (right - left - gap * (pips - 1)) / pips);
        boolean blink = Util.getMillis() / 400 % 2 == 0;

        for (int i = 0; i < pips; i++) {
            Identifier pip = i < used ? SLOT_USED
                    : i < used + pending ? (blink ? SLOT_PENDING_ON : SLOT_PENDING_OFF)
                    : i < max ? SLOT_FREE
                    : SLOT_PENALTY;
            graphics.blit(RenderPipelines.GUI_TEXTURED, pip, left + i * (pipWidth + gap), y, 0, 0, pipWidth, PIP_HEIGHT, PIP_TEXTURE_WIDTH, PIP_HEIGHT);
        }
        graphics.text(this.font, count, right + 4, y - 1, 0xFFD8C8F0, true);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (!this.isHovering(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT, mouseX, mouseY)) return;

        int index = this.indexAt(mouseY);
        if (index < 0 || index >= this.menu.getEntries().size()) return;

        CatalogueEntry entry = this.menu.getEntries().get(index);
        int hoveredLevel = this.levelAt(entry, mouseX);
        int level = hoveredLevel > 0 ? Math.max(entry.currentLevel() + 1, hoveredLevel)
                : index == this.menu.getSelectedIndex() ? this.menu.getSelectedLevel()
                : entry.currentLevel() + 1;
        graphics.setComponentTooltipForNextFrame(this.font, this.costTooltip(entry, level), mouseX, mouseY);
    }

    private List<Component> costTooltip(CatalogueEntry entry, int level) {
        ItemStack item = this.menu.getSlot(CatalogueMenu.ITEM_SLOT).getItem();
        ItemStack reagent = this.menu.getSlot(CatalogueMenu.REAGENT_SLOT).getItem();
        ItemStack requiredReagent = new ItemStack(EnchantmentCosts.reagent(entry.key()));
        int reagentCost = this.menu.reagentCost(entry, level);
        int xpCost = this.menu.xpCost(entry, level);
        int slotCost = this.menu.slotCost(entry, level);

        MutableComponent name = entry.enchantment().value().description().copy().append(CommonComponents.SPACE);
        if (entry.currentLevel() > 0) name.append(levelName(entry.currentLevel())).append(" → ");
        name.append(levelName(level)).withStyle(entry.enchantment().is(EnchantmentTags.CURSE) ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(name);
        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(costLine(requiredReagent.getHoverName(), reagentCost, ItemStack.isSameItem(reagent, requiredReagent) && reagent.getCount() >= reagentCost));
        if (this.menu.isXpCostEnabled()) tooltip.add(costLine(XP_LEVELS, xpCost, this.minecraft.player.experienceLevel >= xpCost));
        tooltip.add(slotCost > 0
                ? costLine(SLOTS, slotCost, SlotSystem.getAvailableSlots(item) >= slotCost)
                : costLine(SLOTS, Component.literal("+1").withStyle(ChatFormatting.GREEN)));

        if (this.menu.getBookshelves() > 0) {
            long discount = Math.round(BookshelfScanner.reagentDiscount(this.menu.getBookshelves()) * 100);
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("screen.enchantment-overhaul.catalogue.bookshelves", this.menu.getBookshelves(), discount)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        return tooltip;
    }

    private static Component costLine(Component label, int amount, boolean affordable) {
        return costLine(label, Component.literal(String.valueOf(amount)).withStyle(affordable ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static Component costLine(Component label, Component amount) {
        return Component.translatable("screen.enchantment-overhaul.catalogue.cost", label).withStyle(ChatFormatting.GRAY).append(amount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.clickEntry(event.x(), event.y())) return true;
        if (this.isScrollBarActive() && this.isOverScrollbar(event.x(), event.y())) {
            this.scrolling = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean clickEntry(double mouseX, double mouseY) {
        int left = this.rowLeft();
        int top = this.rowTop(0);
        if (mouseX < left || mouseX >= left + this.rowWidth() || mouseY < top || mouseY >= this.topPos + LIST_Y + LIST_HEIGHT - 2) return false;

        int index = this.indexAt(mouseY);
        if (index < 0 || index >= this.menu.getEntries().size()) return false;

        CatalogueEntry entry = this.menu.getEntries().get(index);
        int hoveredLevel = this.levelAt(entry, mouseX);
        int buttonId = CatalogueMenu.buttonId(index, hoveredLevel > 0 ? hoveredLevel : entry.currentLevel() + 1);
        if (!this.menu.clickMenuButton(this.minecraft.player, buttonId)) return false;

        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!this.scrolling || !this.isScrollBarActive()) return super.mouseDragged(event, dx, dy);

        this.scrollOffs = Mth.clamp((float) (event.y() - this.scrollbarTop()) / SCROLLBAR_HEIGHT, 0.0F, 1.0F);
        this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows());
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (super.mouseScrolled(x, y, scrollX, scrollY)) return true;
        if (!this.isScrollBarActive()) return false;

        int offscreenRows = this.getOffscreenRows();
        this.startIndex = Mth.clamp(this.startIndex - (int) scrollY, 0, offscreenRows);
        this.scrollOffs = (float) this.startIndex / offscreenRows;
        return true;
    }

    private boolean isScrollBarActive() {
        return this.menu.getEntries().size() > VISIBLE_ROWS;
    }

    private int getOffscreenRows() {
        return Math.max(0, this.menu.getEntries().size() - VISIBLE_ROWS);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = this.scrollbarLeft();
        int y = this.scrollbarTop();
        return mouseX >= x && mouseX < x + SCROLLBAR_WIDTH && mouseY >= y && mouseY < y + SCROLLBAR_HEIGHT;
    }

    private int scrollbarLeft() {
        return this.leftPos + LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH - 2;
    }

    private int scrollbarTop() {
        return this.topPos + LIST_Y + 2;
    }

    private int rowLeft() {
        return this.leftPos + LIST_X + 2;
    }

    private int rowTop(int row) {
        return this.topPos + LIST_Y + 2 + row * ROW_SPACING;
    }

    private int rowWidth() {
        return this.isScrollBarActive() ? LIST_WIDTH - SCROLLBAR_WIDTH - 4 : LIST_WIDTH - 4;
    }

    private int indexAt(double mouseY) {
        return this.startIndex + (int) (mouseY - this.rowTop(0)) / ROW_SPACING;
    }

    private int levelButtonsLeft(CatalogueEntry entry) {
        return this.rowLeft() + this.rowWidth() - 2 - entry.maxLevel() * LEVEL_BUTTON_SPACING;
    }

    private int levelAt(CatalogueEntry entry, double mouseX) {
        int left = this.levelButtonsLeft(entry);
        if (mouseX < left) return 0;
        return Math.min((int) (mouseX - left) / LEVEL_BUTTON_SPACING + 1, entry.maxLevel());
    }

    private boolean isAnyLevelAffordable(CatalogueEntry entry) {
        for (int level = entry.currentLevel() + 1; level <= entry.maxLevel(); level++) {
            if (this.menu.canEnchant(entry, level)) return true;
        }
        return false;
    }

    private static Component levelName(int level) {
        return Component.translatable("enchantment.level." + level);
    }
}
