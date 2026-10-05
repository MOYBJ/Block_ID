package com.moybj.blockid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CopyIdHandler {
    public static void copyBlockId() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ItemStack stack = mc.player.getMainHandStack();
        if (stack != null && stack.getItem() instanceof BlockItem blockItem) {
            
            Identifier id = Registries.BLOCK.getId(blockItem.getBlock());
            String blockId = id.toString();

            
            mc.keyboard.setClipboard(blockId);

            
            Text message = Text.literal("【Block_ID】")
                    .formatted(Formatting.DARK_GREEN)
                    .append(Text.literal("已复制手持方块ID: " + blockId)
                            .formatted(Formatting.DARK_GREEN));
            mc.player.sendMessage(message, true);
        } else {
            mc.player.sendMessage(
                    Text.literal("【Block_ID】手持物不是方块！").formatted(Formatting.DARK_RED),
                    true
            );
        }
    }
}