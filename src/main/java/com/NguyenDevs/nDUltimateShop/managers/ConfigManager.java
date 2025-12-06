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
        plugin.saveDefaultConfig();

        createConfig("itemsell.yml");
        createConfig("language.yml");

        createGUIConfig("shop.yml");
        createGUIConfig("auction.yml");
        createGUIConfig("sell.yml");
        createGUIConfig("nightshop.yml");

        createDataConfig("shops.yml");
        createDataConfig("auctions.yml");
        createDataConfig("nightshop_data.yml");
        createDataConfig("coupons.yml");
        createDataConfig("sell_data.yml");

        loadGUIConfigs();

        plugin.getLogger().info("Da tai tat ca file cau hinh va du lieu!");
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
                plugin.getLogger().severe("Khong the luu file config " + fileName + ": " + e.getMessage());
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
                plugin.getLogger().severe("Khong the luu file data " + fileName + ": " + e.getMessage());
            }
        }
    }

    public void reloadAllConfigs() {
        plugin.reloadConfig();
        for (String fileName : configFiles.keySet()) {
            File file = configFiles.get(fileName);
            if (file != null) configs.put(fileName, YamlConfiguration.loadConfiguration(file));
        }
        for (String fileName : dataFiles.keySet()) {
            File file = dataFiles.get(fileName);
            if (file != null) dataConfigs.put(fileName, YamlConfiguration.loadConfiguration(file));
        }
        loadGUIConfigs();
    }

    public GUIConfigManager.GUIConfig getGUIConfig(String guiName) {
        return guiConfigManager.getGUIConfig(guiName);
    }
}