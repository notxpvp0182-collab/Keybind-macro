package com.notxgamer.keyboardmacro.action;

import com.notxgamer.keyboardmacro.KeyboardMacroMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class ActionExecutor {

    private record PendingAction(MacroAction action, long executeAt) {}

    private final Queue<PendingAction> queue = new ArrayDeque<>();

    public void enqueue(List<MacroAction> actions) {
        queue.clear();
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

    public void tick(MinecraftClient client) {
        if (queue.isEmpty()) return;
        long now = System.currentTimeMillis();
        while (!queue.isEmpty() && queue.peek().executeAt() <= now) {
            execute(queue.poll().action(), client);
        }
    }

    private void execute(MacroAction action, MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null) return;

        try {
            switch (action.getType()) {
                case SELECT_HOTBAR_SLOT ->
                    player.getInventory().selectedSlot = action.getHotbarSlot();

                case RIGHT_CLICK ->
                    client.interactionManager.interactItem(
                        player, Hand.MAIN_HAND);

                case LEFT_CLICK -> {
                    if (client.crosshairTarget != null) {
                        client.interactionManager.attackBlock(
                            net.minecraft.util.math.BlockPos.ORIGIN,
                            net.minecraft.util.math.Direction.UP);
                    }
                }

                case JUMP -> player.jump();

                case SNEAK -> player.setSneaking(!player.isSneaking());

                case SPRINT -> player.setSprinting(true);

                case SWAP_HANDS ->
                    client.interactionManager.interactItem(
                        player, Hand.OFF_HAND);

                case DROP_ITEM ->
                    player.dropSelectedItem(false);

                case OPEN_INVENTORY ->
                    client.setScreen(
                        new net.minecraft.client.gui.screen.ingame.InventoryScreen(player));

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
            KeyboardMacroMod.LOGGER.warn("[KeyboardMacro] Action error: {}", e.getMessage());
        }
    }

    public boolean isIdle() { return queue.isEmpty(); }
    public void clear()     { queue.clear(); }
            }
