package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class SellManager {

    private final NDUltimateShop plugin;
    private final Map<String, Double> itemPrices;
    private final Map<Material, Double> materialPrices;

    public SellManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.itemPrices = new HashMap<>();
        this.materialPrices = new HashMap<>();
    }

    public void loadSellPrices() {
        itemPrices.clear();
        materialPrices.clear();

        FileConfiguration data = plugin.getConfigManager().getDataConfig("sell_data.yml");
        ConfigurationSection customSection = data.getConfigurationSection("custom-items");
        if (customSection != null) {
            for (String key : customSection.getKeys(false)) {
                ItemStack item = customSection.getItemStack(key + ".item");
                double price = customSection.getDouble(key + ".price");

                if (item != null) {
                    itemPrices.put(getItemHash(item), price);
                }
            }
        }

        FileConfiguration config = plugin.getConfigManager().getConfig("itemsell.yml");
        ConfigurationSection materialSection = config.getConfigurationSection("materials");
        if (materialSection != null) {
            for (String key : materialSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    double price = materialSection.getDouble(key);
                    materialPrices.put(material, price);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Material khong hop le trong itemsell.yml: " + key);
                }
            }
        }

        plugin.getLogger().info("Da tai " + itemPrices.size() + " gia custom tu data va " + materialPrices.size() + " gia material!");
    }

    public void saveSellPrices() {
        FileConfiguration data = plugin.getConfigManager().getDataConfig("sell_data.yml");
        data.set("custom-items", null);

        int index = 0;
        for (Map.Entry<String, Double> entry : itemPrices.entrySet()) {
            String key = "custom-items.item" + index;
            data.set(key + ".price", entry.getValue());
            index++;
        }
        plugin.getConfigManager().saveData("sell_data.yml");
    }

    public double getItemPrice(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return 0.0;
        }

        String hash = getItemHash(item);
        if (itemPrices.containsKey(hash)) {
            return itemPrices.get(hash);
        }

        if (materialPrices.containsKey(item.getType())) {
            return materialPrices.get(item.getType());
        }

        return 0.0;
    }

    public double calculateSellValue(ItemStack item) {
        return getItemPrice(item) * item.getAmount();
    }

    public void setCustomItemPrice(ItemStack item, double price) {
        String hash = getItemHash(item);
        itemPrices.put(hash, price);

        FileConfiguration data = plugin.getConfigManager().getDataConfig("sell_data.yml");
        String key = "custom-items.item_" + System.currentTimeMillis();
        data.set(key + ".item", item.clone());
        data.set(key + ".price", price);
        plugin.getConfigManager().saveData("sell_data.yml");

        loadSellPrices();
    }

    private String getItemHash(ItemStack item) {
        StringBuilder hash = new StringBuilder(item.getType().name());
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) hash.append(":").append(meta.getDisplayName());
            if (meta.hasLore()) hash.append(":").append(meta.getLore().toString());
            if (meta.hasCustomModelData()) hash.append(":").append(meta.getCustomModelData());
        }
        return hash.toString();
    }
}