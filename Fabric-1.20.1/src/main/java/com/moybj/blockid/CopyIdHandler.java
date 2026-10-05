package com.moybj.blockid;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class CopyIdHandler {
    public static void copyBlockId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ItemStack stack = mc.player.getMainHandItem();
        if (stack != null && stack.getItem() instanceof BlockItem blockItem) {
            
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            String blockId = id.toString();

            
            mc.keyboardHandler.setClipboard(blockId);

            
            Component message = Component.literal("【Block_ID】")
                    .withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.literal("已复制手持方块ID: " + blockId)
                            .withStyle(ChatFormatting.DARK_GREEN));
            mc.player.displayClientMessage(message, true);
        } else {
            mc.player.displayClientMessage(
                    Component.literal("【Block_ID】手持物不是方块！").withStyle(ChatFormatting.DARK_RED),
                    true
            );
        }
    }
}