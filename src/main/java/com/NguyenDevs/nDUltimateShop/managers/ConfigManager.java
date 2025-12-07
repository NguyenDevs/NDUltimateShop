package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final NDUltimateShop plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;
    private final Map<String, FileConfiguration> dataConfigs;
    private final Map<String, File> dataFiles;
    private GUIConfigManager guiConfigManager;

    public ConfigManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        this.dataConfigs = new HashMap<>();
        this.dataFiles = new HashMap<>();
        this.guiConfigManager = new GUIConfigManager(plugin);
    }

    public void loadAllConfigs() {
        updateDefaultConfig();

        createOrUpdateConfig("itemsell.yml");
        createOrUpdateConfig("language.yml");

        createOrUpdateConfig("gui/shop.yml");
        createOrUpdateConfig("gui/auction.yml");
        createOrUpdateConfig("gui/sell.yml");
        createOrUpdateConfig("gui/nightshop.yml");

        createDataConfig("shops.yml");
        createDataConfig("auctions.yml");
        createDataConfig("nightshop_data.yml");
        createDataConfig("coupons.yml");
        createDataConfig("sell_data.yml");

        loadGUIConfigs();

        plugin.getLogger().info("Đã tải và cập nhật các file cấu hình!");
    }

    public void reloadAllConfigs() {
        configs.clear();
        configFiles.clear();
        dataConfigs.clear();
        dataFiles.clear();

        plugin.reloadConfig();
        loadAllConfigs();
    }

    private void updateDefaultConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        InputStream defConfigStream = plugin.getResource("config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
            config.options().copyDefaults(true);
        }
        plugin.saveConfig();
    }

    private void createOrUpdateConfig(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        InputStream defConfigStream = plugin.getResource(path);

        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
            config.options().copyDefaults(true);
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Không thể lưu file update " + path + ": " + e.getMessage());
            }
        }

        configFiles.put(path, file);
        configs.put(path, config);
    }

    private void createDataConfig(String fileName) {
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataFiles.put(fileName, file);
        dataConfigs.put(fileName, YamlConfiguration.loadConfiguration(file));
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

    public FileConfiguration getDataConfig(String fileName) {
        return dataConfigs.get(fileName);
    }

    public void saveConfig(String fileName) {
        File file = configFiles.get(fileName);
        FileConfiguration config = configs.get(fileName);
        if (file != null && config != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Không thể lưu file config " + fileName + ": " + e.getMessage());
            }
        }
    }

    public void saveData(String fileName) {
        File file = dataFiles.get(fileName);
        FileConfiguration config = dataConfigs.get(fileName);
        if (file != null && config != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Không thể lưu file data " + fileName + ": " + e.getMessage());
            }
        }
    }

    public GUIConfigManager.GUIConfig getGUIConfig(String guiName) {
        return guiConfigManager.getGUIConfig(guiName);
    }
}