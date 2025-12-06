package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

public class BlackShopManager {

    private final NDUltimateShop plugin;
    private final Map<String, ShopItem> blackShopItems;
    private int taskId = -1;
    private boolean isOpen = false;

    public BlackShopManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.blackShopItems = new HashMap<>();
    }

    public void loadBlackShop() {
        blackShopItems.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("gui/blackshop.yml");

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ItemStack item = itemsSection.getItemStack(key + ".item");
                double price = itemsSection.getDouble(key + ".price");
                int stock = itemsSection.getInt(key + ".stock");

                if (item != null) {
                    blackShopItems.put(key, new ShopItem(key, item, price, stock));
                }
            }
        }

        plugin.getLogger().info("Đã tải " + blackShopItems.size() + " vật phẩm trong chợ đêm!");
    }

    public void saveBlackShop() {
        FileConfiguration config = plugin.getConfigManager().getConfig("gui/blackshop.yml");
        config.set("items", null);

        for (Map.Entry<String, ShopItem> entry : blackShopItems.entrySet()) {
            String key = "items." + entry.getKey();
            ShopItem item = entry.getValue();

            config.set(key + ".item", item.getItemStack());
            config.set(key + ".price", item.getPrice());
            config.set(key + ".stock", item.getStock());
        }

        plugin.getConfigManager().saveConfig("gui/blackshop.yml");
    }

    public void startScheduler() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            boolean shouldBeOpen = isWithinOpenHours();

            if (shouldBeOpen && !isOpen) {
                openBlackShop();
            } else if (!shouldBeOpen && isOpen) {
                closeBlackShop();
            }
        }, 0L, 1200L); // Check every minute
    }

    public void stopScheduler() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private boolean isWithinOpenHours() {
        String timezone = plugin.getConfig().getString("blackshop.timezone", "Asia/Ho_Chi_Minh");
        int openHour = plugin.getConfig().getInt("blackshop.open-time", 20);
        int closeHour = plugin.getConfig().getInt("blackshop.close-time", 22);

        LocalTime now = LocalTime.now(ZoneId.of(timezone));
        LocalTime openTime = LocalTime.of(openHour, 0);
        LocalTime closeTime = LocalTime.of(closeHour, 0);

        if (closeTime.isAfter(openTime)) {
            return now.isAfter(openTime) && now.isBefore(closeTime);
        } else {
            // Overnight period (e.g., 22:00 to 02:00)
            return now.isAfter(openTime) || now.isBefore(closeTime);
        }
    }

    private void openBlackShop() {
        isOpen = true;
        if (plugin.getConfig().getBoolean("blackshop.announce-open", true)) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("blackshop-announce-open"));
        }
    }

    private void closeBlackShop() {
        isOpen = false;
        if (plugin.getConfig().getBoolean("blackshop.announce-close", true)) {
            Bukkit.broadcastMessage(plugin.getLanguageManager().getMessage("blackshop-announce-close"));
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void addItem(String id, ItemStack item, double price, int stock) {
        blackShopItems.put(id, new ShopItem(id, item, price, stock));
        saveBlackShop();
    }

    public void removeItem(String id) {
        blackShopItems.remove(id);
        saveBlackShop();
    }

    public ShopItem getItem(String id) {
        return blackShopItems.get(id);
    }

    public Collection<ShopItem> getAllItems() {
        return blackShopItems.values();
    }

    public boolean purchaseItem(String id, int amount) {
        ShopItem item = blackShopItems.get(id);
        if (item == null) return false;

        if (item.decreaseStock(amount)) {
            saveBlackShop();
            return true;
        }
        return false;
    }

    public String getOpenTimeString() {
        int openHour = plugin.getConfig().getInt("blackshop.open-time", 20);
        int closeHour = plugin.getConfig().getInt("blackshop.close-time", 22);
        return openHour + "h - " + closeHour + "h";
    }
}