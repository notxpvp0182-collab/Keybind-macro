package com.notxgamer.keyboardmacro;

import com.notxgamer.keyboardmacro.config.MacroConfig;
import com.notxgamer.keyboardmacro.input.InputHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyboardMacroMod implements ClientModInitializer {

    public static final String MOD_ID = "keyboard_macro";
    public static final String MOD_NAME = "Keyboard Macro";
    public static final String CREATOR = "NoTXGameR";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyboardMacroMod instance;
    private MacroConfig config;
    private InputHandler inputHandler;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] Initializing {} — Created by {}", MOD_NAME, MOD_NAME, CREATOR);

        config = new MacroConfig();
        config.load();

        inputHandler = new InputHandler(config);
        inputHandler.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                inputHandler.onTick(client);
            }
        });

        LOGGER.info("[{}] Ready. {} profiles loaded. Created by {}", MOD_NAME, config.getProfiles().size(), CREATOR);
    }

    public static KeyboardMacroMod getInstance() { return instance; }
    public MacroConfig getConfig() { return config; }
    public InputHandler getInputHandler() { return inputHandler; }
}
