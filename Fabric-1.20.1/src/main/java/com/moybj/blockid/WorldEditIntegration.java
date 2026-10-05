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
        if (isWorldEditLoaded()) {
            runCommand(mc, command);
            mc.player.sendMessage(Text.literal("已执行指令: " + command), true);
        } else {
            mc.player.sendMessage(Text.literal(buildTip(command)), true);
        }
    }

    public static void copyReplaceCommand(String mask, String blockId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        String command = "//replace " + mask + " " + blockId;
        mc.keyboard.setClipboard(command);
        if (isWorldEditLoaded()) {
            runCommand(mc, command);
            mc.player.sendMessage(Text.literal("已执行指令: " + command), true);
        } else {
            mc.player.sendMessage(Text.literal(buildTip(command)), true);
        }
    }

    private static String buildTip(String command) {
        return isWorldEditLoaded()
                ? "已复制指令: " + command
                : "已复制指令: " + command + "（未检测到 WorldEdit，请在聊天栏粘贴后手动执行）";
    }

    /**
     * 检测到 WorldEdit 时直接发送指令，省去手动按 T 再 Ctrl+V 粘贴的步骤。
     * 指令始终会先写入剪贴板，即使发送失败玩家仍可手动粘贴执行。
     */
    private static void runCommand(MinecraftClient mc, String command) {
        try {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendCommand(command.startsWith("/") ? command.substring(1) : command);
            }
        } catch (Throwable ignored) {
            // 发送失败不影响使用：指令已在剪贴板中
        }
    }
}
