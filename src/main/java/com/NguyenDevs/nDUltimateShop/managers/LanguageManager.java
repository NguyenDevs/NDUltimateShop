package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageManager {

    private final NDUltimateShop plugin;
    private final Map<String, String> messages;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");

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
    }

    public String getMessage(String key) {
        return colorize(messages.getOrDefault(key, "&cMissing message: " + key));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
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
        if (text == null) return "";
        text = translateGradients(text);
        text = translateHexColorCodes(text);
        text = org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length());
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        return matcher.appendTail(buffer).toString();
    }

    private static String translateGradients(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String text = matcher.group(3);
            matcher.appendReplacement(buffer, applyGradient(text, startHex, endHex));
        }
        return matcher.appendTail(buffer).toString();
    }

    private static String applyGradient(String text, String startHex, String endHex) {
        if (text == null || text.isEmpty()) return "";
        int[] startRgb = hexToRgb(startHex);
        int[] endRgb = hexToRgb(endHex);
        StringBuilder result = new StringBuilder();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }
            float ratio = (float) i / (length - 1);
            int r = (int) (startRgb[0] + ratio * (endRgb[0] - startRgb[0]));
            int g = (int) (startRgb[1] + ratio * (endRgb[1] - startRgb[1]));
            int b = (int) (startRgb[2] + ratio * (endRgb[2] - startRgb[2]));
            result.append(ChatColor.of(String.format("#%02x%02x%02x", r, g, b))).append(c);
        }
        return result.toString();
    }

    private static int[] hexToRgb(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }
}