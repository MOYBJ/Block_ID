package com.moybj.blockid;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * 界面皮肤：面板与按钮的底图全部来自 PNG 九宫格。
 *
 * <p>贴图放在 {@code assets/block_id/textures/gui/}，资源包同名覆盖即可换肤，
 * 不需要改任何 Java 代码。三张图都是 24×24、四边各留 4 像素边框，
 * 中间区域会被拉伸（纯色即可）。</p>
 */
public final class GuiSkin {
    /** 列表容器、属性面板的底图。 */
    private static final ResourceLocation PANEL = new ResourceLocation("block_id", "textures/gui/panel.png");
    /** 按钮常态。 */
    private static final ResourceLocation BUTTON = new ResourceLocation("block_id", "textures/gui/button.png");
    /** 按钮选中 / 悬停 / 可用态。 */
    private static final ResourceLocation BUTTON_ON = new ResourceLocation("block_id", "textures/gui/button_on.png");

    /** 贴图边长。 */
    private static final int TEX = 24;
    /** 九宫格边框宽度。 */
    private static final int SLICE = 4;

    private GuiSkin() {
    }

    public static void panel(PoseStack pose, int x, int y, int width, int height) {
        nine(pose, PANEL, x, y, width, height);
    }

    public static void button(PoseStack pose, int x, int y, int width, int height, boolean highlighted) {
        nine(pose, highlighted ? BUTTON_ON : BUTTON, x, y, width, height);
    }

    /** 九宫格绘制：四角原样、四边单向拉伸、中心双向拉伸。 */
    private static void nine(PoseStack pose, ResourceLocation tex, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.setShaderTexture(0, tex);
        if (w < SLICE * 2 || h < SLICE * 2) {
            GuiComponent.blit(pose, x, y, w, h, 0, 0, TEX, TEX, TEX, TEX);
            return;
        }
        int mw = w - SLICE * 2;
        int mh = h - SLICE * 2;
        int far = TEX - SLICE;
        GuiComponent.blit(pose, x, y, SLICE, SLICE, 0, 0, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x + w - SLICE, y, SLICE, SLICE, far, 0, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x, y + h - SLICE, SLICE, SLICE, 0, far, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x + w - SLICE, y + h - SLICE, SLICE, SLICE, far, far, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x + SLICE, y, mw, SLICE, SLICE, 0, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x + SLICE, y + h - SLICE, mw, SLICE, SLICE, far, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x, y + SLICE, SLICE, mh, 0, SLICE, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x + w - SLICE, y + SLICE, SLICE, mh, far, SLICE, SLICE, SLICE, TEX, TEX);
        GuiComponent.blit(pose, x + SLICE, y + SLICE, mw, mh, SLICE, SLICE, SLICE, SLICE, TEX, TEX);
    }
}
