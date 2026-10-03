package com.moybj.blockid;

import com.github.promeg.pinyinhelper.Pinyin;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
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
    private String weightInputBlockId = null;
    
    private String draggingWeightBlockId = null;

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
        listEndY = this.height - 105;
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
            this.addRenderableWidget(new Button(midX, tabY, colWidth / 2, tabH, Component.literal("目标方块"), b -> {
                this.targetTabMode = false;
            }));

            this.addRenderableWidget(new Button(midX + colWidth / 2, tabY, colWidth / 2, tabH, Component.literal("收藏夹"), b -> {
                this.targetTabMode = true;
            }));
        }

        String leftBtnText = isCommandMode ? "切换到ID复制" : "切换到指令模式";
        this.addRenderableWidget(new Button(centerX - 105, toggleBtnY, 100, 20, Component.literal(leftBtnText), b -> {
            BlockIdScreen.isCommandMode = !BlockIdScreen.isCommandMode;
            this.minecraft.setScreen(new BlockIdScreen());
        }));

        String rightBtnText = isReplaceMode ? "切换到复制模式" : "切换到替换模式";
        this.addRenderableWidget(new Button(centerX + 5, toggleBtnY, 100, 20, Component.literal(rightBtnText), b -> {
            BlockIdScreen.isReplaceMode = !BlockIdScreen.isReplaceMode;
            this.minecraft.setScreen(new BlockIdScreen());
        }));

        if (!isCommandMode) {
            this.addRenderableWidget(new Button(centerX - 105, bottomBtnY, 100, 20, Component.literal("复制ID"), b -> {
                if (isReplaceMode) {
                    if (!selectedSources.isEmpty() && !selectedTargets.isEmpty()) {
                        String source = joinBlocksWithoutWeight(selectedSources);
                        String target = joinBlocksWithProperties(selectedTargets);
                        String finalString = source + " " + target;
                        this.minecraft.keyboardHandler.setClipboard(finalString);
                        this.minecraft.player.displayClientMessage(Component.literal("已复制替换ID: " + finalString), true);
                    } else {
                        this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
                    }
                } else {
                    if (!selectedIds.isEmpty()) {
                        String id = joinBlocksWithProperties(selectedIds);
                        this.minecraft.keyboardHandler.setClipboard(id);
                        this.minecraft.player.displayClientMessage(Component.literal("已复制方块ID: " + id), true);
                    } else {
                        this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
                    }
                }
            }));
        } else {
            if (!isReplaceMode) {
                this.addRenderableWidget(new Button(centerX - 105, bottomBtnY, 100, 20, Component.literal("复制 (Set)"), b -> {
                    if (!selectedIds.isEmpty()) {
                        String setCmd = joinBlocksWithProperties(selectedIds); WorldEditIntegration.copySetCommand(setCmd); addToHistory(setCmd, "set");
                    } else {
                        this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
                    }
                }));
            } else {
                this.addRenderableWidget(new Button(centerX - 105, bottomBtnY, 100, 20, Component.literal("复制 (Replace)"), b -> {
                    if (!selectedSources.isEmpty() && !selectedTargets.isEmpty()) {
                        String repSrc = joinBlocksWithoutWeight(selectedSources); String repTgt = joinBlocksWithProperties(selectedTargets); WorldEditIntegration.copyReplaceCommand(repSrc, repTgt); addToHistory(repSrc + " " + repTgt, "replace");
                    } else {
                        this.minecraft.player.displayClientMessage(Component.translatable("gui.block_id.please_select"), true);
                    }
                }));
            }
        }

        this.addRenderableWidget(new Button(centerX + 5, bottomBtnY, 100, 20, Component.literal("清空"), b -> {
            selectedIds.clear();
            selectedSources.clear();
            selectedTargets.clear();
            blockProperties.clear();
            blockWeights.clear();
            editingBlockId = null;
        }));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (weightInputBlockId != null && weightInputField != null && weightInputField.isFocused()) {
            if (keyCode == 257 || keyCode == 335) { confirmWeightInput(); return true; }
            if (keyCode == 256) { weightInputBlockId = null; weightInputField.setFocus(false); return true; }
            return weightInputField.keyPressed(keyCode, scanCode, modifiers);
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

    private void renderScrollbar(PoseStack graphics, int x, int y, int height, int scroll, int totalContent) {
        int contentHeight = totalContent * ITEM_HEIGHT;
        if (contentHeight <= height) return;

        fill(graphics, x, y, x + scrollBarWidth, y + height, 0x80000000);
        float visibleRatio = (float) height / contentHeight;
        float sliderHeight = Math.max(20, height * visibleRatio);
        sliderHeight = Math.min(sliderHeight, height);

        int maxScroll = getMaxScroll(totalContent, height);
        float sliderY = y;
        if (maxScroll > 0) {
            sliderY = y + (float) scroll / maxScroll * (height - sliderHeight);
            sliderY = Math.max(y, Math.min(y + height - sliderHeight, sliderY));
        }
        fill(graphics, x, (int) sliderY, x + scrollBarWidth, (int) (sliderY + sliderHeight), 0xCCFFFFFF);
    }

    private boolean handleScrollbarClick(int x, int y, int height, int scroll, int totalContent, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + scrollBarWidth && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        fillGradient(graphics, 0, 0, this.width, this.height, 0xE0202020, 0xF0101010);
        fillGradient(graphics, 0, 0, this.width, 60, 0xFF3a6ea5, 0x00000000);
        RenderSystem.disableBlend();

        this.font.draw(graphics, Component.translatable("gui.block_id.title"), this.width / 2 - this.font.width(Component.translatable("gui.block_id.title")) / 2, 15, 0xFFFFFFFF);

        
        int panelBg = 0x90000000;
        int panelBorder = 0xFF3a6ea5;
        fill(graphics, leftX - 3, listStartY - 3, leftX + colWidth + scrollBarWidth + 3, listEndY + 3, panelBg);
        fill(graphics, midX - 3, listStartY - 3, midX + colWidth + scrollBarWidth + 3, listEndY + 3, panelBg);
        fill(graphics, rightX - 3, listStartY - 3, rightX + colWidth + 3, listEndY + 3, panelBg);
        
        fill(graphics, leftX - 3, listStartY - 3, leftX + colWidth + scrollBarWidth + 3, listStartY - 2, panelBorder);
        fill(graphics, midX - 3, listStartY - 3, midX + colWidth + scrollBarWidth + 3, listStartY - 2, panelBorder);
        fill(graphics, rightX - 3, listStartY - 3, rightX + colWidth + 3, listStartY - 2, panelBorder);

        super.render(graphics, mouseX, mouseY, partialTick);

        this.searchBar.render(graphics, mouseX, mouseY, partialTick);
        if (isReplaceMode) this.targetSearchBar.render(graphics, mouseX, mouseY, partialTick);

        
        if (this.searchBar.getValue().isEmpty() && !this.searchBar.isFocused()) {
            this.font.draw(graphics, "搜索...", this.searchBar.x + 4, this.searchBar.y + 6, 0xFF666666);
        }
        if (isReplaceMode && this.targetSearchBar.getValue().isEmpty() && !this.targetSearchBar.isFocused()) {
            this.font.draw(graphics, "搜索目标...", this.targetSearchBar.x + 4, this.targetSearchBar.y + 6, 0xFF666666);
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
            this.font.draw(graphics, "替换源", leftX, listHeaderY, 0xFFAAAAAA);
            enableScissor(leftX - 5, listStartY, leftX + colWidth, listEndY + 10);
            renderList(graphics, leftX, listStartY - scrollSrc, cachedFilteredAll, selectedSources, 0xCC4CAF50, 0xAA000000, "all");
            disableScissor();
            renderScrollbar(graphics, leftX + colWidth, listStartY, listEndY - listStartY, scrollSrc, cachedFilteredAll.size());

            
            if (targetTabMode) {
                enableScissor(midX - 5, listStartY, midX + colWidth, listEndY + 10);
                renderList(graphics, midX, listStartY - scrollFavorite, favoriteBlocks, selectedTargets, 0xCCFF9800, 0xAA000000, "frequent");
                disableScissor();
                renderScrollbar(graphics, midX + colWidth, listStartY, listEndY - listStartY, scrollFavorite, favoriteBlocks.size());
            } else {
                enableScissor(midX - 5, listStartY, midX + colWidth, listEndY + 10);
                renderList(graphics, midX, listStartY - scrollTgt, cachedFilteredTarget, selectedTargets, 0xCCFF9800, 0xAA000000, "all");
                disableScissor();
                renderScrollbar(graphics, midX + colWidth, listStartY, listEndY - listStartY, scrollTgt, cachedFilteredTarget.size());
            }

            this.font.draw(graphics, "已选源", rightX, listHeaderY, 0xFFAAAAAA);
            enableScissor(rightX - 5, listStartY, rightX + colWidth, rightLineY - 5);
            renderList(graphics, rightX, listStartY - scrollSelSrc, selectedSources, null, 0xCC4CAF50, 0xAA000000, "selected");
            disableScissor();
            fill(graphics, rightX - 5, rightLineY, rightX + colWidth, rightLineY + 2, 0xFF555555);
            this.font.draw(graphics, "已选目标", rightX, rightLineY + 4, 0xFFAAAAAA);
            enableScissor(rightX - 5, rightLineY + 10, rightX + colWidth, listEndY + 10);
            renderList(graphics, rightX, rightLineY + 10 - scrollSelTgt, selectedTargets, null, 0xCCFF9800, 0xAA000000, "selected");
            disableScissor();
        } else {
            this.font.draw(graphics, "全部方块 (第" + (currentPage + 1) + "/" + getTotalPages() + "页)", leftX, listHeaderY, 0xFFAAAAAA);
            List<String> pageBlocks = getCurrentPageBlocks();
            enableScissor(leftX - 5, listStartY, leftX + colWidth, listEndY + 10);
            renderList(graphics, leftX, listStartY - scrollAll, pageBlocks, selectedIds, 0xCC4CAF50, 0xAA000000, "all");
            disableScissor();
            renderScrollbar(graphics, leftX + colWidth, listStartY, listEndY - listStartY, scrollAll, pageBlocks.size());

            int tabW = colWidth / 2; int favColor = midTabMode == 0 ? 0xFF4CAF50 : 0xFF666666; int histColor = midTabMode == 1 ? 0xFF4CAF50 : 0xFF666666; this.font.draw(graphics, "收藏夹", midX, listHeaderY, favColor); this.font.draw(graphics, "历史", midX + tabW, listHeaderY, histColor);
            enableScissor(midX - 5, listStartY, midX + colWidth, listEndY + 10);
            if (midTabMode == 0) { String groupName = FavoritesManager.getCurrentGroup().name; this.font.draw(graphics, "[" + groupName + "]", midX, listHeaderY + 12, 0xFFAAAAAA); renderList(graphics, midX, listStartY - scrollFavorite, favoriteBlocks, selectedIds, 0xCC4CAF50, 0xAA000000, "favorite"); } else { renderHistoryList(graphics, midX, listStartY - scrollHistory); }
            disableScissor();
            renderScrollbar(graphics, midX + colWidth, listStartY, listEndY - listStartY, scrollFavorite, favoriteBlocks.size());

            this.font.draw(graphics, "已选列表", rightX, listHeaderY, 0xFFAAAAAA);
            enableScissor(rightX - 5, listStartY, rightX + colWidth, listEndY + 10);
            renderList(graphics, rightX, listStartY - scrollSelected, selectedIds, null, 0xCCFF9800, 0xAA000000, "selected");
            disableScissor();
            renderScrollbar(graphics, rightX + colWidth, listStartY, listEndY - listStartY, scrollSelected, selectedIds.size());
        }

        int totalSelected = isReplaceMode ? (selectedSources.size() + selectedTargets.size()) : selectedIds.size();
        Component selComp = Component.translatable("gui.block_id.selected_count", totalSelected);
        this.font.draw(graphics, selComp, this.width / 2 - this.font.width(selComp) / 2, tipY, 0xFFFFFFFF);

        
        if (weightInputBlockId != null && weightInputField != null) {
            renderWeightInput(graphics);
        }

        
        if (editingBlockId != null) {
            renderPropertyEditor(graphics, mouseX, mouseY);
        }

        if (!isReplaceMode) {
            int pageBtnY = listEndY + 5;
            int pageBtnW = 50;
            int pageBtnH = 18;
            if (currentPage > 0) {
                fill(graphics, leftX, pageBtnY, leftX + pageBtnW, pageBtnY + pageBtnH, 0xFF4CAF50);
                this.font.draw(graphics, "上一页", leftX + pageBtnW / 2 - this.font.width("上一页") / 2, pageBtnY + 5, 0xFFFFFFFF);
            }
            if (currentPage < getTotalPages() - 1) {
                fill(graphics, leftX + colWidth - pageBtnW, pageBtnY, leftX + colWidth, pageBtnY + pageBtnH, 0xFF4CAF50);
                this.font.draw(graphics, "下一页", leftX + colWidth - pageBtnW / 2 - this.font.width("下一页") / 2, pageBtnY + 5, 0xFFFFFFFF);
            }
        }
    }

    private void renderWeightInput(PoseStack graphics) {
        
        int itemY = -1;
        List<String> activeList = selectedIds;
        int activeScroll = scrollSelected;
        if (isReplaceMode) {
            int srcIdx = selectedSources.indexOf(weightInputBlockId);
            if (srcIdx >= 0) {
                itemY = listStartY + srcIdx * ITEM_HEIGHT - scrollSelSrc;
                activeList = selectedSources; activeScroll = scrollSelSrc;
            } else {
                int tgtIdx = selectedTargets.indexOf(weightInputBlockId);
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

        
        int inputX = rightX + colWidth - 74;
        int inputY = itemY + 3;
        weightInputField.x = inputX;
        weightInputField.y = inputY;
        weightInputField.render(graphics, 0, 0, 0);
    }

    private void renderPropertyEditor(PoseStack graphics, int mouseX, int mouseY) {
        
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

        
        fill(graphics, panelX, panelY, panelX + panelW, panelY + panelH, 0xFF141428);
        fill(graphics, panelX, panelY, panelX + panelW, panelY + 1, 0xFF4a8ad5);
        fill(graphics, panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF4a8ad5);
        fill(graphics, panelX, panelY, panelX + 1, panelY + panelH, 0xFF4a8ad5);
        fill(graphics, panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, 0xFF4a8ad5);

        
        fill(graphics, panelX + 1, panelY + 1, panelX + panelW - 1, panelY + headerH, 0xFF1a2540);
        String blockName = getBlockDisplayName(editingBlockId);
        String title = "⚙ " + blockName;
        if (this.font.width(title) > panelW - 90) {
            
            while (this.font.width(title + "...") > panelW - 90 && title.length() > 1) title = title.substring(0, title.length() - 1);
            title += "...";
        }
        this.font.draw(graphics, title, panelX + 6, panelY + 5, 0xFF88BBFF);

        
        int closeX = panelX + panelW - 36;
        int closeY = panelY + 3;
        fill(graphics, closeX, closeY, closeX + 30, closeY + 13, 0xFF8B2020);
        this.font.draw(graphics, "×", closeX + 15 - this.font.width("×") / 2, closeY + 3, 0xFFFFFFFF);

        
        int copyX = panelX + panelW - 74;
        int copyY = panelY + 3;
        fill(graphics, copyX, copyY, copyX + 34, copyY + 13, 0xFF2a6a3a);
        this.font.draw(graphics, "复制", copyX + 17 - this.font.width("复制") / 2, copyY + 3, 0xFFFFFFFF);

        if (properties.isEmpty()) {
            this.font.draw(graphics, "无可用属性", panelX + panelW / 2 - this.font.width("无可用属性") / 2, panelY + headerH + 8, 0xFF666666);
            return;
        }

        
        int contentY = panelY + headerH + 3;
        int maxScroll = Math.max(0, properties.size() * lineH - contentH);
        propScroll = Math.max(0, Math.min(maxScroll, propScroll));

        enableScissor(panelX + 4, contentY, panelX + panelW - 4, contentY + contentH);
        int y = contentY - propScroll;
        Map<String, String> currentProps = blockProperties.computeIfAbsent(editingBlockId, k -> new HashMap<>());

        for (int i = 0; i < properties.size(); i++) {
            Property<?> prop = properties.get(i);
            if (y + lineH < contentY || y > contentY + contentH) { y += lineH; continue; }

            String propName = prop.getName();
            String currentValue = currentProps.getOrDefault(propName, getDefaultPropertyValue(block, prop));

            int rowBg = (i % 2 == 0) ? 0xFF181830 : 0xFF1c1c36;
            fill(graphics, panelX + 4, y, panelX + panelW - 4, y + lineH - 1, rowBg);
            fill(graphics, panelX + 4, y, panelX + 6, y + lineH - 1, 0xFF4a8ad5);

            
            String cnName = mapPropName(propName);
            this.font.draw(graphics, cnName, panelX + 10, y + 3, 0xFF88BBFF);

            
            String displayValue = mapPropValue(currentValue);
            String valueText = displayValue + " " + currentValue;
            int valueWidth = this.font.width(valueText);
            int valueX = panelX + panelW - valueWidth - 10;
            fill(graphics, valueX - 2, y + 1, valueX + valueWidth + 2, y + lineH - 2, 0xFF252545);
            this.font.draw(graphics, valueText, valueX, y + 3, 0xFFFFFF88);

            y += lineH;
        }
        disableScissor();

        
        if (maxScroll > 0) {
            int sbX = panelX + panelW - 5;
            fill(graphics, sbX, contentY, sbX + 2, contentY + contentH, 0x30FFFFFF);
            float ratio = (float) contentH / (properties.size() * lineH);
            int thumbH = Math.max(12, (int)(contentH * ratio));
            int thumbY = contentY + (int)((float) propScroll / maxScroll * (contentH - thumbH));
            fill(graphics, sbX, thumbY, sbX + 2, thumbY + thumbH, 0x8888BBFF);
        }
    }

    private void renderList(PoseStack graphics, int x, int y, List<String> filtered, List<String> selectionList, int selectedColor, int normalColor, String listKind) {
        for (String blockId : filtered) {
            boolean isSelected = selectionList != null && selectionList.contains(blockId);
            if (isSelected) {
                fill(graphics, x, y, x + colWidth, y + ITEM_HEIGHT - 4, selectedColor);
                fill(graphics, x, y, x + 2, y + ITEM_HEIGHT - 4, 0xFF8BC34A);
            } else {
                fill(graphics, x, y, x + colWidth, y + ITEM_HEIGHT - 4, normalColor);
            }
            
            fill(graphics, x, y + ITEM_HEIGHT - 5, x + colWidth, y + ITEM_HEIGHT - 4, 0xFF222222);

            try {
                String[] parts = blockId.split(":");
                ResourceLocation rl = new ResourceLocation(parts[0], parts[1]);
                ItemStack stack = new ItemStack(Registry.BLOCK.get(rl));
                this.itemRenderer.renderGuiItem(stack, x + 4, y + 2);

                
                if ("selected".equals(listKind)) {
                    Map<String, String> props = blockProperties.get(blockId);
                    int weight = blockWeights.getOrDefault(blockId, 100);
                    String displayText = stack.getHoverName().getString();
                    if (props != null && !props.isEmpty()) displayText += " [" + props.size() + "]";
                    this.font.draw(graphics, displayText, x + 26, y + 6, 0xFFFFFFFF);

                    
                    int trackX = x + colWidth - 78;
                    int trackW = 26;
                    int trackY = y + 10;
                    
                    fill(graphics, trackX, trackY, trackX + trackW, trackY + 3, 0xFF333355);
                    
                    int fillW = (int)((float) trackW * weight / 100);
                    fill(graphics, trackX, trackY, trackX + fillW, trackY + 3, 0xFF4a8ad5);
                    
                    int thumbX = trackX + fillW - 2;
                    fill(graphics, thumbX, trackY - 2, thumbX + 4, trackY + 5, 0xFF88BBFF);
                    
                    String weightText = weight + "%";
                    this.font.draw(graphics, weightText, trackX + trackW + 6, y + 6, 0xFFFFFF88);
                    
                    this.font.draw(graphics, "⚙", x + colWidth - 12, y + 6, 0xFF66B0FF);
                } else {
                    this.font.draw(graphics, stack.getHoverName().getString(), x + 26, y + 6, 0xFFFFFFFF);
                    if ("all".equals(listKind)) {
                        if (isInFavorite(blockId)) this.font.draw(graphics, "✔", x + colWidth - 16, y + 6, 0xFF00FF00);
                        else this.font.draw(graphics, "➕", x + colWidth - 16, y + 6, 0xFFAAAAAA);
                    } else if ("frequent".equals(listKind)) {
                        this.font.draw(graphics, "➖", x + colWidth - 16, y + 6, 0xFFFF6666);
                    }
                }
            } catch (Exception e) {
                this.font.draw(graphics, blockId, x + 26, y + 6, 0xFFFFFFFF);
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
            ResourceLocation rl = new ResourceLocation(parts[0], parts[1]);
            ItemStack stack = new ItemStack(Registry.BLOCK.get(rl));
            return stack.getHoverName().getString();
        } catch (Exception e) { return blockId; }
    }

    private Block getBlockFromId(String blockId) {
        try {
            String[] parts = blockId.split(":");
            ResourceLocation rl = new ResourceLocation(parts[0], parts[1]);
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
    }

    private String formatBlockWithProperties(String blockId) {
        Map<String, String> props = blockProperties.get(blockId);
        StringBuilder sb = new StringBuilder();
        
        int weight = blockWeights.getOrDefault(blockId, 100);
        if (weight < 100) {
            sb.append(weight).append("%");
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

    private String joinBlocksWithProperties(List<String> blockIds) {
        List<String> formatted = new ArrayList<>();
        for (String id : blockIds) formatted.add(formatBlockWithProperties(id));
        return String.join(",", formatted);
    }

    private String joinBlocksWithoutWeight(List<String> blockIds) {
        List<String> formatted = new ArrayList<>();
        for (String id : blockIds) {
            Map<String, String> props = blockProperties.get(id);
            StringBuilder sb = new StringBuilder();
            sb.append(id);
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
            formatted.add(sb.toString());
        }
        return String.join(",", formatted);
    }

    
    private void normalizeWeights(List<String> blockIds) {
        if (blockIds.isEmpty()) return;
        if (blockIds.size() == 1) {
            blockWeights.put(blockIds.get(0), 100);
            return;
        }
        int total = 0;
        for (String id : blockIds) total += blockWeights.getOrDefault(id, 100);
        if (total <= 0) {
            int avg = 100 / blockIds.size();
            int rem = 100 - avg * blockIds.size();
            for (int i = 0; i < blockIds.size(); i++) {
                blockWeights.put(blockIds.get(i), avg + (i < rem ? 1 : 0));
            }
            return;
        }
        
        int[] newWeights = new int[blockIds.size()];
        int allocated = 0;
        for (int i = 0; i < blockIds.size(); i++) {
            int w = blockWeights.getOrDefault(blockIds.get(i), 100);
            newWeights[i] = (int) Math.round((double) w / total * 100);
            allocated += newWeights[i];
        }
        
        int diff = 100 - allocated;
        if (diff != 0) {
            
            int maxIdx = 0;
            for (int i = 1; i < newWeights.length; i++) {
                if (newWeights[i] > newWeights[maxIdx]) maxIdx = i;
            }
            newWeights[maxIdx] += diff;
            newWeights[maxIdx] = Math.max(1, newWeights[maxIdx]);
        }
        for (int i = 0; i < blockIds.size(); i++) {
            blockWeights.put(blockIds.get(i), Math.max(1, newWeights[i]));
        }
    }

    
    private void setWeight(List<String> blockIds, String targetId, int newWeight) {
        if (blockIds.size() == 1) { blockWeights.put(targetId, 100); return; }
        newWeight = Math.max(1, Math.min(99, newWeight));
        blockWeights.put(targetId, newWeight);
        int remaining = 100 - newWeight;
        List<String> others = new ArrayList<>();
        for (String id : blockIds) if (!id.equals(targetId)) others.add(id);
        int otherTotal = 0;
        for (String id : others) otherTotal += blockWeights.getOrDefault(id, 100);
        if (otherTotal <= 0) {
            int avg = remaining / others.size();
            int rem = remaining - avg * others.size();
            for (int i = 0; i < others.size(); i++) {
                blockWeights.put(others.get(i), avg + (i < rem ? 1 : 0));
            }
        } else {
            int allocated = 0;
            for (int i = 0; i < others.size(); i++) {
                int w = blockWeights.getOrDefault(others.get(i), 100);
                int nw = (i == others.size() - 1) ? (remaining - allocated) : (int) Math.round((double) w / otherTotal * remaining);
                nw = Math.max(1, nw);
                blockWeights.put(others.get(i), nw);
                allocated += nw;
            }
        }
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
            int pageBtnY = listEndY + 5;
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
                    if (mouseY >= y && mouseY <= y + ITEM_HEIGHT - 4) {
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
                if (mouseY >= y && mouseY <= y + ITEM_HEIGHT - 4) {
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
            if (isReplaceMode) {
                targetList = targetTabMode ? favoriteBlocks : cachedFilteredTarget;
                scroll = targetTabMode ? scrollFavorite : scrollTgt;
            } else {
                targetList = favoriteBlocks;
                scroll = scrollFavorite;
            }

            int y = listStartY - scroll;
            for (String blockId : targetList) {
                if (mouseY >= y && mouseY <= y + ITEM_HEIGHT - 4) {
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
                    if (mouseY >= ySrc && mouseY <= ySrc + ITEM_HEIGHT - 4) {
                        int relX = (int) (mouseX - rightX);
                        if (relX >= colWidth - 12) {
                            editingBlockId = blockId; propScroll = 0;
                        } else if (relX >= colWidth - 78 && relX < colWidth - 52) {
                            int trackX = colWidth - 78;
                            int pct = (int)((float)(relX - trackX) / 26 * 100);
                            if (hasControlDown()) {
                                
                                openWeightInput(blockId);
                            } else {
                                draggingWeightBlockId = blockId;
                                setWeight(selectedSources, blockId, pct);
                            }
                        } else if (relX >= colWidth - 52 && relX < colWidth - 24 && hasControlDown()) {
                            
                            openWeightInput(blockId);
                        }
                        
                        return true;
                    }
                    ySrc += ITEM_HEIGHT;
                }

                int yTgt = rightLineY + 10 - scrollSelTgt;
                for (String blockId : selectedTargets) {
                    if (mouseY >= yTgt && mouseY <= yTgt + ITEM_HEIGHT - 4) {
                        int relX = (int) (mouseX - rightX);
                        if (relX >= colWidth - 12) {
                            editingBlockId = blockId; propScroll = 0;
                        } else if (relX >= colWidth - 78 && relX < colWidth - 52) {
                            int trackX = colWidth - 78;
                            int pct = (int)((float)(relX - trackX) / 26 * 100);
                            if (hasControlDown()) openWeightInput(blockId);
                            else { draggingWeightBlockId = blockId; setWeight(selectedTargets, blockId, pct); }
                        } else if (relX >= colWidth - 52 && relX < colWidth - 24 && hasControlDown()) {
                            openWeightInput(blockId);
                        }
                        
                        return true;
                    }
                    yTgt += ITEM_HEIGHT;
                }
            } else {
                int y = listStartY - scrollSelected;
                for (String blockId : selectedIds) {
                    if (mouseY >= y && mouseY <= y + ITEM_HEIGHT - 4) {
                        int relX = (int) (mouseX - rightX);
                        if (relX >= colWidth - 12) {
                            editingBlockId = blockId; propScroll = 0;
                        } else if (relX >= colWidth - 78 && relX < colWidth - 52) {
                            int trackX = colWidth - 78;
                            int pct = (int)((float)(relX - trackX) / 26 * 100);
                            if (hasControlDown()) openWeightInput(blockId);
                            else { draggingWeightBlockId = blockId; setWeight(selectedIds, blockId, pct); }
                        } else if (relX >= colWidth - 52 && relX < colWidth - 24 && hasControlDown()) {
                            openWeightInput(blockId);
                        }
                        
                        return true;
                    }
                    y += ITEM_HEIGHT;
                }
            }
        }
        return false;
    }

    private void openWeightInput(String blockId) {
        weightInputBlockId = blockId;
        int weight = blockWeights.getOrDefault(blockId, 100);
        if (weightInputField == null) {
            weightInputField = new EditBox(this.font, 0, 0, 50, 16, Component.literal(""));
            weightInputField.setMaxLength(3);
            weightInputField.setFilter(s -> s.isEmpty() || s.matches("\\d{1,3}"));
        }
        weightInputField.setValue(String.valueOf(weight));
        weightInputField.setFocus(true);
        this.setFocused(weightInputField);
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
        try {
            int val = Integer.parseInt(weightInputField.getValue());
            val = Math.max(1, Math.min(100, val));
            
            if (isReplaceMode) {
                if (selectedSources.contains(weightInputBlockId)) {
                    setWeight(selectedSources, weightInputBlockId, val);
                } else if (selectedTargets.contains(weightInputBlockId)) {
                    setWeight(selectedTargets, weightInputBlockId, val);
                }
            } else {
                setWeight(selectedIds, weightInputBlockId, val);
            }
        } catch (NumberFormatException ignored) {}
        weightInputBlockId = null;
        weightInputField.setFocus(false);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingWeightBlockId != null && button == 0) {
            
            double trackStart = rightX + colWidth - 78;
            double trackEnd = rightX + colWidth - 52;
            double pct = (mouseX - trackStart) / (trackEnd - trackStart) * 100;
            int val = (int) Math.max(1, Math.min(100, Math.round(pct)));
            if (isReplaceMode) {
                if (selectedSources.contains(draggingWeightBlockId)) {
                    setWeight(selectedSources, draggingWeightBlockId, val);
                } else if (selectedTargets.contains(draggingWeightBlockId)) {
                    setWeight(selectedTargets, draggingWeightBlockId, val);
                }
            } else {
                setWeight(selectedIds, draggingWeightBlockId, val);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingWeightBlockId != null) {
            draggingWeightBlockId = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
                    int maxScroll = getMaxScroll(favoriteBlocks.size(), listEndY - listStartY);
                    scrollFavorite -= delta * 20; scrollFavorite = Math.max(0, Math.min(maxScroll, scrollFavorite));
                } else {
                    
                    int maxScroll = getMaxScroll(cachedFilteredTarget.size(), listEndY - listStartY);
                    scrollTgt -= delta * 20; scrollTgt = Math.max(0, Math.min(maxScroll, scrollTgt));
                }
            } else if (midTabMode == 1) {
                
                int maxScroll = getMaxScroll(historyItems.size(), listEndY - listStartY);
                scrollHistory -= delta * 20; scrollHistory = Math.max(0, Math.min(maxScroll, scrollHistory));
            } else {
                int maxScroll = getMaxScroll(favoriteBlocks.size(), listEndY - listStartY);
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
            normalizeWeights(list);
        } else {
            list.add(blockId);
            
            if (list.size() == 1) {
                blockWeights.put(blockId, 100);
            } else {
                int newWeight = 100 / list.size();
                blockWeights.put(blockId, newWeight);
                
                int remaining = 100 - newWeight;
                List<String> others = new ArrayList<>();
                for (String id : list) if (!id.equals(blockId)) others.add(id);
                int otherTotal = 0;
                for (String id : others) otherTotal += blockWeights.getOrDefault(id, 100);
                if (otherTotal > 0) {
                    int allocated = 0;
                    for (int i = 0; i < others.size(); i++) {
                        int w = blockWeights.getOrDefault(others.get(i), 100);
                        int nw = (i == others.size() - 1) ? (remaining - allocated) : (int) Math.round((double) w / otherTotal * remaining);
                        nw = Math.max(1, nw);
                        blockWeights.put(others.get(i), nw);
                        allocated += nw;
                    }
                }
            }
        }
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

    private void renderHistoryList(PoseStack graphics, int x, int y) {
        for (int i = 0; i < historyItems.size(); i++) {
            int itemY = y + i * ITEM_HEIGHT;
            if (itemY < listStartY - ITEM_HEIGHT || itemY > listEndY) continue;
            HistoryManager.HistoryItem item = historyItems.get(i);
            fill(graphics, x, itemY, x + colWidth, itemY + ITEM_HEIGHT - 2, 0xAA000000);
            String modeText = item.mode.equals("id") ? "[ID]" : item.mode.equals("set") ? "[SET]" : "[REP]";
            String display = modeText + " " + item.content;
            if (display.length() > 28) display = display.substring(0, 28) + "...";
            this.font.draw(graphics, display, x + 4, itemY + 6, 0xFFAAAAAA);
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
