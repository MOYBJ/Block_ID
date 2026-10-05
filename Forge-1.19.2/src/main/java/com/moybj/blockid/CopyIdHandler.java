package com.moybj.blockid;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class CopyIdHandler {
    public static void copyBlockId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack != null && stack.getItem() instanceof BlockItem blockItem) {
            String id = Registry.BLOCK.getKey(blockItem.getBlock()).toString();
            mc.keyboardHandler.setClipboard(id);
            Component message = Component.literal("【Block_ID】").withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal("已复制手持方块ID: " + id).withStyle(ChatFormatting.DARK_GREEN));
            mc.player.displayClientMessage(message, true);
        } else {
            mc.player.displayClientMessage(Component.literal("【Block_ID】手持物不是方块！").withStyle(ChatFormatting.DARK_RED), true);
        }
    }
}