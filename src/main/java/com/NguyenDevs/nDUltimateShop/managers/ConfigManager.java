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

    public ConfigManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
    }

    public void loadAllConfigs() {
        // Save default config
        plugin.saveDefaultConfig();

        // Create and load custom configs
        createConfig("shops.yml");
        createConfig("auctions.yml");
        createConfig("itemsell.yml");
        createConfig("blackshop.yml");
        createConfig("coupons.yml");
        createConfig("language.yml");

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
    }
}