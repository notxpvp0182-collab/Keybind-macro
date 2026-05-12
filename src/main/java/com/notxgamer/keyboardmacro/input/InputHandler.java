package com.notxgamer.keyboardmacro.input;

import com.notxgamer.keyboardmacro.KeyboardMacroMod;
import com.notxgamer.keyboardmacro.action.ActionExecutor;
import com.notxgamer.keyboardmacro.config.MacroConfig;
import com.notxgamer.keyboardmacro.profile.MacroProfile;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private final MacroConfig config;
    private final ActionExecutor executor = new ActionExecutor();
    private final Set<Integer> heldKeys = new HashSet<>();

    // GUI open keybind (configurable via Minecraft controls)
    private KeyBinding openGuiKey;

    public InputHandler(MacroConfig config) {
        this.config = config;
    }

    public void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.keyboard_macro.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.keyboard_macro"
        ));
    }

    public void onTick(MinecraftClient client) {
        executor.tick(client);

        // Open GUI keybind
        if (openGuiKey != null && openGuiKey.wasPressed()) {
            client.setScreen(new com.notxgamer.keyboardmacro.gui.MacroMainScreen(null));
            return;
        }

        if (!config.isMasterEnabled()) return;
        if (client.currentScreen != null) return; // don't fire while any screen is open

        long window = client.getWindow().getHandle();

        for (MacroProfile profile : config.getProfiles()) {
            if (!profile.isEnabled()) continue;
            int key = profile.getTriggerKey();
            if (key == GLFW.GLFW_KEY_UNKNOWN) continue;

            boolean pressed;
            if (key < 0) {
                // mouse button: stored as negative
                int btn = -key - 1;
                pressed = GLFW.glfwGetMouseButton(window, btn) == GLFW.GLFW_PRESS;
            } else {
                pressed = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            }

            boolean wasHeld = heldKeys.contains(key);
            if (pressed && !wasHeld) {
                heldKeys.add(key);
                triggerProfile(profile);
            } else if (!pressed) {
                heldKeys.remove(key);
            }
        }
    }

    private void triggerProfile(MacroProfile profile) {
        if (profile.getActions().isEmpty()) return;
        KeyboardMacroMod.LOGGER.debug("[KeyboardMacro] Triggering profile: {}", profile.getName());
        executor.enqueue(profile.getActions());
    }

    public ActionExecutor getExecutor() { return executor; }
}
