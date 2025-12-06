package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ShopManager {

    private final NDUltimateShop plugin;
    private final Map<String, ShopItem> shopItems;

    public ShopManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.shopItems = new HashMap<>();
    }

    public void loadShops() {
        shopItems.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("shops.yml");

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ItemStack item = itemsSection.getItemStack(key + ".item");
                double price = itemsSection.getDouble(key + ".price");
                int stock = itemsSection.getInt(key + ".stock", -1);

                if (item != null) {
                    shopItems.put(key, new ShopItem(key, item, price, stock));
                }
            }
        }

        plugin.getLogger().info("Đã tải " + shopItems.size() + " vật phẩm trong cửa hàng!");
    }

    public void saveShops() {
        FileConfiguration config = plugin.getConfigManager().getConfig("shops.yml");
        config.set("items", null);

        for (Map.Entry<String, ShopItem> entry : shopItems.entrySet()) {
            String key = "items." + entry.getKey();
            ShopItem item = entry.getValue();

            config.set(key + ".item", item.getItemStack());
            config.set(key + ".price", item.getPrice());
            config.set(key + ".stock", item.getStock());
        }

        plugin.getConfigManager().saveConfig("shops.yml");
    }

    public void addShopItem(String id, ItemStack item, double price, int stock) {
        shopItems.put(id, new ShopItem(id, item, price, stock));
        saveShops();
    }

    public void removeShopItem(String id) {
        shopItems.remove(id);
        saveShops();
    }

    public ShopItem getShopItem(String id) {
        return shopItems.get(id);
    }

    public Collection<ShopItem> getAllShopItems() {
        return shopItems.values();
    }

    public boolean purchaseItem(String id, int amount) {
        ShopItem item = shopItems.get(id);
        if (item == null) return false;

        if (item.decreaseStock(amount)) {
            saveShops();
            return true;
        }
        return false;
    }
}