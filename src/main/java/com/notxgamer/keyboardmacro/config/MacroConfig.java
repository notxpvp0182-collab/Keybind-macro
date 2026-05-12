package com.notxgamer.keyboardmacro.config;

import com.google.gson.*;
import com.notxgamer.keyboardmacro.KeyboardMacroMod;
import com.notxgamer.keyboardmacro.profile.MacroProfile;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class MacroConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configFile;
    private final Path backupFile;

    private boolean masterEnabled = true;
    private List<MacroProfile> profiles = new ArrayList<>();

    public MacroConfig() {
        Path cfgDir = FabricLoader.getInstance().getConfigDir().resolve("keyboard_macro");
        try { Files.createDirectories(cfgDir); } catch (IOException ignored) {}
        configFile = cfgDir.resolve("profiles.json");
        backupFile = cfgDir.resolve("profiles.backup.json");
    }

    // ── Load ───────────────────────────────────────────────────────
    public void load() {
        if (!Files.exists(configFile)) {
            loadDefaults();
            save();
            return;
        }
        try {
            String raw = Files.readString(configFile);
            parseJson(raw);
        } catch (Exception e) {
            KeyboardMacroMod.LOGGER.warn("[KeyboardMacro] Config corrupted, attempting backup restore...");
            tryRestoreBackup();
        }
    }

    private void parseJson(String raw) throws Exception {
        JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
        masterEnabled = root.has("masterEnabled") && root.get("masterEnabled").getAsBoolean();
        profiles.clear();
        if (root.has("profiles")) {
            for (var el : root.getAsJsonArray("profiles")) {
                try { profiles.add(MacroProfile.fromJson(el.getAsJsonObject())); }
                catch (Exception ex) {
                    KeyboardMacroMod.LOGGER.warn("[KeyboardMacro] Skipped malformed profile: {}", ex.getMessage());
                }
            }
        }
    }

    private void tryRestoreBackup() {
        if (!Files.exists(backupFile)) {
            KeyboardMacroMod.LOGGER.warn("[KeyboardMacro] No backup found. Loading defaults.");
            loadDefaults();
            return;
        }
        try {
            String raw = Files.readString(backupFile);
            parseJson(raw);
            KeyboardMacroMod.LOGGER.info("[KeyboardMacro] Backup restored successfully.");
        } catch (Exception ex) {
            KeyboardMacroMod.LOGGER.warn("[KeyboardMacro] Backup also corrupted. Loading defaults.");
            loadDefaults();
        }
    }

    private void loadDefaults() {
        profiles.clear();
        profiles.add(MacroProfile.waterClutchPreset());
        profiles.add(MacroProfile.fastPearlPreset());
    }

    // ── Save ───────────────────────────────────────────────────────
    public void save() {
        try {
            // rotate backup first
            if (Files.exists(configFile)) {
                Files.copy(configFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            }
            JsonObject root = new JsonObject();
            root.addProperty("masterEnabled", masterEnabled);
            JsonArray arr = new JsonArray();
            for (MacroProfile p : profiles) arr.add(p.toJson());
            root.add("profiles", arr);
            Files.writeString(configFile, GSON.toJson(root));
        } catch (IOException e) {
            KeyboardMacroMod.LOGGER.error("[KeyboardMacro] Failed to save config: {}", e.getMessage());
        }
    }

    // ── Getters / Setters ──────────────────────────────────────────
    public boolean isMasterEnabled()              { return masterEnabled; }
    public void setMasterEnabled(boolean v)       { this.masterEnabled = v; }
    public List<MacroProfile> getProfiles()       { return profiles; }

    public void enableAll()  { profiles.forEach(p -> p.setEnabled(true)); }
    public void disableAll() { profiles.forEach(p -> p.setEnabled(false)); }
}
