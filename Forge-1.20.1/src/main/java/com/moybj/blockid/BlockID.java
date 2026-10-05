package com.moybj.blockid;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(BlockID.MOD_ID)
public class BlockID {
    public static final String MOD_ID = "block_id";
    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
            "key.block_id.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.block_id"
    );
    // 复制键默认 H：C 为整合包高频冲突键，改用 H 降低冲突概率
    public static final KeyMapping COPY_ID_KEY = new KeyMapping(
            "key.block_id.copy_id",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.block_id"
    );

    public BlockID() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeys);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ConfigManager.init();
            FavoritesManager.init();
            HistoryManager.init();
        });
    }

    private void registerKeys(final RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI_KEY);
        event.register(COPY_ID_KEY);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (OPEN_GUI_KEY.consumeClick()) {
                mc.setScreen(new BlockIdScreen());
            }
            if (COPY_ID_KEY.consumeClick() && mc.player != null && mc.level != null) {
                CopyIdHandler.copyBlockId();
            }
        }
    }
}