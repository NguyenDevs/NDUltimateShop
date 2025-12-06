package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class AuctionManager {

    private final NDUltimateShop plugin;
    private final Map<String, AuctionListing> activeListings;
    private int taskId = -1;

    public AuctionManager(NDUltimateShop plugin) {
        this.plugin = plugin;
        this.activeListings = new HashMap<>();
    }

    public void loadAuctions() {
        activeListings.clear();
        FileConfiguration config = plugin.getConfigManager().getConfig("gui/auction.yml");

        ConfigurationSection listingsSection = config.getConfigurationSection("listings");
        if (listingsSection != null) {
            for (String key : listingsSection.getKeys(false)) {
                UUID sellerUUID = UUID.fromString(listingsSection.getString(key + ".seller"));
                String sellerName = listingsSection.getString(key + ".sellerName");
                ItemStack item = listingsSection.getItemStack(key + ".item");
                double price = listingsSection.getDouble(key + ".price");
                long expiration = listingsSection.getLong(key + ".expiration");

                if (item != null && !isExpired(expiration)) {
                    activeListings.put(key, new AuctionListing(key, sellerUUID, sellerName, item, price, expiration));
                }
            }
        }

        plugin.getLogger().info("Đã tải " + activeListings.size() + " mặt hàng đấu giá!");
    }

    public void saveAuctions() {
        FileConfiguration config = plugin.getConfigManager().getConfig("gui/auction.yml");
        config.set("listings", null);

        for (Map.Entry<String, AuctionListing> entry : activeListings.entrySet()) {
            String key = "listings." + entry.getKey();
            AuctionListing listing = entry.getValue();

            config.set(key + ".seller", listing.getSellerUUID().toString());
            config.set(key + ".sellerName", listing.getSellerName());
            config.set(key + ".item", listing.getItemStack());
            config.set(key + ".price", listing.getPrice());
            config.set(key + ".expiration", listing.getExpirationTime());
        }

        plugin.getConfigManager().saveConfig("gui/auction.yml");
    }

    public String createListing(Player seller, ItemStack item, double price) {
        int maxListings = plugin.getConfig().getInt("auction.max-listings-per-player", 10);
        long playerListings = activeListings.values().stream()
                .filter(l -> l.getSellerUUID().equals(seller.getUniqueId()))
                .count();

        if (playerListings >= maxListings) {
            return null;
        }

        long duration = plugin.getConfig().getLong("auction.duration", 86400) * 1000;
        long expiration = System.currentTimeMillis() + duration;

        String id = UUID.randomUUID().toString();
        AuctionListing listing = new AuctionListing(id, seller.getUniqueId(), seller.getName(), item, price, expiration);

        activeListings.put(id, listing);
        saveAuctions();

        return id;
    }

    public boolean removeListing(String id) {
        if (activeListings.remove(id) != null) {
            saveAuctions();
            return true;
        }
        return false;
    }

    public AuctionListing getListing(String id) {
        return activeListings.get(id);
    }

    public Collection<AuctionListing> getAllListings() {
        return activeListings.values().stream()
                .filter(l -> !l.isExpired())
                .collect(Collectors.toList());
    }

    public List<AuctionListing> getPlayerListings(UUID playerUUID) {
        return activeListings.values().stream()
                .filter(l -> l.getSellerUUID().equals(playerUUID))
                .collect(Collectors.toList());
    }

    public double getCommissionFee() {
        return plugin.getConfig().getDouble("auction.commission-fee", 5.0);
    }

    public void startExpirationChecker() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            List<String> expiredIds = new ArrayList<>();

            for (AuctionListing listing : activeListings.values()) {
                if (listing.isExpired()) {
                    expiredIds.add(listing.getId());

                    Player seller = Bukkit.getPlayer(listing.getSellerUUID());
                    if (seller != null && seller.isOnline()) {
                        seller.getInventory().addItem(listing.getItemStack());
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("item", listing.getItemStack().getType().name());
                        seller.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-expired", placeholders));
                    }
                }
            }

            expiredIds.forEach(this::removeListing);
        }, 1200L, 1200L);
    }

    private boolean isExpired(long expirationTime) {
        return System.currentTimeMillis() >= expirationTime;
    }
}