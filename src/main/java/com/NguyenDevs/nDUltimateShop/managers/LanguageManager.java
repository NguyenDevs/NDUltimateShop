package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final NDUltimateShop plugin;
    private final Map<String, String> messages;

    public LanguageManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }

    public void loadLanguage() {
        FileConfiguration lang = plugin.getConfigManager().getConfig("language.yml");
        messages.clear();

        for (String key : lang.getKeys(false)) {
            messages.put(key, lang.getString(key));
        }

        plugin.getLogger().info("Đã tải ngôn ngữ thành công!");
    }

    public String getMessage(String key) {
        return colorize(messages.getOrDefault(key, "&cMessage not found: " + key));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    public String getPrefixedMessage(String key) {
        return getPrefix() + " " + getMessage(key);
    }

    public String getPrefixedMessage(String key, Map<String, String> placeholders) {
        return getPrefix() + " " + getMessage(key, placeholders);
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}