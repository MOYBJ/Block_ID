package com.moybj.blockid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final Charset GBK = Charset.forName("GBK");
    private static final Charset GB18030 = Charset.forName("GB18030");

    private static File configFile;
    private static ConfigData data;

    private static class ConfigData {
        List<FavoritesManager.FavoriteGroup> favoriteGroups = new ArrayList<>();
        List<HistoryManager.HistoryItem> history = new ArrayList<>();
    }

    public static void init() {
        configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "block_id.json");
        load();
    }

    private static void load() {
        if (configFile != null && configFile.exists()) {
            try {
                String json = decode(Files.readAllBytes(configFile.toPath()));
                Type type = new TypeToken<ConfigData>() {}.getType();
                data = GSON.fromJson(json, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (data == null) data = new ConfigData();
        repairGroupNames();
    }

    /**
     * 读取时先按 UTF-8 解；解出替换符说明这份文件是旧版本用平台默认编码（中文系统为 GBK）写的，
     * 退回 GBK 再解一次。写入一律用 UTF-8，所以新文件不会再有这个问题。
     */
    private static String decode(byte[] raw) {
        String text = new String(raw, UTF8);
        if (text.indexOf('\uFFFD') < 0) return text;
        for (Charset cs : new Charset[]{GB18030, GBK}) {
            String alt = new String(raw, cs);
            if (alt.indexOf('\uFFFD') < 0) return alt;
        }
        return text;
    }

    private static void repairGroupNames() {
        if (data == null || data.favoriteGroups == null) return;
        for (FavoritesManager.FavoriteGroup group : data.favoriteGroups) {
            if (group == null || group.name == null) continue;
            group.name = repairName(group.name);
        }
    }

    /**
     * 旧版本写配置用过平台默认编码，UTF-8 字节被当成 GBK 读出来就成了「榛樿鏀惰棌」这类乱码，
     * 而且会被原样存回文件。把这种字符串按 GBK 取回原始字节、再用 UTF-8 解一次即可还原。
     *
     * 只做保守还原：必须能被 GBK 无损表示，且还原结果里没有替换符。正常保存的中文名
     * （本身是 UTF-8）按 GBK 取字节后基本不可能通过 UTF-8 解码，因此不会被误改。
     */
    static String repairName(String name) {
        if (name == null || name.isEmpty() || !hasCjk(name)) return name;
        if (name.equals(FavoritesManager.DEFAULT_GROUP_NAME)) return name;
        for (Charset cs : new Charset[]{GBK, GB18030}) {
            byte[] bytes;
            try {
                bytes = name.getBytes(cs);
            } catch (Exception e) {
                continue;
            }
            if (!new String(bytes, cs).equals(name)) continue;
            String back = new String(bytes, UTF8);
            if (back.indexOf('\uFFFD') >= 0) continue;
            if (back.equals(name)) continue;
            if (!isPrintable(back)) continue;
            return back;
        }
        return name;
    }

    private static boolean hasCjk(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= '\u4E00' && s.charAt(i) <= '\u9FFF') return true;
        }
        return false;
    }

    private static boolean isPrintable(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < ' ' || c == '\u007F') return false;
        }
        return true;
    }

    public static void save() {
        if (configFile == null) init();
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Files.write(configFile.toPath(), GSON.toJson(data).getBytes(UTF8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<FavoritesManager.FavoriteGroup> getFavoriteGroups() {
        if (data == null) init();
        if (data.favoriteGroups == null) data.favoriteGroups = new ArrayList<>();
        return new ArrayList<>(data.favoriteGroups);
    }

    public static void setFavoriteGroups(List<FavoritesManager.FavoriteGroup> groups) {
        if (data == null) init();
        data.favoriteGroups = new ArrayList<>(groups);
        save();
    }

    public static List<HistoryManager.HistoryItem> getHistory() {
        if (data == null) init();
        if (data.history == null) data.history = new ArrayList<>();
        return new ArrayList<>(data.history);
    }

    public static void setHistory(List<HistoryManager.HistoryItem> history) {
        if (data == null) init();
        data.history = new ArrayList<>(history);
        save();
    }
}
