package com.moybj.blockid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class BlockID implements ClientModInitializer {
    public static final String MOD_ID = "block_id";

    
    public static final KeyMapping OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.block_id.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.block_id"
    ));

    // 复制键默认 H：C 为整合包高频冲突键，改用 H 降低冲突概率
    public static final KeyMapping COPY_ID_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.block_id.copy_id",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.block_id"
    ));

    @Override
    public void onInitializeClient() {
        
        ConfigManager.init();
        FavoritesManager.init();
        HistoryManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            
            while (OPEN_GUI_KEY.consumeClick()) {
                client.setScreen(new BlockIdScreen());
            }
            
            while (COPY_ID_KEY.consumeClick()) {
                CopyIdHandler.copyBlockId();
            }
        });
    }
}
