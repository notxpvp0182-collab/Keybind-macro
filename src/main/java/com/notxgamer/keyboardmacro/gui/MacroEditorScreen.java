package com.notxgamer.keyboardmacro.gui;

import com.notxgamer.keyboardmacro.KeyboardMacroMod;
import com.notxgamer.keyboardmacro.action.ActionType;
import com.notxgamer.keyboardmacro.action.MacroAction;
import com.notxgamer.keyboardmacro.config.MacroConfig;
import com.notxgamer.keyboardmacro.profile.MacroProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MacroEditorScreen extends Screen {

    private static final int COL_BG     = 0xFF0A0A14;
    private static final int COL_PANEL  = 0xFF12121E;
    private static final int COL_CARD   = 0xFF1A1A2E;
    private static final int COL_CYAN   = 0xFF00E5FF;
    private static final int COL_PURPLE = 0xFFAA44FF;
    private static final int COL_GREEN  = 0xFF44FF88;
    private static final int COL_RED    = 0xFFFF4455;
    private static final int COL_GRAY   = 0xFF666688;
    private static final int COL_WHITE  = 0xFFFFFFFF;

    private static final int ACTION_H   = 30;
    private static final int ACTION_GAP = 3;
    private static final int LIST_TOP   = 90;

    private final Screen parent;
    private final MacroProfile profile;
    private final MacroConfig config;

    private TextFieldWidget nameField;
    private boolean capturingKey = false;
    private int scrollOffset = 0;

    private final List<ButtonWidget> actionButtons = new ArrayList<>();

    // Action type selector state
    private int selectedActionTypeIdx = 0;
    private final ActionType[] actionTypes = ActionType.values();

    public MacroEditorScreen(Screen parent, MacroProfile profile) {
        super(Text.literal("Edit: " + profile.getName()));
        this.parent = parent;
        this.profile = profile;
        this.config = KeyboardMacroMod.getInstance().getConfig();
    }

    @Override
    protected void init() {
        clearChildren();
        actionButtons.clear();

        int w = this.width;

        // Name field
        nameField = new TextFieldWidget(textRenderer, 60, 34, w - 130, 16, Text.literal("Name"));
        nameField.setText(profile.getName());
        nameField.setMaxLength(40);
        nameField.setChangedListener(s -> profile.setName(s.isBlank() ? "Macro" : s));
        addSelectableChild(nameField);

        // Key capture button
        addDrawableChild(ButtonWidget.builder(
                capturingKey ? Text.literal("Press any key...").formatted(Formatting.YELLOW)
                        : Text.literal("[" + profile.getTriggerKeyName() + "]").formatted(Formatting.AQUA),
                btn -> { capturingKey = true; btn.setMessage(Text.literal("Press any key...").formatted(Formatting.YELLOW)); }
        ).dimensions(w - 66, 34, 60, 16).build());

        // Add action — type cycle + add button
        addDrawableChild(ButtonWidget.builder(Text.literal("◀"), btn -> {
            selectedActionTypeIdx = (selectedActionTypeIdx - 1 + actionTypes.length) % actionTypes.length;
            rebuildActions();
        }).dimensions(8, LIST_TOP - 24, 16, 16).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("▶"), btn -> {
            selectedActionTypeIdx = (selectedActionTypeIdx + 1) % actionTypes.length;
            rebuildActions();
        }).dimensions(26, LIST_TOP - 24, 16, 16).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("+ Add " + actionTypes[selectedActionTypeIdx].getDisplayName()), btn -> {
            profile.getActions().add(new MacroAction(actionTypes[selectedActionTypeIdx]));
            config.save();
            rebuildActions();
        }).dimensions(46, LIST_TOP - 24, w - 100, 16).build());

        // Save / Back
        addDrawableChild(ButtonWidget.builder(Text.literal("✔ Save & Back").formatted(Formatting.GREEN), btn -> {
            config.save();
            client.setScreen(parent);
        }).dimensions(w / 2 - 80, height - 22, 160, 18).build());

        rebuildActions();
    }

    private void rebuildActions() {
        for (ButtonWidget b : actionButtons) remove(b);
        actionButtons.clear();

        List<MacroAction> actions = profile.getActions();
        int w = this.width;
        int listBottom = height - 28;

        for (int i = 0; i < actions.size(); i++) {
            MacroAction action = actions.get(i);
            int ay = LIST_TOP + i * (ACTION_H + ACTION_GAP) - scrollOffset;
            if (ay + ACTION_H < LIST_TOP || ay > listBottom) continue;

            int btnX = w - 8 - 130;
            final int idx = i;

            // Up
            if (i > 0) {
                ButtonWidget up = ButtonWidget.builder(Text.literal("▲"), btn -> {
                    MacroAction tmp = actions.get(idx - 1);
                    actions.set(idx - 1, actions.get(idx));
                    actions.set(idx, tmp);
                    config.save(); rebuildActions();
                }).dimensions(btnX, ay + 7, 16, 16).build();
                addDrawableChild(up); actionButtons.add(up);
            }
            // Down
            if (i < actions.size() - 1) {
                ButtonWidget down = ButtonWidget.builder(Text.literal("▼"), btn -> {
                    MacroAction tmp = actions.get(idx + 1);
                    actions.set(idx + 1, actions.get(idx));
                    actions.set(idx, tmp);
                    config.save(); rebuildActions();
                }).dimensions(btnX + 18, ay + 7, 16, 16).build();
                addDrawableChild(down); actionButtons.add(down);
            }

            // Slot adjust (for SELECT_HOTBAR_SLOT)
            if (action.getType() == ActionType.SELECT_HOTBAR_SLOT) {
                ButtonWidget minus = ButtonWidget.builder(Text.literal("-"), btn -> {
                    action.setHotbarSlot(action.getHotbarSlot() - 1);
                    config.save(); rebuildActions();
                }).dimensions(btnX + 38, ay + 7, 14, 16).build();

                ButtonWidget plus = ButtonWidget.builder(Text.literal("+"), btn -> {
                    action.setHotbarSlot(action.getHotbarSlot() + 1);
                    config.save(); rebuildActions();
                }).dimensions(btnX + 54, ay + 7, 14, 16).build();

                addDrawableChild(minus); actionButtons.add(minus);
                addDrawableChild(plus);  actionButtons.add(plus);
            }

            // Delay adjust
            if (action.getType() == ActionType.DELAY) {
                ButtonWidget minus = ButtonWidget.builder(Text.literal("-"), btn -> {
                    action.setDelayMs(Math.max(0, action.getDelayMs() - 10));
                    config.save(); rebuildActions();
                }).dimensions(btnX + 38, ay + 7, 14, 16).build();

                ButtonWidget plus = ButtonWidget.builder(Text.literal("+"), btn -> {
                    action.setDelayMs(action.getDelayMs() + 10);
                    config.save(); rebuildActions();
                }).dimensions(btnX + 54, ay + 7, 14, 16).build();

                addDrawableChild(minus); actionButtons.add(minus);
                addDrawableChild(plus);  actionButtons.add(plus);
            }

            // Delete
            ButtonWidget del = ButtonWidget.builder(Text.literal("✗").formatted(Formatting.RED), btn -> {
                actions.remove(idx);
                config.save(); rebuildActions();
            }).dimensions(btnX + 110, ay + 7, 16, 16).build();
            addDrawableChild(del); actionButtons.add(del);
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, COL_BG);

        // Header
        ctx.fill(0, 0, width, 30, COL_PANEL);
        ctx.fill(0, 30, width, 31, COL_CYAN);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Edit Profile").formatted(Formatting.AQUA), 8, 10, COL_WHITE);

        // Name label
        ctx.drawTextWithShadow(textRenderer, Text.literal("Name:").formatted(Formatting.GRAY), 8, 39, COL_GRAY);

        // Key label
        ctx.drawTextWithShadow(textRenderer, Text.literal("Trigger Key:").formatted(Formatting.GRAY),
                width - 130, 22, COL_GRAY);

        // Action type selector label
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Type: " + actionTypes[selectedActionTypeIdx].getDisplayName()).formatted(Formatting.AQUA),
                46, LIST_TOP - 12, COL_CYAN);

        // Separator
        ctx.fill(0, LIST_TOP - 2, width, LIST_TOP - 1, COL_PURPLE);

        // Actions
        int listBottom = height - 28;
        for (int i = 0; i < profile.getActions().size(); i++) {
            MacroAction action = profile.getActions().get(i);
            int ay = LIST_TOP + i * (ACTION_H + ACTION_GAP) - scrollOffset;
            if (ay + ACTION_H < LIST_TOP || ay > listBottom) continue;

            ctx.fill(8, ay, width - 8, ay + ACTION_H, COL_CARD);
            ctx.fill(8, ay, 9, ay + ACTION_H, COL_CYAN);

            ctx.drawTextWithShadow(textRenderer,
                    Text.literal((i + 1) + ". " + action.getType().getDisplayName()).formatted(Formatting.WHITE),
                    14, ay + 4, COL_WHITE);
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(action.getSummary()).formatted(Formatting.GRAY),
                    14, ay + 16, COL_GRAY);
        }

        if (profile.getActions().isEmpty()) {
            String empty = "No actions. Add one above.";
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(empty).formatted(Formatting.DARK_GRAY),
                    width / 2 - textRenderer.getWidth(empty) / 2,
                    LIST_TOP + 20, COL_GRAY);
        }

        // Clip
        ctx.fill(0, 0, width, LIST_TOP, COL_BG);
        ctx.fill(0, height - 28, width, height, COL_BG);

        // Footer
        ctx.fill(0, height - 28, width, height - 27, COL_CYAN);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Keyboard Macro • Created by NoTXGameR").formatted(Formatting.DARK_GRAY),
                8, height - 18, COL_GRAY);

        if (capturingKey) {
            // dim overlay
            ctx.fill(0, 0, width, height, 0xBB000000);
            String msg = "Press any key or mouse button...";
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(msg).formatted(Formatting.YELLOW),
                    width / 2 - textRenderer.getWidth(msg) / 2,
                    height / 2 - 5, COL_WHITE);
        }

        super.render(ctx, mx, my, delta);
        if (nameField != null) nameField.render(ctx, mx, my, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (capturingKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                capturingKey = false;
                init();
                return true;
            }
            profile.setTriggerKey(keyCode);
            capturingKey = false;
            config.save();
            init();
            return true;
        }
        if (nameField != null && nameField.isFocused()) {
            return nameField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (capturingKey) {
            // negative codes for mouse: -1 = left, -2 = right, -3 = middle...
            profile.setTriggerKey(-(button + 1));
            capturingKey = false;
            config.save();
            init();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (nameField != null && nameField.isFocused()) return nameField.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        int totalH = profile.getActions().size() * (ACTION_H + ACTION_GAP);
        int visibleH = (height - 28) - LIST_TOP;
        int maxScroll = Math.max(0, totalH - visibleH);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - vAmount * 12));
        rebuildActions();
        return true;
    }

    @Override
    public void close() {
        config.save();
        client.setScreen(parent);
    }
}
