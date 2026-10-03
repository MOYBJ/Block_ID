package com.moybj.blockid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.fabricmc.loader.api.FabricLoader;

public class WorldEditIntegration {

    /**
     * 运行时判断是否安装了 WorldEdit（软依赖）。
     * 本模组只生成 //set //replace 指令字符串，不调用 WorldEdit 的任何 API，
     * 因此未安装时全部功能照常可用，仅在提示语上做区分。
     */
    public static boolean isWorldEditLoaded() {
        try {
            return FabricLoader.getInstance().isModLoaded("worldedit");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void copySetCommand(String blockId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        String command = "//set " + blockId;
        mc.keyboard.setClipboard(command);
        mc.player.sendMessage(Text.literal(buildTip(command)), true);
    }

    public static void copyReplaceCommand(String mask, String blockId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        String command = "//replace " + mask + " " + blockId;
        mc.keyboard.setClipboard(command);
        mc.player.sendMessage(Text.literal(buildTip(command)), true);
    }

    private static String buildTip(String command) {
        return isWorldEditLoaded()
                ? "已复制指令: " + command
                : "已复制指令: " + command + "（未检测到 WorldEdit，请在聊天栏粘贴后手动执行）";
    }
}
