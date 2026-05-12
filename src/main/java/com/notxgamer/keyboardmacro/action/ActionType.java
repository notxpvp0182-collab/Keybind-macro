package com.notxgamer.keyboardmacro.action;

public enum ActionType {
    SELECT_HOTBAR_SLOT("Select Hotbar Slot"),
    RIGHT_CLICK("Right Click"),
    LEFT_CLICK("Left Click"),
    JUMP("Jump"),
    SNEAK("Sneak"),
    SPRINT("Sprint"),
    SWAP_HANDS("Swap Hands"),
    DROP_ITEM("Drop Item"),
    OPEN_INVENTORY("Open Inventory"),
    CHAT_COMMAND("Chat Command"),
    DELAY("Delay (ms)");

    private final String displayName;

    ActionType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
