package com.notxgamer.keyboardmacro.action;

import com.google.gson.JsonObject;

public class MacroAction {

    private ActionType type;
    private int hotbarSlot;   // 0–8 for SELECT_HOTBAR_SLOT
    private int delayMs;      // for DELAY
    private String chatCommand; // for CHAT_COMMAND

    public MacroAction(ActionType type) {
        this.type = type;
        this.hotbarSlot = 0;
        this.delayMs = 50;
        this.chatCommand = "/say Hello";
    }

    // ── Serialization ──────────────────────────────────────────────
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type.name());
        obj.addProperty("hotbarSlot", hotbarSlot);
        obj.addProperty("delayMs", delayMs);
        obj.addProperty("chatCommand", chatCommand);
        return obj;
    }

    public static MacroAction fromJson(JsonObject obj) {
        ActionType type = ActionType.valueOf(obj.get("type").getAsString());
        MacroAction action = new MacroAction(type);
        if (obj.has("hotbarSlot"))   action.hotbarSlot   = obj.get("hotbarSlot").getAsInt();
        if (obj.has("delayMs"))      action.delayMs      = obj.get("delayMs").getAsInt();
        if (obj.has("chatCommand"))  action.chatCommand  = obj.get("chatCommand").getAsString();
        return action;
    }

    // ── Getters / Setters ──────────────────────────────────────────
    public ActionType getType()               { return type; }
    public void setType(ActionType type)      { this.type = type; }
    public int getHotbarSlot()                { return hotbarSlot; }
    public void setHotbarSlot(int slot)       { this.hotbarSlot = Math.max(0, Math.min(8, slot)); }
    public int getDelayMs()                   { return delayMs; }
    public void setDelayMs(int ms)            { this.delayMs = Math.max(0, ms); }
    public String getChatCommand()            { return chatCommand; }
    public void setChatCommand(String cmd)    { this.chatCommand = cmd; }

    public String getSummary() {
        return switch (type) {
            case SELECT_HOTBAR_SLOT -> "Slot " + (hotbarSlot + 1);
            case DELAY              -> "Wait " + delayMs + "ms";
            case CHAT_COMMAND       -> "CMD: " + chatCommand;
            default                 -> type.getDisplayName();
        };
    }
}
