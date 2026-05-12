package com.notxgamer.keyboardmacro.action;

import com.notxgamer.keyboardmacro.KeyboardMacroMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Zero-delay execution engine.
 * Actions are queued and flushed each client tick.
 * Delay actions schedule remaining actions after elapsed ms.
 */
public class ActionExecutor {

    private record PendingAction(MacroAction action, long executeAt) {}

    private final Queue<PendingAction> queue = new ArrayDeque<>();

    /** Enqueue a full action chain for execution. */
    public void enqueue(List<MacroAction> actions) {
        queue.clear(); // cancel previous chain
        long now = System.currentTimeMillis();
        long cursor = now;
        for (MacroAction a : actions) {
            if (a.getType() == ActionType.DELAY) {
                cursor += a.getDelayMs();
            } else {
                queue.add(new PendingAction(a, cursor));
            }
        }
    }

    /** Called every client tick. Executes all due actions. */
    public void tick(MinecraftClient client) {
        if (queue.isEmpty()) return;
        long now = System.currentTimeMillis();
        while (!queue.isEmpty() && queue.peek().executeAt() <= now) {
            PendingAction pending = queue.poll();
            execute(pending.action(), client);
        }
    }

    private void execute(MacroAction action, MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        try {
            switch (action.getType()) {
                case SELECT_HOTBAR_SLOT -> player.getInventory().selectedSlot = action.getHotbarSlot();
                case RIGHT_CLICK -> {
                    if (client.interactionManager != null && client.crosshairTarget != null) {
                        client.options.useKey.setPressed(true);
                        client.doItemUse();
                        client.options.useKey.setPressed(false);
                    }
                }
                case LEFT_CLICK -> {
                    if (client.interactionManager != null) {
                        client.options.attackKey.setPressed(true);
                        client.doAttack();
                        client.options.attackKey.setPressed(false);
                    }
                }
                case JUMP -> player.jump();
                case SNEAK -> player.setSneaking(!player.isSneaking());
                case SPRINT -> player.setSprinting(true);
                case SWAP_HANDS -> client.interactionManager.swapHands();
                case DROP_ITEM -> player.dropSelectedItem(false);
                case OPEN_INVENTORY -> client.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(player));
                case CHAT_COMMAND -> {
                    String cmd = action.getChatCommand();
                    if (cmd != null && !cmd.isBlank()) {
                        if (cmd.startsWith("/")) {
                            player.networkHandler.sendChatCommand(cmd.substring(1));
                        } else {
                            player.networkHandler.sendChatMessage(cmd);
                        }
                    }
                }
                default -> {}
            }
        } catch (Exception e) {
            KeyboardMacroMod.LOGGER.warn("[KeyboardMacro] Action execute error: {}", e.getMessage());
        }
    }

    public boolean isIdle() { return queue.isEmpty(); }
    public void clear()     { queue.clear(); }
}
