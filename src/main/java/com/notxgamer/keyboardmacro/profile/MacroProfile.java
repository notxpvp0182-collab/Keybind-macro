package com.notxgamer.keyboardmacro.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.notxgamer.keyboardmacro.action.MacroAction;
import com.notxgamer.keyboardmacro.action.ActionType;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MacroProfile {

    private String id;
    private String name;
    private int triggerKey;      // GLFW key code, or negative for mouse buttons
    private boolean enabled;
    private List<MacroAction> actions;

    public MacroProfile(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.triggerKey = GLFW.GLFW_KEY_UNKNOWN;
        this.enabled = true;
        this.actions = new ArrayList<>();
    }

    // ── Presets ────────────────────────────────────────────────────
    public static MacroProfile waterClutchPreset() {
        MacroProfile p = new MacroProfile("Water Clutch");
        p.setTriggerKey(GLFW.GLFW_KEY_R);
        MacroAction slot = new MacroAction(ActionType.SELECT_HOTBAR_SLOT);
        slot.setHotbarSlot(3); // slot 4
        p.getActions().add(slot);
        MacroAction rc = new MacroAction(ActionType.RIGHT_CLICK);
        p.getActions().add(rc);
        return p;
    }

    public static MacroProfile fastPearlPreset() {
        MacroProfile p = new MacroProfile("Fast Pearl");
        p.setTriggerKey(GLFW.GLFW_KEY_G);
        MacroAction slot = new MacroAction(ActionType.SELECT_HOTBAR_SLOT);
        slot.setHotbarSlot(4);
        p.getActions().add(slot);
        MacroAction rc = new MacroAction(ActionType.RIGHT_CLICK);
        p.getActions().add(rc);
        return p;
    }

    // ── Serialization ──────────────────────────────────────────────
    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("triggerKey", triggerKey);
        obj.addProperty("enabled", enabled);
        JsonArray arr = new JsonArray();
        for (MacroAction a : actions) arr.add(a.toJson());
        obj.add("actions", arr);
        return obj;
    }

    public static MacroProfile fromJson(JsonObject obj) {
        MacroProfile p = new MacroProfile(obj.get("name").getAsString());
        if (obj.has("id"))         p.id = obj.get("id").getAsString();
        if (obj.has("triggerKey")) p.triggerKey = obj.get("triggerKey").getAsInt();
        if (obj.has("enabled"))    p.enabled = obj.get("enabled").getAsBoolean();
        if (obj.has("actions")) {
            for (var el : obj.getAsJsonArray("actions")) {
                try { p.actions.add(MacroAction.fromJson(el.getAsJsonObject())); }
                catch (Exception ignored) {}
            }
        }
        return p;
    }

    // ── Clone ──────────────────────────────────────────────────────
    public MacroProfile duplicate() {
        MacroProfile copy = MacroProfile.fromJson(toJson());
        copy.id = UUID.randomUUID().toString();
        copy.name = name + " (Copy)";
        return copy;
    }

    // ── Key display ────────────────────────────────────────────────
    public String getTriggerKeyName() {
        if (triggerKey == GLFW.GLFW_KEY_UNKNOWN) return "None";
        if (triggerKey < 0) return "Mouse " + (-triggerKey);
        String name = GLFW.glfwGetKeyName(triggerKey, 0);
        if (name != null) return name.toUpperCase();
        return "Key " + triggerKey;
    }

    // ── Getters / Setters ──────────────────────────────────────────
    public String getId()                         { return id; }
    public String getName()                       { return name; }
    public void setName(String name)              { this.name = name; }
    public int getTriggerKey()                    { return triggerKey; }
    public void setTriggerKey(int key)            { this.triggerKey = key; }
    public boolean isEnabled()                    { return enabled; }
    public void setEnabled(boolean enabled)       { this.enabled = enabled; }
    public List<MacroAction> getActions()         { return actions; }
}
