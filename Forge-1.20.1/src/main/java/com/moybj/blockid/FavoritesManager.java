package com.moybj.blockid;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    /** 「默认收藏」。用转义写法避免源码/编译编码不一致时被写坏。 */
    public static final String DEFAULT_GROUP_NAME = "\u9ED8\u8BA4\u6536\u85CF";

    public static class FavoriteGroup {
        public String name;
        public List<String> blockIds = new ArrayList<>();

        public FavoriteGroup() {}

        public FavoriteGroup(String name) {
            this.name = name;
        }
    }

    private static List<FavoriteGroup> groups = new ArrayList<>();
    private static int currentGroupIndex = 0;

    public static void init() {
        groups = ConfigManager.getFavoriteGroups();
        if (groups.isEmpty()) {
            groups.add(new FavoriteGroup(DEFAULT_GROUP_NAME));
            ConfigManager.setFavoriteGroups(groups);
        }
    }

    public static List<FavoriteGroup> getGroups() {
        if (groups.isEmpty()) init();
        return groups;
    }

    public static FavoriteGroup getCurrentGroup() {
        if (groups.isEmpty()) init();
        if (currentGroupIndex >= groups.size()) currentGroupIndex = 0;
        return groups.get(currentGroupIndex);
    }

    public static int getCurrentGroupIndex() {
        return currentGroupIndex;
    }

    public static void setCurrentGroupIndex(int index) {
        if (index >= 0 && index < groups.size()) {
            currentGroupIndex = index;
        }
    }

    public static void addBlock(String blockId) {
        FavoriteGroup group = getCurrentGroup();
        if (!group.blockIds.contains(blockId)) {
            group.blockIds.add(blockId);
            ConfigManager.setFavoriteGroups(groups);
        }
    }

    public static void removeBlock(String blockId) {
        FavoriteGroup group = getCurrentGroup();
        group.blockIds.remove(blockId);
        ConfigManager.setFavoriteGroups(groups);
    }

    public static boolean isInCurrentGroup(String blockId) {
        return getCurrentGroup().blockIds.contains(blockId);
    }

    public static void addGroup(String name) {
        groups.add(new FavoriteGroup(name));
        currentGroupIndex = groups.size() - 1;
        ConfigManager.setFavoriteGroups(groups);
    }

    public static void removeGroup(int index) {
        if (index >= 0 && index < groups.size() && groups.size() > 1) {
            groups.remove(index);
            if (currentGroupIndex >= groups.size()) currentGroupIndex = groups.size() - 1;
            ConfigManager.setFavoriteGroups(groups);
        }
    }

    public static void renameGroup(int index, String newName) {
        if (index >= 0 && index < groups.size()) {
            groups.get(index).name = newName;
            ConfigManager.setFavoriteGroups(groups);
        }
    }

    public static List<String> getCurrentBlocks() {
        return new ArrayList<>(getCurrentGroup().blockIds);
    }
}
