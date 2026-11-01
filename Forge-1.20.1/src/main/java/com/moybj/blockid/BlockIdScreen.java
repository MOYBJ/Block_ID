package com.moybj.blockid;

import com.github.promeg.pinyinhelper.Pinyin;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BlockIdScreen extends Screen {
    public static boolean isReplaceMode = false;
    public static boolean isCommandMode = false;

    
    private boolean targetTabMode = false;
    private int midTabMode = 0;

    private EditBox searchBar;
    private EditBox targetSearchBar;

    private List<String> allBlocks;
    private List<String> favoriteBlocks;
    private List<HistoryManager.HistoryItem> historyItems;

    private final List<String> selectedIds = new ArrayList<>();
    private final List<String> selectedSources = new ArrayList<>();
    private final List<String> selectedTargets = new ArrayList<>();

    
    private final Map<String, Map<String, String>> blockProperties = new HashMap<>();
    
    private final Map<String, Integer> blockWeights = new HashMap<>();
    private String editingBlockId = null;
    private int propScroll = 0;
    
    private EditBox weightInputField = null;
    private EditBox renameGroupField = null;
    private String weightInputBlockId = null;
    /** 这次比例编辑属于哪个列表（替换模式下源/目标可能是同一个方块，不能只按 ID 猜）。 */
    private List<String> weightInputGroup = null;
    /** 预览框当前内容的校验问题；null 表示格式正常。 */
    private String previewError = null;
    private EditBox previewField = null;

    private final Map<String, List<String>> fullPinyinCache = new HashMap<>();
    private final Map<String, List<String>> initialPinyinCache = new HashMap<>();

    private String lastSearchText = "";
    private String lastTargetText = "";
    private List<String> cachedFilteredAll;
    private List<String> cachedFilteredFavorite;
    private List<String> cachedFilteredSelected;
    private List<String> cachedFilteredTarget;

    private int scrollAll = 0;
    private int scrollFavorite = 0;
    private int scrollHistory = 0;

    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 18;
    private int scrollSelected = 0;
    private int scrollSrc = 0;
    private int scrollTgt = 0;
    private int scrollSelSrc = 0;
    private int scrollSelTgt = 0;

    private final int ITEM_HEIGHT = 24;
    private int listStartY, listEndY, bottomBtnY, tipY;
    private int colWidth, leftX, midX, rightX;
    private int rightLineY;
    private int inputY, toggleBtnY;
    private int listHeaderY;

    private final int margin = 20;
    private final int gap = 10;
    private final int scrollBarWidth = 6;

    private int draggingScrollbar = -1;

    private static final List<String> EXCLUDED_NAMESPACES = List.of("blocktown");

    
    private static final Map<String, String> PROP_NAME_MAP = Map.ofEntries(
        Map.entry("facing", "朝向"), Map.entry("half", "半层"), Map.entry("shape", "形状"),
        Map.entry("waterlogged", "含水"), Map.entry("type", "类型"), Map.entry("powered", "充能"),
        Map.entry("lit", "点亮"), Map.entry("open", "开启"), Map.entry("hinge", "铰链侧"),
        Map.entry("part", "部位"), Map.entry("occupied", "占用"), Map.entry("stage", "阶段"),
        Map.entry("age", "生长"), Map.entry("level", "等级"), Map.entry("distance", "距离"),
        Map.entry("persistent", "永久"), Map.entry("axis", "轴向"), Map.entry("rotation", "旋转"),
        Map.entry("snowlogged", "覆雪"), Map.entry("down", "下方"), Map.entry("up", "上方"),
        Map.entry("east", "东侧"), Map.entry("west", "西侧"), Map.entry("north", "北侧"),
        Map.entry("south", "南侧"), Map.entry("note", "音符"), Map.entry("instrument", "乐器"),
        Map.entry("mode", "模式"), Map.entry("delay", "延迟"), Map.entry("hatch", "舱口"),
        Map.entry("short", "短"), Map.entry("extended", "伸出"), Map.entry("drag", "拖拽"),
        Map.entry("signal_fire", "信号火"), Map.entry("layers", "层数"), Map.entry("moisture", "湿度"),
        Map.entry("bites", "咬口"), Map.entry("honey_level", "蜜量"), Map.entry("eggs", "蛋数"),
        Map.entry("pickles", "海泡菜数"), Map.entry("candles", "蜡烛数"), Map.entry("charges", "充能数"),
        Map.entry("bloom", "绽放"), Map.entry("sculk_sensor_phase", "传感器阶段"),
        Map.entry("vault_state", "宝库状态"), Map.entry("ominous", "不祥"), Map.entry("trial_spawner_state", "刷怪状态")
    );

    
    private static final Map<String, String> PROP_VALUE_MAP = Map.ofEntries(
        Map.entry("north", "北"), Map.entry("south", "南"), Map.entry("east", "东"),
        Map.entry("west", "西"), Map.entry("up", "上"), Map.entry("down", "下"),
        Map.entry("top", "上半"), Map.entry("bottom", "下半"), Map.entry("true", "是"),
        Map.entry("false", "否"), Map.entry("left", "左"), Map.entry("right", "右"),
        Map.entry("straight", "直"), Map.entry("inner_left", "左内弯"),
        Map.entry("inner_right", "右内弯"), Map.entry("outer_left", "左外弯"),
        Map.entry("outer_right", "右外弯"), Map.entry("x", "X轴"), Map.entry("y", "Y轴"),
        Map.entry("z", "Z轴"), Map.entry("floor", "地面"), Map.entry("wall", "墙上"),
        Map.entry("ceiling", "天花板"), Map.entry("lower", "下半"), Map.entry("upper", "上半"),
        Map.entry("none", "无"), Map.entry("side", "侧边"), Map.entry("double", "双层"),
        Map.entry("single", "单层"), Map.entry("rising", "上升"), Map.entry("falling", "下降"),
        Map.entry("horizontal", "水平"), Map.entry("vertical", "垂直"),
        Map.entry("front", "前"), Map.entry("back", "后"),
        Map.entry("inactive", "未激活"), Map.entry("active", "激活"), Map.entry("cooldown", "冷却"),
        Map.entry("waiting", "等待"), Map.entry("in_progress", "进行中"), Map.entry("finished", "完成"),
        Map.entry("reward", "奖励"), Map.entry("locked", "锁定"), Map.entry("unlocked", "解锁"),
        Map.entry("harp", "竖琴"), Map.entry("basedrum", "底鼓"), Map.entry("snare", "军鼓"),
        Map.entry("hat", "踩镲"), Map.entry("bass", "贝斯"), Map.entry("flute", "长笛"),
        Map.entry("bell", "铃铛"), Map.entry("guitar", "吉他"), Map.entry("chime", "风铃"),
        Map.entry("xylophone", "木琴"), Map.entry("iron_xylophone", "铁琴"),
        Map.entry("cow_bell", "牛铃"), Map.entry("didgeridoo", "迪吉里杜管"),
        Map.entry("bit", "电子音"), Map.entry("banjo", "班卓琴"), Map.entry("pling", "拨弦"),
        Map.entry("zombie", "僵尸"), Map.entry("skeleton", "骷髅"), Map.entry("spider", "蜘蛛"),
        Map.entry("creeper", "苦力怕"), Map.entry("dragon", "末影龙"), Map.entry("wither", "凋灵"),
        Map.entry("piglin", "猪灵"), Map.entry("custom", "自定义")
    );

    private static String mapPropName(String name) {
        return PROP_NAME_MAP.getOrDefault(name, name);
    }

    private static String mapPropValue(String value) {
        return PROP_VALUE_MAP.getOrDefault(value, value);
    }

    public BlockIdScreen() {
        super(Component.translatable("gui.block_id.title"));
        this.allBlocks = loadAllBlocks();
        this.favoriteBlocks = FavoritesManager.getCurrentBlocks();
        this.historyItems = HistoryManager.getHistory();
        for (String blockId : allBlocks) {
            String displayName = getBlockDisplayName(blockId).toLowerCase();
            fullPinyinCache.put(blockId, getAllFullPinyins(displayName));
            initialPinyinCache.put(blockId, getAllInitials(displayName));
        }
        cachedFilteredAll = allBlocks;
        cachedFilteredFavorite = favoriteBlocks;
        cachedFilteredSelected = selectedIds;
        cachedFilteredTarget = allBlocks;
    }

    private List<String> loadAllBlocks() {
        List<String> all = new ArrayList<>();
        ForgeRegistries.BLOCKS.forEach(block -> {
            ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
            if (rl == null || EXCLUDED_NAMESPACES.contains(rl.getNamespace())) return;
            String id = rl.toString();
            
            
            if (!id.equals("minecraft:air")) {
                if (block instanceof AirBlock) return;
                String displayName = new ItemStack(block).getHoverName().getString();
                if (displayName.equals("空气") || displayName.equalsIgnoreCase("Air")) return;
            }
            all.add(id);
        });
        return all;
    }

    private boolean isInFavorite(String blockId) {
        return favoriteBlocks.contains(blockId);
    }

    private int getMaxScroll(int contentSize, int viewHeight) {
        return Math.max(0, contentSize * ITEM_HEIGHT - viewHeight);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        colWidth = (this.width - margin * 2 - scrollBarWidth * 3 - gap * 2) / 3;
        leftX = margin;
        midX = leftX + colWidth + scrollBarWidth + gap;
        rightX = midX + colWidth + scrollBarWidth + gap;

        
        inputY = 38;
        toggleBtnY = 68;
        listStartY = 118;
        listEndY = this.height - 136;
        bottomBtnY = this.height - 65;
        tipY = this.height - 28;
        rightLineY = listStartY + (listEndY - listStartY) / 2;
        listHeaderY = 102; 

        if (isReplaceMode) {
            this.searchBar = new EditBox(this.font, leftX, inputY, colWidth, 20, Component.translatable("gui.block_id.search"));
            this.targetSearchBar = new EditBox(this.font, midX, inputY, colWidth, 20, Component.translatable("gui.block_id.target_label"));
        } else {
            this.searchBar = new EditBox(this.font, centerX - 100, inputY, 200, 20, Component.translatable("gui.block_id.search"));
            this.targetSearchBar = new EditBox(this.font, midX, inputY, colWidth, 20, Component.translatable("gui.block_id.target_label"));
            this.targetSearchBar.setVisible(false);
        }
        this.searchBar.setMaxLength(50);
        this.targetSearchBar.setMaxLength(50);
        this.addRenderableWidget(this.searchBar);
        this.addRenderableWidget(this.targetSearchBar);

        
        if (isReplaceMode) {
            int tabY = 92;
            int tabH = 18;
            this.addRenderableWidget(Button.builder(Component.literal("目标方块"), b -> {
                this.targetTabMode = false;
            }).bounds(midX, tabY, colWidth / 2, tabH).build());

            this.addRenderableWidget(Button.builder(Component.literal("收藏夹"), b -> {
                this.targetTabMode = true;
            }).bounds(midX + colWidth / 2, tabY, colWidth / 2, tabH).build());
        }

        String leftBtnText = isCommandMode ? "切换到ID复制" : "切换到指令模式";
        this.addRenderableWidget(Button.builder(Component.literal(leftBtnText), b -> {
            BlockIdScreen.isCommandMode = !BlockIdScreen.isCommandMode;
            this.minecraft.setScreen(new BlockIdScreen());
        }).bounds(centerX - 105, toggleBtnY, 100, 20).build());

        String rightBtnText = isReplaceMode ? "切换到复制模式" : "切换到替换模式";
        this.addRenderableWidget(Button.builder(Component.literal(rightBtnText), b -> {
            BlockIdScreen.isReplaceMode = !BlockIdScreen.isReplaceMode;
            this.minecraft.setScreen(new BlockIdScreen());
        }).bounds(centerX + 5, toggleBtnY, 100, 20).build());

        if (!isCommandMode) {
            this.addRenderableWidget(Button.builder(Component.literal("复制ID"), b -> {
                boolean empty = isReplaceMode
                        ? (selectedSources.isEmpty() || selectedTargets.isEmpty())
                        : selectedIds.isEmpty();
                if (empty) {
                    this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
                    return;
                }
                String out = currentOutput();
                if (out == null) return;
                this.minecraft.keyboardHandler.setClipboard(out);
                this.minecraft.player.displayClientMessage(Component.literal("已复制: " + out), true);
                addToHistory(out, isReplaceMode ? "replace" : "id");
            }).bounds(centerX - 105, bottomBtnY, 100, 20).build());
        } else {
            if (!isReplaceMode) {
                this.addRenderableWidget(Button.builder(Component.literal("复制 (Set)"), b -> {
                    if (selectedIds.isEmpty()) {
                        this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
                        return;
                    }
                    String out = currentOutput();
                    if (out == null) return;
                    this.minecraft.keyboardHandler.setClipboard(out);
                    WorldEditIntegration.runFullCommand(out);
                    addToHistory(out, "set");
                }).bounds(centerX - 105, bottomBtnY, 100, 20).build());
            } else {
                this.addRenderableWidget(Button.builder(Component.literal("复制 (Replace)"), b -> {
                    if (selectedSources.isEmpty() || selectedTargets.isEmpty()) {
                        this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
                        return;
                    }
                    String out = currentOutput();
                    if (out == null) return;
                    this.minecraft.keyboardHandler.setClipboard(out);
                    WorldEditIntegration.runFullCommand(out);
                    addToHistory(out, "replace");
                }).bounds(centerX - 105, bottomBtnY, 100, 20).build());
            }
        }

        this.addRenderableWidget(Button.builder(Component.literal("清空"), b -> {
            selectedIds.clear();
            selectedSources.clear();
            selectedTargets.clear();
            blockProperties.clear();
            blockWeights.clear();
            editingBlockId = null;
            refreshPreview();
        }).bounds(centerX + 5, bottomBtnY, 100, 20).build());

        initPreviewField();
    }

    /** 底部预览框：显示当前会输出的指令/ID，用户可以直接在这里改。 */
    private void initPreviewField() {
        this.previewField = new EditBox(this.font, margin, this.height - 102, this.width - margin * 2, 16,
                Component.literal(""));
        this.previewField.setMaxLength(2000);
        this.previewField.setResponder(t -> validatePreview());
        this.addRenderableWidget(this.previewField);
        refreshPreview();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (weightInputBlockId != null && weightInputField != null && weightInputField.isFocused()) {
            if (keyCode == 257 || keyCode == 335) { confirmWeightInput(); return true; }
            if (keyCode == 256) { weightInputBlockId = null; weightInputGroup = null; weightInputField.setFocused(false); return true; }
            return weightInputField.keyPressed(keyCode, scanCode, modifiers);
        }
        if (renameGroupField != null && renameGroupField.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                String newName = renameGroupField.getValue();
                if (newName != null && !newName.isEmpty()) {
                    FavoritesManager.renameGroup(FavoritesManager.getCurrentGroupIndex(), newName);
                    this.minecraft.player.displayClientMessage(Component.literal("组已重命名为: " + newName), true);
                }
                renameGroupField = null;
                return true;
            }
            if (keyCode == 256) {
                renameGroupField = null;
                return true;
            }
            return renameGroupField.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.searchBar.keyPressed(keyCode, scanCode, modifiers) || this.targetSearchBar.keyPressed(keyCode, scanCode, modifiers)) {
            resetScroll();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (weightInputBlockId != null && weightInputField != null && weightInputField.isFocused()) {
            return weightInputField.charTyped(codePoint, modifiers);
        }
        if (renameGroupField != null && renameGroupField.isFocused()) {
            return renameGroupField.charTyped(codePoint, modifiers);
        }
        if (this.searchBar.charTyped(codePoint, modifiers) || this.targetSearchBar.charTyped(codePoint, modifiers)) {
            resetScroll();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void resetScroll() {
        scrollAll = 0;
        scrollFavorite = 0;
        scrollHistory = 0;
        scrollSelected = 0;
        scrollSrc = 0;
        scrollTgt = 0;
        scrollSelSrc = 0;
        scrollSelTgt = 0;
        
        currentPage = 0;
    }

    private void renderScrollbar(GuiGraphics graphics, int x, int y, int height, int scroll, int totalContent) {
        int contentHeight = totalContent * ITEM_HEIGHT;
        if (contentHeight <= height) return;

        graphics.fill(x, y, x + scrollBarWidth, y + height, 0x80000000);
        float visibleRatio = (float) height / contentHeight;
        float sliderHeight = Math.max(20, height * visibleRatio);
        sliderHeight = Math.min(sliderHeight, height);

        int maxScroll = getMaxScroll(totalContent, height);
        float sliderY = y;
        if (maxScroll > 0) {
            sliderY = y + (float) scroll / maxScroll * (height - sliderHeight);
            sliderY = Math.max(y, Math.min(y + height - sliderHeight, sliderY));
        }
        graphics.fill(x, (int) sliderY, x + scrollBarWidth, (int) (sliderY + sliderHeight), 0xCCFFFFFF);
    }

    private boolean handleScrollbarClick(int x, int y, int height, int scroll, int totalContent, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + scrollBarWidth && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.fillGradient(0, 0, this.width, this.height, 0xE0202020, 0xF0101010);
        graphics.fillGradient(0, 0, this.width, 60, 0xFF3a6ea5, 0x00000000);
        RenderSystem.disableBlend();

        graphics.drawCenteredString(this.font, Component.translatable("gui.block_id.title"), this.width / 2, 15, 0xFFFFFFFF);

        
        int panelBg = 0x90000000;
        int panelBorder = 0xFF3a6ea5;
        graphics.fill(leftX - 3, listStartY - 3, leftX + colWidth + scrollBarWidth + 3, listEndY + 3, panelBg);
        graphics.fill(midX - 3, listStartY - 3, midX + colWidth + scrollBarWidth + 3, listEndY + 3, panelBg);
        graphics.fill(rightX - 3, listStartY - 3, rightX + colWidth + 3, listEndY + 3, panelBg);
        
        graphics.fill(leftX - 3, listStartY - 3, leftX + colWidth + scrollBarWidth + 3, listStartY - 2, panelBorder);
        graphics.fill(midX - 3, listStartY - 3, midX + colWidth + scrollBarWidth + 3, listStartY - 2, panelBorder);
        graphics.fill(rightX - 3, listStartY - 3, rightX + colWidth + 3, listStartY - 2, panelBorder);

        super.render(graphics, mouseX, mouseY, partialTick);

        this.searchBar.render(graphics, mouseX, mouseY, partialTick);
        if (isReplaceMode) this.targetSearchBar.render(graphics, mouseX, mouseY, partialTick);

        
        if (this.searchBar.getValue().isEmpty() && !this.searchBar.isFocused()) {
            graphics.drawString(this.font, "搜索...", this.searchBar.getX() + 4, this.searchBar.getY() + 6, 0xFF666666);
        }
        if (isReplaceMode && this.targetSearchBar.getValue().isEmpty() && !this.targetSearchBar.isFocused()) {
            graphics.drawString(this.font, "搜索目标...", this.targetSearchBar.getX() + 4, this.targetSearchBar.getY() + 6, 0xFF666666);
        }

        String searchText = searchBar.getValue().toLowerCase();
        String targetText = targetSearchBar.getValue().toLowerCase();

        if (!searchText.equals(lastSearchText)) {
            cachedFilteredAll = filterBlocks(allBlocks, searchText);
            cachedFilteredFavorite = filterBlocks(favoriteBlocks, searchText);
            cachedFilteredSelected = filterBlocks(selectedIds, searchText);
            lastSearchText = searchText;
        }
        if (isReplaceMode && !targetText.equals(lastTargetText)) {
            cachedFilteredTarget = filterBlocks(allBlocks, targetText);
            lastTargetText = targetText;
        }

        if (isReplaceMode) {
            graphics.drawString(this.font, "替换源", leftX, listHeaderY, 0xFFAAAAAA);
            graphics.enableScissor(leftX - 5, listStartY, leftX + colWidth, listEndY + 2);
            renderList(graphics, leftX, listStartY - scrollSrc, cachedFilteredAll, selectedSources, 0xCC4CAF50, 0xAA000000, "all");
            graphics.disableScissor();
            renderScrollbar(graphics, leftX + colWidth, listStartY, listEndY - listStartY, scrollSrc, cachedFilteredAll.size());

            
            if (targetTabMode) {
                graphics.enableScissor(midX - 5, listStartY, midX + colWidth, listEndY + 2);
                renderList(graphics, midX, listStartY - scrollFavorite, cachedFilteredFavorite, selectedTargets, 0xCCFF9800, 0xAA000000, "frequent");
                graphics.disableScissor();
                renderScrollbar(graphics, midX + colWidth, listStartY + 52, listEndY - listStartY - 52, scrollFavorite, cachedFilteredFavorite.size());
            } else {
                graphics.enableScissor(midX - 5, listStartY, midX + colWidth, listEndY + 2);
                renderList(graphics, midX, listStartY - scrollTgt, cachedFilteredTarget, selectedTargets, 0xCCFF9800, 0xAA000000, "all");
                graphics.disableScissor();
                renderScrollbar(graphics, midX + colWidth, listStartY, listEndY - listStartY, scrollTgt, cachedFilteredTarget.size());
            }

            graphics.drawString(this.font, "已选源", rightX, listHeaderY, 0xFFAAAAAA);
            graphics.enableScissor(rightX - 5, listStartY, rightX + colWidth, rightLineY - 5);
            renderList(graphics, rightX, listStartY - scrollSelSrc, selectedSources, null, 0xCC4CAF50, 0xAA000000, "source");
            graphics.disableScissor();
            graphics.fill(rightX - 5, rightLineY, rightX + colWidth, rightLineY + 2, 0xFF555555);
            graphics.drawString(this.font, "已选目标", rightX, rightLineY + 4, 0xFFAAAAAA);
            graphics.enableScissor(rightX - 5, rightLineY + 10, rightX + colWidth, listEndY + 2);
            renderList(graphics, rightX, rightLineY + 10 - scrollSelTgt, selectedTargets, null, 0xCCFF9800, 0xAA000000, "selected");
            graphics.disableScissor();
        } else {
            List<String> pageBlocks = getCurrentPageBlocks();
            graphics.drawString(this.font, "全部方块 (第" + (currentPage + 1) + "/" + getTotalPages() + "页)", leftX, listHeaderY, 0xFFAAAAAA);
            graphics.enableScissor(leftX - 5, listStartY, leftX + colWidth, listEndY + 2);
            renderList(graphics, leftX, listStartY - scrollAll, pageBlocks, selectedIds, 0xCC4CAF50, 0xAA000000, "all");
            graphics.disableScissor();
            renderScrollbar(graphics, leftX + colWidth, listStartY, listEndY - listStartY, scrollAll, pageBlocks.size());

            int tabW = colWidth / 2; int favColor = midTabMode == 0 ? 0xFF4CAF50 : 0xFF666666; int histColor = midTabMode == 1 ? 0xFF4CAF50 : 0xFF666666; graphics.drawString(this.font, "收藏夹", midX, listHeaderY, favColor); graphics.drawString(this.font, "历史", midX + tabW, listHeaderY, histColor);
            int listOffset = 0;
            if (midTabMode == 0) {
                int groupsSize = FavoritesManager.getGroups().size();
                int curIdx = FavoritesManager.getCurrentGroupIndex();
                String groupName = FavoritesManager.getCurrentGroup().name;
                int nameY = listHeaderY + 16;
                int btnW = 20;
                int btnH = 14;
                int gap = 4;
                int step = btnW + gap;
                boolean[] visible = {curIdx > 0, curIdx < groupsSize - 1, true, true, groupsSize > 1};
                int btnY = listHeaderY + 30;
                if (renameGroupField != null) {
                    renameGroupField.render(graphics, mouseX, mouseY, partialTick);
                } else {
                    graphics.drawString(this.font, groupName, midX, nameY, 0xFFFFFFFF);
                }
                graphics.drawString(this.font, (curIdx + 1) + "/" + groupsSize, midX + this.font.width(groupName) + 6, nameY, 0xFFAAAAAA);
                int[] colors = {0xFF666666, 0xFF666666, 0xFF388E3C, 0xFF1565C0, 0xFFC62828};
                String[] labels = {"<", ">", "+", "R", "D"};
                int bx = midX;
                for (int i = 0; i < 5; i++) {
                    if (!visible[i]) continue;
                    boolean hover = mouseX >= bx && mouseX <= bx + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
                    graphics.fill(bx, btnY, bx + btnW, btnY + btnH, hover ? colors[i] + 0x222222 : colors[i]);
                    graphics.fill(bx, btnY, bx + btnW, btnY + 1, 0xFFFFFFFF);
                    graphics.fill(bx, btnY + btnH - 1, bx + btnW, btnY + btnH, 0xFF000000);
                    graphics.drawCenteredString(this.font, labels[i], bx + btnW / 2, btnY + 3, 0xFFFFFFFF);
                    bx += step;
                }
                int lineY = listHeaderY + 48;
                graphics.fill(midX, lineY, midX + colWidth, lineY + 1, 0xFF4A90D9);
                listOffset = 40;
            }
            graphics.enableScissor(midX - 5, listStartY + listOffset, midX + colWidth, listEndY + 2);
            if (midTabMode == 0) { renderList(graphics, midX, listStartY + listOffset - scrollFavorite, cachedFilteredFavorite, selectedIds, 0xCC4CAF50, 0xAA000000, "favorite"); } else { renderHistoryList(graphics, midX, listStartY - scrollHistory); }
            graphics.disableScissor();
            if (midTabMode == 0 && listOffset > 0) {
                renderScrollbar(graphics, midX + colWidth, listStartY + listOffset, listEndY - listStartY - listOffset, scrollFavorite, cachedFilteredFavorite.size());
            }

            graphics.drawString(this.font, "已选列表", rightX, listHeaderY, 0xFFAAAAAA);
            graphics.enableScissor(rightX - 5, listStartY, rightX + colWidth, listEndY + 2);
            renderList(graphics, rightX, listStartY - scrollSelected, selectedIds, null, 0xCCFF9800, 0xAA000000, "selected");
            graphics.disableScissor();
            renderScrollbar(graphics, rightX + colWidth, listStartY, listEndY - listStartY, scrollSelected, selectedIds.size());
        }

        int totalSelected = isReplaceMode ? (selectedSources.size() + selectedTargets.size()) : selectedIds.size();
        graphics.drawCenteredString(this.font, Component.translatable("gui.block_id.selected_count", totalSelected), this.width / 2, tipY, 0xFFFFFFFF);

        
        // 预览框标题与校验提示
        graphics.drawString(this.font, isCommandMode ? "指令预览（可直接修改） · 比例全 100 = 等概率随机" : "ID 预览（可直接修改） · 比例全 100 = 等概率随机",
                margin, this.height - 113, 0xFF9FB4D0);
        if (previewError != null) {
            graphics.drawString(this.font, "✕ " + previewError, margin, this.height - 82, 0xFFFF6B6B);
        }

        
        if (weightInputBlockId != null && weightInputField != null && weightInputField.isFocused()) {
            renderWeightInput(graphics);
        }

        
        if (editingBlockId != null) {
            renderPropertyEditor(graphics, mouseX, mouseY);
        }

        
        if (!isReplaceMode) {
            int pageBtnY = listEndY + 2;
            int pageBtnW = 50;
            int pageBtnH = 18;
            if (currentPage > 0) {
                graphics.fill(leftX, pageBtnY, leftX + pageBtnW, pageBtnY + pageBtnH, 0xFF4CAF50);
                graphics.drawString(this.font, "上一页", leftX + pageBtnW / 2 - this.font.width("上一页") / 2, pageBtnY + 5, 0xFFFFFFFF);
            }
            if (currentPage < getTotalPages() - 1) {
                graphics.fill(leftX + colWidth - pageBtnW, pageBtnY, leftX + colWidth, pageBtnY + pageBtnH, 0xFF4CAF50);
                graphics.drawString(this.font, "下一页", leftX + colWidth - pageBtnW / 2 - this.font.width("下一页") / 2, pageBtnY + 5, 0xFFFFFFFF);
            }
        }
    }

    private void renderWeightInput(GuiGraphics graphics) {
        
        int itemY = -1;
        List<String> activeList = selectedIds;
        int activeScroll = scrollSelected;
        if (isReplaceMode) {
            int srcIdx = (weightInputGroup == selectedSources) ? selectedSources.indexOf(weightInputBlockId) : -1;
            if (srcIdx >= 0) {
                itemY = listStartY + srcIdx * ITEM_HEIGHT - scrollSelSrc;
                activeList = selectedSources; activeScroll = scrollSelSrc;
            } else {
                int tgtIdx = (weightInputGroup == selectedTargets) ? selectedTargets.indexOf(weightInputBlockId) : -1;
                if (tgtIdx >= 0) {
                    itemY = rightLineY + 10 + tgtIdx * ITEM_HEIGHT - scrollSelTgt;
                    activeList = selectedTargets; activeScroll = scrollSelTgt;
                }
            }
        } else {
            int idx = selectedIds.indexOf(weightInputBlockId);
            if (idx >= 0) itemY = listStartY + idx * ITEM_HEIGHT - scrollSelected;
        }
        if (itemY < 0) { weightInputBlockId = null; return; }

        
        int inputX = rightX + colWidth - 66;
        int inputY = itemY + 4;
        weightInputField.setX(inputX);
        weightInputField.setY(inputY);
        weightInputField.setWidth(40);
        weightInputField.render(graphics, 0, 0, 0);
    }

    private void renderPropertyEditor(GuiGraphics graphics, int mouseX, int mouseY) {
        
        Block block = getBlockFromId(editingBlockId);
        List<Property<?>> properties = (block != null) ? new ArrayList<>(block.getStateDefinition().getProperties()) : new ArrayList<>();

        int headerH = 18;
        int lineH = 13;
        int padding = 10;
        int contentH = Math.min(properties.size() * lineH, 5 * lineH); 
        int panelH = headerH + contentH + padding;
        int panelW = 230; 
        int panelX = margin; 
        int panelY = this.height - panelH - 5; 

        
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF141428);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 1, 0xFF4a8ad5);
        graphics.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF4a8ad5);
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelH, 0xFF4a8ad5);
        graphics.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, 0xFF4a8ad5);

        
        graphics.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + headerH, 0xFF1a2540);
        String blockName = getBlockDisplayName(editingBlockId);
        String title = "⚙ " + blockName;
        if (this.font.width(title) > panelW - 90) {
            
            while (this.font.width(title + "...") > panelW - 90 && title.length() > 1) title = title.substring(0, title.length() - 1);
            title += "...";
        }
        graphics.drawString(this.font, title, panelX + 6, panelY + 5, 0xFF88BBFF);

        
        int closeX = panelX + panelW - 36;
        int closeY = panelY + 3;
        graphics.fill(closeX, closeY, closeX + 30, closeY + 13, 0xFF8B2020);
        graphics.drawCenteredString(this.font, "×", closeX + 15, closeY + 3, 0xFFFFFFFF);

        
        int copyX = panelX + panelW - 74;
        int copyY = panelY + 3;
        graphics.fill(copyX, copyY, copyX + 34, copyY + 13, 0xFF2a6a3a);
        graphics.drawCenteredString(this.font, "复制", copyX + 17, copyY + 3, 0xFFFFFFFF);

        if (properties.isEmpty()) {
            graphics.drawCenteredString(this.font, "无可用属性", panelX + panelW / 2, panelY + headerH + 8, 0xFF666666);
            return;
        }

        
        int contentY = panelY + headerH + 3;
        int maxScroll = Math.max(0, properties.size() * lineH - contentH);
        propScroll = Math.max(0, Math.min(maxScroll, propScroll));

        graphics.enableScissor(panelX + 4, contentY, panelX + panelW - 4, contentY + contentH);
        int y = contentY - propScroll;
        Map<String, String> currentProps = blockProperties.computeIfAbsent(editingBlockId, k -> new HashMap<>());

        for (int i = 0; i < properties.size(); i++) {
            Property<?> prop = properties.get(i);
            if (y + lineH < contentY || y > contentY + contentH) { y += lineH; continue; }

            String propName = prop.getName();
            String currentValue = currentProps.getOrDefault(propName, getDefaultPropertyValue(block, prop));

            int rowBg = (i % 2 == 0) ? 0xFF181830 : 0xFF1c1c36;
            graphics.fill(panelX + 4, y, panelX + panelW - 4, y + lineH - 1, rowBg);
            graphics.fill(panelX + 4, y, panelX + 6, y + lineH - 1, 0xFF4a8ad5);

            
            String cnName = mapPropName(propName);
            graphics.drawString(this.font, cnName, panelX + 10, y + 3, 0xFF88BBFF);

            
            String displayValue = mapPropValue(currentValue);
            String valueText = displayValue + " " + currentValue;
            int valueWidth = this.font.width(valueText);
            int valueX = panelX + panelW - valueWidth - 10;
            graphics.fill(valueX - 2, y + 1, valueX + valueWidth + 2, y + lineH - 2, 0xFF252545);
            graphics.drawString(this.font, valueText, valueX, y + 3, 0xFFFFFF88);

            y += lineH;
        }
        graphics.disableScissor();

        
        if (maxScroll > 0) {
            int sbX = panelX + panelW - 5;
            graphics.fill(sbX, contentY, sbX + 2, contentY + contentH, 0x30FFFFFF);
            float ratio = (float) contentH / (properties.size() * lineH);
            int thumbH = Math.max(12, (int)(contentH * ratio));
            int thumbY = contentY + (int)((float) propScroll / maxScroll * (contentH - thumbH));
            graphics.fill(sbX, thumbY, sbX + 2, thumbY + thumbH, 0x8888BBFF);
        }
    }

    private void renderList(GuiGraphics graphics, int x, int y, List<String> filtered, List<String> selectionList, int selectedColor, int normalColor, String listKind) {
        for (String blockId : filtered) {
            if (y + ITEM_HEIGHT - 4 > listEndY) break;
            boolean isSelected = selectionList != null && selectionList.contains(blockId);
            if (isSelected) {
                graphics.fill(x, y, x + colWidth, y + ITEM_HEIGHT - 4, selectedColor);
                graphics.fill(x, y, x + 2, y + ITEM_HEIGHT - 4, 0xFF8BC34A);
            } else {
                graphics.fill(x, y, x + colWidth, y + ITEM_HEIGHT - 4, normalColor);
            }
            
            graphics.fill(x, y + ITEM_HEIGHT - 5, x + colWidth, y + ITEM_HEIGHT - 4, 0xFF222222);

            try {
                String[] parts = blockId.split(":");
                ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
                ItemStack stack = new ItemStack(BuiltInRegistries.BLOCK.get(rl));
                graphics.renderItem(stack, x + 4, y + 2);

                
                if ("selected".equals(listKind)) {
                    Map<String, String> props = blockProperties.get(blockId);
                    int weight = blockWeights.getOrDefault(blockId, 100);
                    String displayText = stack.getHoverName().getString();
                    if (props != null && !props.isEmpty()) displayText += " [" + props.size() + "]";
                    graphics.drawString(this.font, displayText, x + 26, y + 6, 0xFFFFFFFF);

                    
                    int trackX = x + colWidth - 66;
                    int boxW = 40;
                    int boxH = 14;
                    int boxY = y + 4;
                    boolean editing = blockId.equals(weightInputBlockId);

                    if (!editing) {
                        graphics.fill(trackX, boxY, trackX + boxW, boxY + boxH, 0xFF1E2942);
                        graphics.fill(trackX, boxY, trackX + boxW, boxY + 1, 0xFF4a8ad5);
                        graphics.fill(trackX, boxY + boxH - 1, trackX + boxW, boxY + boxH, 0xFF4a8ad5);
                        graphics.fill(trackX, boxY, trackX + 1, boxY + boxH, 0xFF4a8ad5);
                        graphics.fill(trackX + boxW - 1, boxY, trackX + boxW, boxY + boxH, 0xFF4a8ad5);
                        String weightText = weight + "%";
                        graphics.drawString(this.font, weightText,
                                trackX + (boxW - this.font.width(weightText)) / 2, y + 6, 0xFFFFFF88);
                    }

                    graphics.drawString(this.font, "⚙", x + colWidth - 12, y + 6, 0xFF66B0FF);
                } else if ("source".equals(listKind)) {
                    Map<String, String> props = blockProperties.get(blockId);
                    String displayText = stack.getHoverName().getString();
                    if (props != null && !props.isEmpty()) displayText += " [" + props.size() + "]";
                    graphics.drawString(this.font, displayText, x + 26, y + 6, 0xFFFFFFFF);
                    graphics.drawString(this.font, "⚙", x + colWidth - 12, y + 6, 0xFF66B0FF);
                } else {
                    graphics.drawString(this.font, stack.getHoverName().getString(), x + 26, y + 6, 0xFFFFFFFF);
                    if ("all".equals(listKind)) {
                        if (isInFavorite(blockId)) graphics.drawString(this.font, "✔", x + colWidth - 16, y + 6, 0xFF00FF00);
                        else graphics.drawString(this.font, "➕", x + colWidth - 16, y + 6, 0xFFAAAAAA);
                    } else if ("frequent".equals(listKind)) {
                        graphics.drawString(this.font, "➖", x + colWidth - 16, y + 6, 0xFFFF6666);
                    }
                }
            } catch (Exception e) {
                graphics.drawString(this.font, blockId, x + 26, y + 6, 0xFFFFFFFF);
            }
            y += ITEM_HEIGHT;
            if (y > listEndY + 20) break;
        }
    }

    private List<String> filterBlocks(List<String> source, String search) {
        if (search.isEmpty()) return source;
        String cleanSearch = search.toLowerCase().replace("_", "").replace(" ", "");
        List<String> result = new ArrayList<>();
        for (String blockId : source) {
            String displayName = getBlockDisplayName(blockId).toLowerCase();
            String blockEnglishId = blockId.toLowerCase();

            if (blockEnglishId.replace("minecraft:", "").contains(cleanSearch) || displayName.contains(cleanSearch)) {
                result.add(blockId);
                continue;
            }
            List<String> fullPinyins = fullPinyinCache.get(blockId);
            if (fullPinyins != null) {
                for (String fullPinyin : fullPinyins) {
                    if (fullPinyin.contains(cleanSearch)) { result.add(blockId); break; }
                }
                if (result.contains(blockId)) continue;
            }
            List<String> initials = initialPinyinCache.get(blockId);
            if (initials != null) {
                for (String initial : initials) {
                    if (initial.contains(cleanSearch)) { result.add(blockId); break; }
                }
            }
        }
        return result;
    }

    /**
     * 生成全拼候选。返回多个候选以兼容 ü 的两种常见输入习惯，
     * 例如「绿宝石」同时产出 lvbaoshi 与 lubaoshi，用户输入任一种都能命中。
     */
    private List<String> getAllFullPinyins(String chinese) {
        String primary = buildPinyin(chinese, false);
        String alias = buildPinyin(chinese, true);
        if (alias.equals(primary)) return Collections.singletonList(primary);
        List<String> results = new ArrayList<>(2);
        results.add(primary);
        results.add(alias);
        return results;
    }

    private List<String> getAllInitials(String chinese) {
        String primary = buildPinyin(chinese, false, true);
        String alias = buildPinyin(chinese, true, true);
        if (alias.equals(primary)) return Collections.singletonList(primary);
        List<String> results = new ArrayList<>(2);
        results.add(primary);
        results.add(alias);
        return results;
    }

    private String buildPinyin(String chinese, boolean useUmlautAlias) {
        return buildPinyin(chinese, useUmlautAlias, false);
    }

    /**
     * @param useUmlautAlias true 时把 ü 记作 u（绿->lu），false 时记作 v（绿->lv）
     * @param initialOnly    只取每个字的首字母
     */
    private String buildPinyin(String chinese, boolean useUmlautAlias, boolean initialOnly) {
        StringBuilder builder = new StringBuilder(chinese.length());
        for (char c : chinese.toCharArray()) {
            String pinyin = toPinyin(c);
            if (pinyin == null) {
                builder.append(c);
                continue;
            }
            if (useUmlautAlias) pinyin = pinyin.replace('v', 'u');
            builder.append(initialOnly ? pinyin.charAt(0) : pinyin);
        }
        return builder.toString();
    }

    /**
     * 取单个汉字的拼音（小写、无声调）；非汉字返回 null。
     * 底层库返回大写拼音，其中 ü 记作 v（如 绿->LV），此处统一转小写。
     */
    private static String toPinyin(char c) {
        if (c < '\u4e00' || c > '\u9fa5') return null;
        String pinyin = Pinyin.toPinyin(c);
        if (pinyin == null || pinyin.isEmpty()) return null;
        return pinyin.toLowerCase(Locale.ROOT);
    }

    private String getBlockDisplayName(String blockId) {
        try {
            String[] parts = blockId.split(":");
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            ItemStack stack = new ItemStack(BuiltInRegistries.BLOCK.get(rl));
            return stack.getHoverName().getString();
        } catch (Exception e) { return blockId; }
    }

    private Block getBlockFromId(String blockId) {
        try {
            String[] parts = blockId.split(":");
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            return ForgeRegistries.BLOCKS.getValue(rl);
        } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private String getDefaultPropertyValue(Block block, Property<?> prop) {
        try {
            BlockState defaultState = block.defaultBlockState();
            Object value = defaultState.getValue(prop);
            return value.toString();
        } catch (Exception e) {
            return prop.getPossibleValues().iterator().next().toString();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void cyclePropertyValue(String blockId, String propName) {
        Block block = getBlockFromId(blockId);
        if (block == null) return;
        Property targetProp = null;
        for (Property<?> p : block.getStateDefinition().getProperties()) {
            if (p.getName().equals(propName)) { targetProp = p; break; }
        }
        if (targetProp == null) return;

        List<String> values = new ArrayList<>();
        for (Object v : targetProp.getPossibleValues()) values.add(v.toString());
        if (values.isEmpty()) return;

        Map<String, String> props = blockProperties.computeIfAbsent(blockId, k -> new HashMap<>());
        String current = props.getOrDefault(propName, values.get(0));
        int idx = values.indexOf(current);
        if (idx < 0) idx = 0;
        idx = (idx + 1) % values.size();
        props.put(propName, values.get(idx));
        refreshPreview();
    }

    private String formatBlockWithProperties(String blockId) {
        return formatBlockWithProperties(blockId, null);
    }

    /** weightPrefix 为 null 时不输出百分比前缀。 */
    private String formatBlockWithProperties(String blockId, String weightPrefix) {
        Map<String, String> props = blockProperties.get(blockId);
        StringBuilder sb = new StringBuilder();
        if (weightPrefix != null && !weightPrefix.isEmpty()) {
            sb.append(weightPrefix);
        }
        sb.append(blockId);
        if (props != null && !props.isEmpty()) {
            sb.append("[");
            boolean first = true;
            for (Map.Entry<String, String> entry : props.entrySet()) {
                if (!first) sb.append(",");
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * 合并方块列表并决定要不要带权重前缀：
     * 全部权重相等（全默认，或都调成同一个值）时输出最干净的 //set a,b,c，不带任何百分比；
     * 权重不等时带百分比前缀，比例按最大余数法归一到合计 100%；
     * 悬殊到整数百分比表达不了（会算出 0%）时退回原始相对权重。
     */
    /**
     * 全部方块都是默认比例（100）时输出去掉百分比，交给 WorldEdit 等概率随机；
     * 只要有方块改过比例，就把各自的数值原样带上，不做归一化——你输入什么，指令里就是什么。
     */
    private String joinBlocksWithProperties(List<String> blockIds) {
        if (blockIds == null || blockIds.isEmpty()) return "";
        boolean equal = allWeightsEqual(blockIds);
        List<String> formatted = new ArrayList<>();
        for (String id : blockIds) {
            String prefix = equal ? null : blockWeights.getOrDefault(id, 100) + "%";
            formatted.add(formatBlockWithProperties(id, prefix));
        }
        return String.join(",", formatted);
    }

    private boolean allWeightsEqual(List<String> group) {
        if (group == null || group.isEmpty()) return true;
        int first = blockWeights.getOrDefault(group.get(0), 100);
        for (String id : group) {
            if (blockWeights.getOrDefault(id, 100) != first) return false;
        }
        return true;
    }




    /** 按当前选择拼出默认的指令/ID 文本（不含预览框里的人工修改）。 */
    private String buildDefaultOutput() {
        if (isReplaceMode) {
            String src = joinBlocksWithoutWeight(selectedSources);
            String tgt = joinBlocksWithProperties(selectedTargets);
            if (src.isEmpty() || tgt.isEmpty()) return "";
            return isCommandMode ? ("//replace " + src + " " + tgt) : (src + " " + tgt);
        }
        String blocks = joinBlocksWithProperties(selectedIds);
        if (blocks.isEmpty()) return "";
        return isCommandMode ? ("//set " + blocks) : blocks;
    }

    /** 选择或比例变化后刷新预览框。 */
    private void refreshPreview() {
        if (previewField == null) return;
        previewField.setValue(buildDefaultOutput());
        validatePreview();
    }

    /** 校验预览框当前内容；不合法时置红字提示。 */
    private boolean validatePreview() {
        if (previewField == null) { previewError = null; return true; }
        String text = previewField.getValue().trim();
        if (text.isEmpty()) { previewError = null; return true; }
        String err = validateOutput(text);
        previewError = (err == null) ? null : "\u5f53\u524d\u6307\u4ee4\u683c\u5f0f/\u65b9\u5757ID\u4e0d\u6b63\u786e";
        return err == null;
    }

    /** 校验完整输出文本；返回 null 表示通过，否则返回具体原因。 */
    private String validateOutput(String text) {
        if (text == null || text.isEmpty()) return "\u6ca1\u6709\u5185\u5bb9\u53ef\u8f93\u51fa";
        String body = text;
        if (isCommandMode) {
            if (body.startsWith("//set ")) {
                body = body.substring(6);
            } else if (body.startsWith("//replace ")) {
                body = body.substring(10);
                int sp = body.indexOf(' ');
                if (sp < 0) return "//replace \u9700\u8981\u300c\u6e90\u65b9\u5757 \u76ee\u6807\u65b9\u5757\u300d\u4e24\u6bb5";
                String e1 = validateGenerated(body.substring(0, sp).trim());
                if (e1 != null) return "\u6e90\u65b9\u5757\uff1a" + e1;
                body = body.substring(sp + 1).trim();
            } else {
                return "\u6307\u4ee4\u9700\u4ee5 //set \u6216 //replace \u5f00\u5934";
            }
        } else if (isReplaceMode) {
            // ID 模式下替换结果是「源方块组 目标方块组」两段，按第一个空格拆开分别校验
            int sp = body.indexOf(' ');
            if (sp < 0) return "替换结果需要「源方块 目标方块」两段";
            String errSrc = validateGenerated(body.substring(0, sp).trim());
            if (errSrc != null) return "源方块：" + errSrc;
            body = body.substring(sp + 1).trim();
        }
        return validateGenerated(body.trim());
    }

    /** 当前要复制/执行的内容：以预览框为准；校验不通过返回 null，并在动作栏说明原因。 */
    private String currentOutput() {
        if (previewField == null) return null;
        String text = previewField.getValue().trim();
        if (text.isEmpty()) {
            previewError = null;
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
            }
            return null;
        }
        String err = validateOutput(text);
        previewError = (err == null) ? null : "\u5f53\u524d\u6307\u4ee4\u683c\u5f0f/\u65b9\u5757ID\u4e0d\u6b63\u786e";
        if (err != null) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                        Component.literal("\u3010Block_ID\u3011" + err), true);
            }
            return null;
        }
        return text;
    }

    /** 校验生成的方块串能否被 WorldEdit 正确解析；返回 null 表示通过。 */
    private String validateGenerated(String generated) {
        if (generated == null || generated.isEmpty()) return "\u6ca1\u6709\u751f\u6210\u5185\u5bb9";
        List<String> entries = splitTopLevel(generated);
        boolean withWeight = false, withoutWeight = false;
        for (String entry : entries) {
            String s = entry.trim();
            if (s.isEmpty()) return "\u5b58\u5728\u7a7a\u6761\u76ee";
            int pctIdx = s.indexOf('%');
            if (pctIdx >= 0) {
                String num = s.substring(0, pctIdx);
                if (!num.matches("\\d{1,7}")) return "\u6743\u91cd\u4e0d\u662f\u6570\u5b57\uff1a" + s;
                if (Integer.parseInt(num) <= 0) return "\u6743\u91cd\u5fc5\u987b\u5927\u4e8e 0\uff1a" + s;
                withWeight = true;
                s = s.substring(pctIdx + 1);
            } else {
                withoutWeight = true;
            }
            int bracket = s.indexOf('[');
            String idPart = bracket >= 0 ? s.substring(0, bracket) : s;
            if (!idPart.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return "\u65b9\u5757 ID \u4e0d\u5408\u6cd5\uff1a" + idPart;
            if (bracket >= 0) {
                if (!s.endsWith("]")) return "\u65b9\u5757\u72b6\u6001\u7f3a\u5c11\u53f3\u62ec\u53f7\uff1a" + s;
                String body = s.substring(bracket + 1, s.length() - 1);
                if (body.isEmpty()) return "\u65b9\u5757\u72b6\u6001\u4e3a\u7a7a\uff1a" + s;
                for (String kv : body.split(",")) {
                    if (!kv.matches("[a-z0-9_]+=[a-z0-9_.-]+")) return "\u65b9\u5757\u72b6\u6001\u683c\u5f0f\u4e0d\u5bf9\uff1a" + kv;
                }
            }
        }
        if (withWeight && withoutWeight) return "\u90e8\u5206\u5e26\u6743\u91cd\u3001\u90e8\u5206\u4e0d\u5e26\uff0cWorldEdit \u4f1a\u6309\u9519\u8bef\u6bd4\u4f8b\u751f\u6210";
        return null;
    }

    /** 按顶层逗号切分，方括号里的逗号（方块状态）不参与切分。 */
    private List<String> splitTopLevel(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            if (c == ',' && depth == 0) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }

    private String joinBlocksWithoutWeight(List<String> blockIds) {
        List<String> formatted = new ArrayList<>();
        for (String id : blockIds) formatted.add(formatBlockWithProperties(id, null));
        return String.join(",", formatted);
    }

    
    private int totalWeight(List<String> group) {
        if (group == null || group.isEmpty()) return 0;
        int t = 0;
        for (String id : group) t += blockWeights.getOrDefault(id, 100);
        return t;
    }



    private void setWeight(List<String> blockIds, String targetId, int newWeight) {
        if (blockIds.size() == 1) { blockWeights.put(targetId, 100); return; }
        newWeight = Math.max(1, Math.min(1000000, newWeight));
        blockWeights.put(targetId, newWeight);
    }

    private List<String> getActiveSelectedList() {
        if (isReplaceMode) {
            
            if (editingBlockId != null && selectedSources.contains(editingBlockId)) return selectedSources;
            return selectedTargets;
        }
        return selectedIds;
    }

    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isReplaceMode) {
            int tabY = listHeaderY;
            int tabW = colWidth / 2;
            if (mouseY >= tabY && mouseY <= tabY + 12) {
                if (mouseX >= midX && mouseX <= midX + tabW) {
                    midTabMode = 0; return true;
                }
                if (mouseX >= midX + tabW && mouseX <= midX + colWidth) {
                    midTabMode = 1; return true;
                }
            }
            if (midTabMode == 0) {
                int groupsSize = FavoritesManager.getGroups().size();
                int curIdx = FavoritesManager.getCurrentGroupIndex();
                int nameY = listHeaderY + 16;
                int btnW = 20;
                int btnH = 14;
                int gap = 4;
                int step = btnW + gap;
                int btnY = listHeaderY + 30;
                boolean[] visible = {curIdx > 0, curIdx < groupsSize - 1, true, true, groupsSize > 1};
                if (mouseY >= btnY && mouseY <= btnY + btnH) {
                    int bx = midX;
                    for (int i = 0; i < 5; i++) {
                        if (!visible[i]) continue;
                        if (mouseX >= bx && mouseX <= bx + btnW) {
                            if (i == 0) { FavoritesManager.setCurrentGroupIndex(curIdx - 1); }
                            else if (i == 1) { FavoritesManager.setCurrentGroupIndex(curIdx + 1); }
                            else if (i == 2) { FavoritesManager.addGroup("新组" + (groupsSize + 1)); }
                            else if (i == 3) {
                                if (renameGroupField == null) {
                                    renameGroupField = new EditBox(this.font, midX, listHeaderY + 14, colWidth - 100, 14, Component.literal(""));
                                    renameGroupField.setMaxLength(16);
                                    renameGroupField.setValue(FavoritesManager.getCurrentGroup().name);
                                    renameGroupField.setFocused(true);
                                    this.setFocused(renameGroupField);
                                } else {
                                    String newName = renameGroupField.getValue();
                                    if (newName != null && !newName.isEmpty()) {
                                        FavoritesManager.renameGroup(curIdx, newName);
                                        this.minecraft.player.displayClientMessage(Component.literal("组已重命名为: " + newName), true);
                                    }
                                    renameGroupField = null;
                                }
                                return true;
                            }
                            else if (i == 4) { FavoritesManager.removeGroup(curIdx); }
                            favoriteBlocks = FavoritesManager.getCurrentBlocks();
                            cachedFilteredFavorite = filterBlocks(favoriteBlocks, searchBar.getValue());
                            return true;
                        }
                        bx += step;
                    }
                }
            }
            int pageBtnY = listEndY + 2;
            int pageBtnW = 50;
            if (mouseY >= pageBtnY && mouseY <= pageBtnY + 18) {
                if (mouseX >= leftX && mouseX <= leftX + pageBtnW && currentPage > 0) {
                    currentPage--; scrollAll = 0; return true;
                }
                if (mouseX >= leftX + colWidth - pageBtnW && mouseX <= leftX + colWidth && currentPage < getTotalPages() - 1) {
                    currentPage++; scrollAll = 0; return true;
                }
            }
            if (midTabMode == 1 && mouseX >= midX && mouseX <= midX + colWidth) {
                int y = listStartY - scrollHistory;
                for (int i = 0; i < historyItems.size(); i++) {
                    if (mouseY >= y && mouseY <= Math.min(y + ITEM_HEIGHT - 4, listEndY)) {
                        HistoryManager.HistoryItem item = historyItems.get(i);
                        this.minecraft.keyboardHandler.setClipboard(item.content);
                        this.minecraft.player.displayClientMessage(Component.literal("已复制历史记录: " + item.content), true);
                        return true;
                    }
                    y += ITEM_HEIGHT;
                }
            }
        }
        
        if (editingBlockId != null) {
            int[] b = getPropertyPanelBounds();
            int panelX = b[0], panelY = b[1], panelW = b[2], panelH = b[3], contentH = b[4];
            int headerH = 18;

            if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
                
                int closeX = panelX + panelW - 36;
                int closeY = panelY + 3;
                if (mouseX >= closeX && mouseX <= closeX + 30 && mouseY >= closeY && mouseY <= closeY + 13) {
                    editingBlockId = null; propScroll = 0; return true;
                }
                
                int copyX = panelX + panelW - 74;
                int copyY = panelY + 3;
                if (mouseX >= copyX && mouseX <= copyX + 34 && mouseY >= copyY && mouseY <= copyY + 13) {
                    String formatted = formatBlockWithProperties(editingBlockId);
                    this.minecraft.keyboardHandler.setClipboard(formatted);
                    this.minecraft.player.displayClientMessage(Component.literal("已复制: " + formatted), true);
                    return true;
                }
                
                int contentY = panelY + headerH + 3;
                int lineH = 13;
                if (mouseY >= contentY && mouseY <= contentY + contentH) {
                    Block block = getBlockFromId(editingBlockId);
                    if (block != null) {
                        List<Property<?>> properties = new ArrayList<>(block.getStateDefinition().getProperties());
                        int idx = (int)((mouseY - contentY + propScroll) / lineH);
                        if (idx >= 0 && idx < properties.size()) {
                            cyclePropertyValue(editingBlockId, properties.get(idx).getName());
                            return true;
                        }
                    }
                }
                return true;
            }
        }

        if (this.searchBar.isMouseOver(mouseX, mouseY)) {
            this.searchBar.mouseClicked(mouseX, mouseY, button);
            this.setFocused(this.searchBar);
            return true;
        }
        if (isReplaceMode && this.targetSearchBar.isMouseOver(mouseX, mouseY)) {
            this.targetSearchBar.mouseClicked(mouseX, mouseY, button);
            this.setFocused(this.targetSearchBar);
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        
        if (mouseX >= leftX && mouseX <= leftX + colWidth) {
            int y = listStartY - (isReplaceMode ? scrollSrc : scrollAll);
            List<String> leftList = isReplaceMode ? cachedFilteredAll : getCurrentPageBlocks();
            for (String blockId : leftList) {
                if (mouseY >= y && mouseY <= Math.min(y + ITEM_HEIGHT - 4, listEndY)) {
                    if (mouseX >= leftX + colWidth - 16 && mouseX <= leftX + colWidth) toggleFavorite(blockId);
                    else {
                        if (isReplaceMode) toggleSelection(selectedSources, blockId);
                        else toggleSelection(selectedIds, blockId);
                    }
                    return true;
                }
                y += ITEM_HEIGHT;
            }
        }

        
        if (mouseX >= midX && mouseX <= midX + colWidth) {
            List<String> targetList;
            int scroll;
            int listOffset = 0;
            if (midTabMode == 0) {
                int nameY = listHeaderY + 16;
                int btnH = 14;
                int btnY = listHeaderY + 30;
                int lineY = listHeaderY + 48;
                listOffset = 40;
            }
            if (isReplaceMode) {
                targetList = targetTabMode ? cachedFilteredFavorite : cachedFilteredTarget;
                scroll = targetTabMode ? scrollFavorite : scrollTgt;
                if (!targetTabMode) listOffset = 0;
            } else {
                targetList = cachedFilteredFavorite;
                scroll = scrollFavorite;
            }

            int y = listStartY + listOffset - scroll;
            for (String blockId : targetList) {
                if (mouseY >= y && mouseY <= Math.min(y + ITEM_HEIGHT - 4, listEndY)) {
                    if (mouseX >= midX + colWidth - 16 && mouseX <= midX + colWidth) toggleFavorite(blockId);
                    else {
                        if (isReplaceMode) toggleSelection(selectedTargets, blockId);
                        else toggleSelection(selectedIds, blockId);
                    }
                    return true;
                }
                y += ITEM_HEIGHT;
            }
        }

        
        if (mouseX >= rightX && mouseX <= rightX + colWidth) {
            if (isReplaceMode) {
                int ySrc = listStartY - scrollSelSrc;
                for (String blockId : selectedSources) {
                    if (mouseY >= ySrc && mouseY <= Math.min(ySrc + ITEM_HEIGHT - 4, listEndY)) {
                        int relX = (int) (mouseX - rightX);
                        if (relX >= colWidth - 12) {
                            editingBlockId = blockId; propScroll = 0;
                        }
                        return true;
                    }
                    ySrc += ITEM_HEIGHT;
                }

                int yTgt = rightLineY + 10 - scrollSelTgt;
                for (String blockId : selectedTargets) {
                    if (mouseY >= yTgt && mouseY <= Math.min(yTgt + ITEM_HEIGHT - 4, listEndY)) {
                        int relX = (int) (mouseX - rightX);
                        if (relX >= colWidth - 12) {
                            editingBlockId = blockId; propScroll = 0;
                        } else if (relX >= colWidth - 68 && relX <= colWidth - 20) {
                            openWeightInput(blockId, selectedTargets);
                        }
                        
                        return true;
                    }
                    yTgt += ITEM_HEIGHT;
                }
            } else {
                int y = listStartY - scrollSelected;
                for (String blockId : selectedIds) {
                    if (mouseY >= y && mouseY <= Math.min(y + ITEM_HEIGHT - 4, listEndY)) {
                        int relX = (int) (mouseX - rightX);
                        if (relX >= colWidth - 12) {
                            editingBlockId = blockId; propScroll = 0;
                        } else if (relX >= colWidth - 68 && relX <= colWidth - 20) {
                            openWeightInput(blockId, selectedIds);
                        }
                        
                        return true;
                    }
                    y += ITEM_HEIGHT;
                }
            }
        }
        return false;
    }

    /**
     * 打开权重输入框。显示的是「这个方块占多少百分比」，输入框预填当前百分比，
     * 用户直接改数字即可（也可以带 % 号，带不带都认）。
     */
    private void openWeightInput(String blockId, List<String> group) {
        weightInputBlockId = blockId;
        weightInputGroup = group;
        int pct = blockWeights.getOrDefault(blockId, 100);
        if (weightInputField == null) {
            weightInputField = new EditBox(this.font, 0, 0, 40, 14, Component.literal(""));
            weightInputField.setMaxLength(5);
            weightInputField.setFilter(this::isValidPercentText);
        }
        weightInputField.setValue(String.valueOf(pct));
        weightInputField.setFocused(true);
        this.setFocused(weightInputField);
    }


    /** 允许空串、数字，以及数字后跟一个百分号（半角或全角）；其余一律拦下。 */
    private boolean isValidPercentText(String text) {
        if (text == null || text.isEmpty()) return true;
        String t = text.trim().replace('\uFF05', '%');
        if (t.endsWith("%")) t = t.substring(0, t.length() - 1);
        return t.matches("\\d{0,3}");
    }

    private int[] getPropertyPanelBounds() {
        Block block = getBlockFromId(editingBlockId);
        int propCount = (block != null) ? block.getStateDefinition().getProperties().size() : 0;
        int headerH = 18, lineH = 13, padding = 10;
        int contentH = Math.min(propCount * lineH, 5 * lineH);
        int panelH = headerH + contentH + padding;
        int panelW = 230;
        int panelX = margin;
        int panelY = this.height - panelH - 5;
        return new int[]{panelX, panelY, panelW, panelH, contentH};
    }

    private void confirmWeightInput() {
        if (weightInputBlockId == null || weightInputField == null) return;
        String raw = weightInputField.getValue().replace('\uFF05', '%').replace("%", "").trim();
        List<String> group = weightInputGroup != null ? weightInputGroup
                : (isReplaceMode ? selectedTargets : selectedIds);
        if (group != null && !group.isEmpty()) {
            if (raw.isEmpty() || group.size() == 1) {
                blockWeights.put(weightInputBlockId, 100);
            } else {
                try {
                    // 输入什么就存什么：比例是相对权重，不需要凑满 100，也不做二次换算
                    int pct = Integer.parseInt(raw);
                    blockWeights.put(weightInputBlockId, Math.max(1, Math.min(100, pct)));
                } catch (NumberFormatException ignored) {}
            }
        }
        weightInputBlockId = null;
        weightInputGroup = null;
        weightInputField.setFocused(false);
        refreshPreview();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        
        if (editingBlockId != null) {
            int[] b = getPropertyPanelBounds();
            if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
                propScroll -= delta * 13;
                propScroll = Math.max(0, propScroll);
                return true;
            }
        }

        if (mouseX >= leftX && mouseX < leftX + colWidth) {
            
            int maxScroll = isReplaceMode
                    ? getMaxScroll(cachedFilteredAll.size(), listEndY - listStartY)
                    : getMaxScroll(getCurrentPageBlocks().size(), listEndY - listStartY);
            if (isReplaceMode) {
                scrollSrc -= delta * 20; scrollSrc = Math.max(0, Math.min(maxScroll, scrollSrc));
            } else {
                scrollAll -= delta * 20; scrollAll = Math.max(0, Math.min(maxScroll, scrollAll));
            }
            return true;
        } else if (mouseX >= midX && mouseX < midX + colWidth) {
            if (isReplaceMode) {
                if (targetTabMode) {
                    int maxScroll = getMaxScroll(cachedFilteredFavorite.size(), listEndY - listStartY - 52);
                    scrollFavorite -= delta * 20; scrollFavorite = Math.max(0, Math.min(maxScroll, scrollFavorite));
                } else {
                    
                    int maxScroll = getMaxScroll(cachedFilteredTarget.size(), listEndY - listStartY);
                    scrollTgt -= delta * 20; scrollTgt = Math.max(0, Math.min(maxScroll, scrollTgt));
                }
            } else if (midTabMode == 1) {
                
                int maxScroll = getMaxScroll(historyItems.size(), listEndY - listStartY);
                scrollHistory -= delta * 20; scrollHistory = Math.max(0, Math.min(maxScroll, scrollHistory));
            } else {
                int maxScroll = getMaxScroll(cachedFilteredFavorite.size(), listEndY - listStartY - 52);
                scrollFavorite -= delta * 20; scrollFavorite = Math.max(0, Math.min(maxScroll, scrollFavorite));
            }
            return true;
        } else if (mouseX >= rightX && mouseX < rightX + colWidth) {
            if (isReplaceMode) {
                
                if (mouseY < rightLineY) {
                    
                    int srcHeight = rightLineY - 5 - listStartY;
                    int maxScroll = getMaxScroll(selectedSources.size(), srcHeight);
                    scrollSelSrc -= delta * 20; scrollSelSrc = Math.max(0, Math.min(maxScroll, scrollSelSrc));
                } else {
                    
                    int tgtHeight = listEndY - (rightLineY + 10);
                    int maxScroll = getMaxScroll(selectedTargets.size(), tgtHeight);
                    scrollSelTgt -= delta * 20; scrollSelTgt = Math.max(0, Math.min(maxScroll, scrollSelTgt));
                }
            } else {
                int maxScroll = getMaxScroll(selectedIds.size(), listEndY - listStartY);
                scrollSelected -= delta * 20; scrollSelected = Math.max(0, Math.min(maxScroll, scrollSelected));
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void toggleFavorite(String blockId) {
        if (FavoritesManager.isInCurrentGroup(blockId)) {
            FavoritesManager.removeBlock(blockId);
        } else {
            FavoritesManager.addBlock(blockId);
        }
        favoriteBlocks = FavoritesManager.getCurrentBlocks();
        cachedFilteredFavorite = filterBlocks(favoriteBlocks, searchBar.getValue());
    }
    private void toggleSelection(List<String> list, String blockId) {
        if (list.contains(blockId)) {
            list.remove(blockId);
            blockProperties.remove(blockId);
            blockWeights.remove(blockId);
        } else {
            list.add(blockId);
            blockWeights.put(blockId, 100);
        }
        refreshPreview();
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) cachedFilteredAll.size() / ITEMS_PER_PAGE));
    }

    private List<String> getCurrentPageBlocks() {
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, cachedFilteredAll.size());
        if (start >= cachedFilteredAll.size()) {
            currentPage = 0;
            start = 0;
            end = Math.min(ITEMS_PER_PAGE, cachedFilteredAll.size());
        }
        return new ArrayList<>(cachedFilteredAll.subList(start, end));
    }

    private void renderHistoryList(GuiGraphics graphics, int x, int y) {
        for (int i = 0; i < historyItems.size(); i++) {
            int itemY = y + i * ITEM_HEIGHT;
            if (itemY < listStartY - ITEM_HEIGHT || itemY > listEndY) continue;
            HistoryManager.HistoryItem item = historyItems.get(i);
            graphics.fill(x, itemY, x + colWidth, itemY + ITEM_HEIGHT - 2, 0xAA000000);
            String modeText = item.mode.equals("id") ? "[ID]" : item.mode.equals("set") ? "[SET]" : "[REP]";
            String[] ids = item.content.split(",");
            StringBuilder names = new StringBuilder();
            for (String id : ids) {
                id = id.trim();
                if (id.contains("%")) id = id.substring(id.indexOf("%") + 1);
                if (id.contains(" ")) id = id.substring(id.lastIndexOf(" ") + 1);
                String name = getBlockDisplayName(id);
                if (name != null && !name.isEmpty()) names.append(name).append(", ");
                else names.append(id).append(", ");
            }
            String nameStr = names.toString();
            if (nameStr.endsWith(", ")) nameStr = nameStr.substring(0, nameStr.length() - 2);
            String display = modeText + " " + nameStr;
            if (display.length() > 28) display = display.substring(0, 28) + "...";
            graphics.drawString(this.font, display, x + 4, itemY + 6, 0xFFAAAAAA);
        }
    }

    private void addToHistory(String content, String mode) {
        HistoryManager.addRecord(content, mode);
        historyItems = HistoryManager.getHistory();
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
