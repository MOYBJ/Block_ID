package com.moybj.blockid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BlockID implements ClientModInitializer {
    public static final String MOD_ID = "block_id";

    
    public static final KeyBinding OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.block_id.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.block_id"
    ));

    
    public static final KeyBinding COPY_ID_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.block_id.copy_id",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.block_id"
    ));

    @Override
    public void onInitializeClient() {
        
        ConfigManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            
            while (OPEN_GUI_KEY.wasPressed()) {
                client.setScreen(new BlockIdScreen());
            }
            
            while (COPY_ID_KEY.wasPressed()) {
                CopyIdHandler.copyBlockId();
            }
        });
    }
}
