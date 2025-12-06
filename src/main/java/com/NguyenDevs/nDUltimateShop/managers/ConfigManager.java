package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final NDUltimateShop plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;
    private GUIConfigManager guiConfigManager;

    public ConfigManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        this.guiConfigManager = new GUIConfigManager(plugin);
    }

    public void loadAllConfigs() {
        plugin.saveDefaultConfig();

        createConfig("itemsell.yml");
        createConfig("coupons.yml");
        createConfig("language.yml");

        File guiFolder = new File(plugin.getDataFolder(), "gui");
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }

        createGUIConfig("shop.yml");
        createGUIConfig("auction.yml");
        createGUIConfig("sell.yml");
        createGUIConfig("nightshop.yml");

        loadGUIConfigs();

        plugin.getLogger().info("Đã tải tất cả các file cấu hình!");
    }

    private void createConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        configFiles.put(fileName, file);
        configs.put(fileName, YamlConfiguration.loadConfiguration(file));
    }

    private void createGUIConfig(String fileName) {
        File guiFolder = new File(plugin.getDataFolder(), "gui");
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }

        File file = new File(guiFolder, fileName);
        if (!file.exists()) {
            plugin.saveResource("gui/" + fileName, false);
        }
        String key = "gui/" + fileName;
        configFiles.put(key, file);
        configs.put(key, YamlConfiguration.loadConfiguration(file));
    }

    private void loadGUIConfigs() {
        guiConfigManager.loadGUIConfig("shop");
        guiConfigManager.loadGUIConfig("auction");
        guiConfigManager.loadGUIConfig("sell");
        guiConfigManager.loadGUIConfig("nightshop");
    }

    public FileConfiguration getConfig(String fileName) {
        return configs.getOrDefault(fileName, plugin.getConfig());
    }

    public void saveConfig(String fileName) {
        File file = configFiles.get(fileName);
        FileConfiguration config = configs.get(fileName);
        if (file != null && config != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Không thể lưu file " + fileName + ": " + e.getMessage());
            }
        }
    }

    public void reloadConfig(String fileName) {
        File file = configFiles.get(fileName);
        if (file != null) {
            configs.put(fileName, YamlConfiguration.loadConfiguration(file));
        }
    }

    public void reloadAllConfigs() {
        plugin.reloadConfig();
        for (String fileName : configFiles.keySet()) {
            reloadConfig(fileName);
        }
        loadGUIConfigs();
    }

    public GUIConfigManager.GUIConfig getGUIConfig(String guiName) {
        return guiConfigManager.getGUIConfig(guiName);
    }
}