package com.NguyenDevs.nDUltimateShop.managers;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

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

    public int getPlayerListingLimit(Player player) {
        String prefix = "ndshop.auction.limit.";
        int maxLimit = -1;

        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission();
            if (perm.startsWith(prefix)) {
                try {
                    int amount = Integer.parseInt(perm.substring(prefix.length()));
                    if (amount > maxLimit) {
                        maxLimit = amount;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (maxLimit != -1) {
            return maxLimit;
        }

        return plugin.getConfig().getInt("auction.max-listings-per-player", 10);
    }

    public void loadAuctions() {
        activeListings.clear();
        FileConfiguration data = plugin.getConfigManager().getDataConfig("auctions.yml");

        ConfigurationSection listingsSection = data.getConfigurationSection("listings");
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

        plugin.getLogger().info("Da tai " + activeListings.size() + " mat hang dau gia tu data/auctions.yml!");
    }

    public void saveAuctions() {
        FileConfiguration data = plugin.getConfigManager().getDataConfig("auctions.yml");
        data.set("listings", null);

        for (Map.Entry<String, AuctionListing> entry : activeListings.entrySet()) {
            String key = "listings." + entry.getKey();
            AuctionListing listing = entry.getValue();

            data.set(key + ".seller", listing.getSellerUUID().toString());
            data.set(key + ".sellerName", listing.getSellerName());
            data.set(key + ".item", listing.getItemStack());
            data.set(key + ".price", listing.getPrice());
            data.set(key + ".expiration", listing.getExpirationTime());
        }

        plugin.getConfigManager().saveData("auctions.yml");
    }

    public String createListing(Player seller, ItemStack item, double price) {
        int maxListings = getPlayerListingLimit(seller);
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