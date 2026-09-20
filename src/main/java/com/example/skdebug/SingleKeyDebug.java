package com.example.skdebug;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SingleKeyDebug implements ClientModInitializer {

    public static final String CATEGORY = "key.categories.skdebug";

    private static KeyBinding toggleDebug;
    private static KeyBinding toggleHitboxes;
    private static KeyBinding toggleChunkBorders;

    @Override
    public void onInitializeClient() {
        // These show up in Options -> Controls and can be bound to any key,
        // mouse button, or on-screen Amethyst button you like.
        toggleDebug = register("key.skdebug.debug", GLFW.GLFW_KEY_F6);
        toggleHitboxes = register("key.skdebug.hitboxes", GLFW.GLFW_KEY_F7);
        toggleChunkBorders = register("key.skdebug.chunkborders", GLFW.GLFW_KEY_F8);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private KeyBinding register(String translationKey, int defaultKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey,
                InputUtil.Type.KEYSYM,
                defaultKey,
                CATEGORY
        ));
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        // wasPressed() drains one queued press per call, so holding a key
        // or a touch button won't rapid-toggle.
        while (toggleDebug.wasPressed()) {
            client.options.debugEnabled = !client.options.debugEnabled;
            // Vanilla F3 also clears these sub-overlays on close.
            client.options.debugProfilerEnabled = false;
            client.options.debugTpsEnabled = false;
        }

        while (toggleHitboxes.wasPressed()) {
            boolean on = !client.getEntityRenderManager().shouldRenderHitboxes();
            client.getEntityRenderManager().setRenderHitboxes(on);
            overlay(client, "Hitboxes: " + (on ? "ON" : "OFF"));
        }

        while (toggleChunkBorders.wasPressed()) {
            boolean on = client.debugRenderer.toggleShowChunkBorder();
            overlay(client, "Chunk borders: " + (on ? "ON" : "OFF"));
        }
    }

    private void overlay(MinecraftClient client, String message) {
        if (client.inGameHud != null) {
            client.inGameHud.setOverlayMessage(new LiteralText(message), false);
        }
    }
}
