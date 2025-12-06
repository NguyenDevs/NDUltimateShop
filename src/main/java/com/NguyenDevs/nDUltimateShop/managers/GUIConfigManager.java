package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIConfigManager {

    private final NDUltimateShop plugin;
    private final Map<String, GUIConfig> guiConfigs;

    public GUIConfigManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.guiConfigs = new HashMap<>();
    }

    public void loadGUIConfig(String guiName) {
        FileConfiguration config = plugin.getConfigManager().getConfig("gui/" + guiName + ".yml");
        guiConfigs.put(guiName, new GUIConfig(guiName, config));
    }

    public GUIConfig getGUIConfig(String guiName) {
        return guiConfigs.get(guiName);
    }

    public class GUIConfig {
        private final String name;
        private final FileConfiguration config;
        private final Map<String, ItemStack> decorativeItems;

        public GUIConfig(String name, FileConfiguration config) {
            this.name = name;
            this.config = config;
            this.decorativeItems = new HashMap<>();
            loadDecorativeItems();
        }

        private void loadDecorativeItems() {
            ConfigurationSection itemsSection = config.getConfigurationSection("decorative-items");
            if (itemsSection == null) return;

            for (String key : itemsSection.getKeys(false)) {
                String path = "decorative-items." + key;
                Material material = Material.valueOf(config.getString(path + ".material", "STONE"));
                String displayName = config.getString(path + ".name", "");
                List<String> lore = config.getStringList(path + ".lore");
                int customModelData = config.getInt(path + ".custom-model-data", 0);
                boolean enchanted = config.getBoolean(path + ".enchanted", false);

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (!displayName.isEmpty()) meta.setDisplayName(LanguageManager.colorize(displayName));
                    if (!lore.isEmpty()) {
                        List<String> coloredLore = new ArrayList<>();
                        for (String line : lore) coloredLore.add(LanguageManager.colorize(line));
                        meta.setLore(coloredLore);
                    }
                    if (customModelData > 0) meta.setCustomModelData(customModelData);
                    if (enchanted) {
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    item.setItemMeta(meta);
                }
                decorativeItems.put(key, item);
            }
        }

        public String getTitle() { return config.getString("title", "&6GUI"); }
        public int getRows() { return config.getInt("rows", 6); }

        public Map<String, Integer> getSlotMapping() {
            Map<String, Integer> mapping = new HashMap<>();
            ConfigurationSection slotsSection = config.getConfigurationSection("slots");
            if (slotsSection != null) {
                for (String key : slotsSection.getKeys(false)) mapping.put(key, slotsSection.getInt(key));
            }
            if (!mapping.containsKey("sort")) {
                mapping.put("sort", 52);
            }
            return mapping;
        }

        public List<Integer> getItemSlots() { return config.getIntegerList("item-slots"); }
        public ItemStack getDecorativeItem(String key) {
            return decorativeItems.containsKey(key) ? decorativeItems.get(key).clone() : null;
        }
        public List<Integer> getFillerSlots() { return config.getIntegerList("filler-slots"); }
        public Material getFillerMaterial() { return Material.valueOf(config.getString("filler-material", "GRAY_STAINED_GLASS_PANE")); }
        public FileConfiguration getConfig() { return config; }

        public void playSound(Player player, String key) {
            if (!plugin.getConfig().getBoolean("sounds.enabled", true)) return;

            if (!config.getBoolean("sounds.enable", true)) return;

            String path = "sounds." + key;
            if (!config.contains(path)) return;

            String soundStr = config.getString(path);
            if (soundStr == null || soundStr.isEmpty()) return;
            try {
                String[] parts = soundStr.split(":");
                Sound sound = Sound.valueOf(parts[0].toUpperCase());
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception ignored) {}
        }

        public List<String> getLoreFormat() {
            return config.getStringList("item-lore-format");
        }

        public String getMessage(String key) {
            return config.getString("messages." + key, "");
        }
    }
}