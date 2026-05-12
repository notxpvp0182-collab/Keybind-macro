package com.notxgamer.keyboardmacro.gui;

import com.notxgamer.keyboardmacro.KeyboardMacroMod;
import com.notxgamer.keyboardmacro.config.MacroConfig;
import com.notxgamer.keyboardmacro.profile.MacroProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class MacroMainScreen extends Screen {

    private static final int CARD_H      = 44;
    private static final int CARD_GAP    = 4;
    private static final int LIST_TOP    = 72;
    private static final int SIDE_PAD    = 12;

    // colors
    private static final int COL_BG      = 0xFF0A0A14;
    private static final int COL_PANEL   = 0xFF12121E;
    private static final int COL_CARD    = 0xFF1A1A2E;
    private static final int COL_CARD_HL = 0xFF1E1E3A;
    private static final int COL_CYAN    = 0xFF00E5FF;
    private static final int COL_PURPLE  = 0xFFAA44FF;
    private static final int COL_GREEN   = 0xFF44FF88;
    private static final int COL_RED     = 0xFFFF4455;
    private static final int COL_GRAY    = 0xFF666688;
    private static final int COL_WHITE   = 0xFFFFFFFF;

    private final Screen parent;
    private final MacroConfig config;

    private TextFieldWidget searchField;
    private int scrollOffset = 0;
    private List<MacroProfile> filteredProfiles = new ArrayList<>();

    // per-card button references rebuilt on layout
    private final List<ButtonWidget> cardButtons = new ArrayList<>();

    public MacroMainScreen(Screen parent) {
        super(Text.literal("Keyboard Macro"));
        this.parent = parent;
        this.config = KeyboardMacroMod.getInstance().getConfig();
    }

    @Override
    protected void init() {
        cardButtons.clear();
        clearChildren();

        int w = this.width;

        // ── Search bar ────────────────────────────────────────────
        searchField = new TextFieldWidget(textRenderer, SIDE_PAD + 2, 46, w - SIDE_PAD * 2 - 4, 18, Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search profiles...").formatted(Formatting.DARK_GRAY));
        searchField.setChangedListener(s -> { scrollOffset = 0; rebuildList(); });
        addSelectableChild(searchField);

        // ── Top buttons ───────────────────────────────────────────
        // Master toggle
        addDrawableChild(ButtonWidget.builder(masterToggleText(), btn -> {
            config.setMasterEnabled(!config.isMasterEnabled());
            btn.setMessage(masterToggleText());
            config.save();
        }).dimensions(w - 160, 8, 72, 18).build());

        // Add profile
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Add"), btn -> {
            MacroProfile newP = new MacroProfile("New Macro");
            config.getProfiles().add(newP);
            config.save();
            rebuildList();
            openEditor(newP);
        }).dimensions(w - 82, 8, 70, 18).build());

        // Enable All / Disable All
        addDrawableChild(ButtonWidget.builder(Text.literal("Enable All"), btn -> {
            config.enableAll(); config.save(); rebuildList();
        }).dimensions(SIDE_PAD, 8, 72, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Disable All"), btn -> {
            config.disableAll(); config.save(); rebuildList();
        }).dimensions(SIDE_PAD + 76, 8, 76, 18).build());

        // Done
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
                .dimensions(w / 2 - 50, height - 22, 100, 18).build());

        rebuildList();
    }

    private void rebuildList() {
        // remove old card buttons
        for (ButtonWidget b : cardButtons) remove(b);
        cardButtons.clear();

        String query = searchField != null ? searchField.getText().toLowerCase() : "";
        filteredProfiles = config.getProfiles().stream()
                .filter(p -> query.isEmpty() || p.getName().toLowerCase().contains(query))
                .toList();

        int w = this.width;
        int listBottom = height - 28;
        int visibleH = listBottom - LIST_TOP;

        for (int i = 0; i < filteredProfiles.size(); i++) {
            MacroProfile profile = filteredProfiles.get(i);
            int cardY = LIST_TOP + i * (CARD_H + CARD_GAP) - scrollOffset;
            if (cardY + CARD_H < LIST_TOP || cardY > listBottom) continue;

            int btnX = w - SIDE_PAD - 150;

            // Toggle ON/OFF
            ButtonWidget toggleBtn = ButtonWidget.builder(
                    profile.isEnabled() ? Text.literal("ON").formatted(Formatting.GREEN)
                            : Text.literal("OFF").formatted(Formatting.RED),
                    btn -> {
                        profile.setEnabled(!profile.isEnabled());
                        btn.setMessage(profile.isEnabled()
                                ? Text.literal("ON").formatted(Formatting.GREEN)
                                : Text.literal("OFF").formatted(Formatting.RED));
                        config.save();
                    }
            ).dimensions(btnX, cardY + 4, 32, 16).build();

            // Edit
            ButtonWidget editBtn = ButtonWidget.builder(Text.literal("Edit"), btn -> openEditor(profile))
                    .dimensions(btnX + 36, cardY + 4, 36, 16).build();

            // Duplicate
            ButtonWidget dupBtn = ButtonWidget.builder(Text.literal("Dup"), btn -> {
                config.getProfiles().add(profile.duplicate());
                config.save();
                rebuildList();
            }).dimensions(btnX + 76, cardY + 4, 30, 16).build();

            // Delete
            ButtonWidget delBtn = ButtonWidget.builder(Text.literal("✗").formatted(Formatting.RED), btn -> {
                config.getProfiles().remove(profile);
                config.save();
                rebuildList();
            }).dimensions(btnX + 110, cardY + 4, 18, 16).build();

            addDrawableChild(toggleBtn);
            addDrawableChild(editBtn);
            addDrawableChild(dupBtn);
            addDrawableChild(delBtn);
            cardButtons.add(toggleBtn);
            cardButtons.add(editBtn);
            cardButtons.add(dupBtn);
            cardButtons.add(delBtn);
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // Background
        ctx.fill(0, 0, width, height, COL_BG);

        // Header panel
        ctx.fill(0, 0, width, 30, COL_PANEL);
        drawHLine(ctx, 0, width, 30, COL_CYAN);

        // Title
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("⌨ Keyboard Macro").formatted(Formatting.AQUA),
                SIDE_PAD, 10, COL_WHITE);

        // Creator tag
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("by NoTXGameR").formatted(Formatting.DARK_PURPLE),
                SIDE_PAD, 20, COL_PURPLE);

        // Search background
        ctx.fill(SIDE_PAD, 42, width - SIDE_PAD, 68, COL_PANEL);
        drawHLine(ctx, SIDE_PAD, width - SIDE_PAD, 68, COL_PURPLE);

        // List area background
        ctx.fill(0, LIST_TOP, width, height - 28, COL_BG);

        // Profile cards
        int listBottom = height - 28;
        for (int i = 0; i < filteredProfiles.size(); i++) {
            MacroProfile profile = filteredProfiles.get(i);
            int cardY = LIST_TOP + i * (CARD_H + CARD_GAP) - scrollOffset;
            if (cardY + CARD_H < LIST_TOP || cardY > listBottom) continue;

            // Card background
            boolean hover = mx >= SIDE_PAD && mx <= width - SIDE_PAD && my >= cardY && my <= cardY + CARD_H;
            ctx.fill(SIDE_PAD, cardY, width - SIDE_PAD, cardY + CARD_H, hover ? COL_CARD_HL : COL_CARD);
            drawHLine(ctx, SIDE_PAD, width - SIDE_PAD, cardY, profile.isEnabled() ? COL_CYAN : COL_GRAY);

            // Profile name
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(profile.getName()).formatted(Formatting.WHITE),
                    SIDE_PAD + 6, cardY + 6, COL_WHITE);

            // Trigger key
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("Key: [" + profile.getTriggerKeyName() + "]").formatted(Formatting.AQUA),
                    SIDE_PAD + 6, cardY + 18, COL_CYAN);

            // Action summary
            String summary = buildActionSummary(profile);
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(summary).formatted(Formatting.GRAY),
                    SIDE_PAD + 6, cardY + 30, COL_GRAY);
        }

        // Clip overlay (top/bottom of list)
        ctx.fill(0, 0, width, LIST_TOP, COL_BG);
        ctx.fill(0, height - 28, width, height, COL_BG);

        // Scrollbar
        drawScrollbar(ctx, listBottom);

        // Footer
        drawHLine(ctx, 0, width, height - 28, COL_CYAN);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Keyboard Macro • Created by NoTXGameR").formatted(Formatting.DARK_GRAY),
                SIDE_PAD, height - 18, COL_GRAY);

        // Profile count
        String countStr = filteredProfiles.size() + " profile" + (filteredProfiles.size() != 1 ? "s" : "");
        ctx.drawTextWithShadow(textRenderer,
                Text.literal(countStr).formatted(Formatting.GRAY),
                width - SIDE_PAD - textRenderer.getWidth(countStr), height - 18, COL_GRAY);

        super.render(ctx, mx, my, delta);
        if (searchField != null) searchField.render(ctx, mx, my, delta);
    }

    private void drawScrollbar(DrawContext ctx, int listBottom) {
        int totalH = filteredProfiles.size() * (CARD_H + CARD_GAP);
        int visibleH = listBottom - LIST_TOP;
        if (totalH <= visibleH) return;

        int sbX = width - 5;
        float ratio = (float) visibleH / totalH;
        int sbH = Math.max(20, (int)(visibleH * ratio));
        int sbY = LIST_TOP + (int)((visibleH - sbH) * ((float) scrollOffset / (totalH - visibleH)));
        ctx.fill(sbX, LIST_TOP, sbX + 4, listBottom, COL_PANEL);
        ctx.fill(sbX, sbY, sbX + 4, sbY + sbH, COL_PURPLE);
    }

    private void drawHLine(DrawContext ctx, int x1, int x2, int y, int color) {
        ctx.fill(x1, y, x2, y + 1, color);
    }

    private String buildActionSummary(MacroProfile profile) {
        if (profile.getActions().isEmpty()) return "No actions";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(4, profile.getActions().size()); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(profile.getActions().get(i).getSummary());
        }
        if (profile.getActions().size() > 4) sb.append(" ...");
        return sb.toString();
    }

    private Text masterToggleText() {
        return config.isMasterEnabled()
                ? Text.literal("Master: ON").formatted(Formatting.GREEN)
                : Text.literal("Master: OFF").formatted(Formatting.RED);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        int totalH = filteredProfiles.size() * (CARD_H + CARD_GAP);
        int visibleH = (height - 28) - LIST_TOP;
        int maxScroll = Math.max(0, totalH - visibleH);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - vAmount * 16));
        rebuildList();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField != null && searchField.isFocused()) {
            return searchField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchField != null && searchField.isFocused()) {
            return searchField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    private void openEditor(MacroProfile profile) {
        client.setScreen(new MacroEditorScreen(this, profile));
    }

    @Override
    public void close() {
        config.save();
        client.setScreen(parent);
    }
}
