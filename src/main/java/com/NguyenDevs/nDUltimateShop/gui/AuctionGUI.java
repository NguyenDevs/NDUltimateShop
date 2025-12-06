package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AuctionGUI {

    private final NDUltimateShop plugin;
    private final Player player;
    private Inventory inventory;
    private int currentPage = 0;
    private final GUIConfigManager.GUIConfig config;
    private final List<AuctionListing> listings;

    public AuctionGUI(NDUltimateShop plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.config = plugin.getConfigManager().getGUIConfig("auction");
        this.listings = new ArrayList<>(plugin.getAuctionManager().getAllListings());
    }

    public void open() {
        Map<String, String> titlePlaceholders = new HashMap<>();
        titlePlaceholders.put("page", String.valueOf(currentPage + 1));
        titlePlaceholders.put("total_listings", String.valueOf(listings.size()));
        titlePlaceholders.put("player_listings", String.valueOf(
                plugin.getAuctionManager().getPlayerListings(player.getUniqueId()).size()));
        titlePlaceholders.put("commission", String.valueOf((int)plugin.getAuctionManager().getCommissionFee()));

        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle(), titlePlaceholders);
        int rows = config.getRows();
        inventory = Bukkit.createInventory(null, rows * 9,
                plugin.getLanguageManager().colorize(title));

        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        List<Integer> itemSlots = config.getItemSlots();
        int startIndex = currentPage * itemSlots.size();
        int endIndex = Math.min(startIndex + itemSlots.size(), listings.size());

        for (int i = startIndex; i < endIndex; i++) {
            AuctionListing listing = listings.get(i);
            int slot = itemSlots.get(i - startIndex);
            ItemStack displayItem = createAuctionDisplay(listing);
            inventory.setItem(slot, displayItem);
        }

        Map<String, Integer> slots = config.getSlotMapping();

        if (currentPage > 0 && slots.containsKey("previous")) {
            ItemStack prevButton = config.getDecorativeItem("previous-button");
            inventory.setItem(slots.get("previous"), prevButton);
        }

        if (endIndex < listings.size() && slots.containsKey("next")) {
            ItemStack nextButton = config.getDecorativeItem("next-button");
            inventory.setItem(slots.get("next"), nextButton);
        }

        if (slots.containsKey("close")) {
            ItemStack closeButton = config.getDecorativeItem("close-button");
            inventory.setItem(slots.get("close"), closeButton);
        }

        if (slots.containsKey("info")) {
            ItemStack infoItem = createInfoItem();
            inventory.setItem(slots.get("info"), infoItem);
        }

        if (slots.containsKey("my-listings")) {
            ItemStack myListingsButton = createMyListingsButton();
            inventory.setItem(slots.get("my-listings"), myListingsButton);
        }

        fillDecorative();
    }

    private ItemStack createAuctionDisplay(AuctionListing listing) {
        ItemStack display = listing.getItemStack().clone();
        ItemMeta meta = display.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("seller", listing.getSellerName());

            double originalPrice = listing.getPrice();
            double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), originalPrice);

            placeholders.put("price", String.format("%.2f", finalPrice));
            placeholders.put("original_price", String.format("%.2f", originalPrice));
            placeholders.put("time", formatTimeLeft(listing.getTimeLeft()));

            lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                    config.getMessage("lore-seller"), placeholders));

            if (finalPrice < originalPrice) {
                String activeCoupon = plugin.getCouponManager().getActiveCoupon(player.getUniqueId());
                Coupon coupon = plugin.getCouponManager().getCoupon(activeCoupon);
                if (coupon != null) {
                    placeholders.put("discount", String.format("%.0f", coupon.getDiscount()));
                    lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                            config.getMessage("lore-discount"), placeholders));
                    lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                            config.getMessage("lore-original-price"), placeholders));
                }
            }

            lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                    config.getMessage("lore-price"), placeholders));
            lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                    config.getMessage("lore-time-left"), placeholders));

            if (listing.getSellerUUID().equals(player.getUniqueId())) {
                lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                        config.getMessage("lore-click-cancel"), placeholders));
            } else {
                lore.add(plugin.getPlaceholderManager().replacePlaceholders(player,
                        config.getMessage("lore-click-buy"), placeholders));
            }

            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(plugin.getLanguageManager().colorize(line));
            }
            meta.setLore(coloredLore);
            display.setItemMeta(meta);
        }

        return display;
    }

    private ItemStack createInfoItem() {
        ItemStack infoItem = config.getDecorativeItem("info");
        if (infoItem == null) return null;

        ItemMeta meta = infoItem.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("total_listings", String.valueOf(listings.size()));
            placeholders.put("player_listings", String.valueOf(
                    plugin.getAuctionManager().getPlayerListings(player.getUniqueId()).size()));
            placeholders.put("commission", String.valueOf((int)plugin.getAuctionManager().getCommissionFee()));

            for (String line : meta.getLore()) {
                lore.add(plugin.getPlaceholderManager().replacePlaceholders(player, line, placeholders));
            }
            meta.setLore(lore);
            infoItem.setItemMeta(meta);
        }

        return infoItem;
    }

    private ItemStack createMyListingsButton() {
        ItemStack button = config.getDecorativeItem("my-listings");
        if (button == null) return null;

        ItemMeta meta = button.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("count", String.valueOf(
                    plugin.getAuctionManager().getPlayerListings(player.getUniqueId()).size()));

            for (String line : meta.getLore()) {
                lore.add(plugin.getPlaceholderManager().replacePlaceholders(player, line, placeholders));
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
        }

        return button;
    }

    private void fillDecorative() {
        List<Integer> fillerSlots = config.getFillerSlots();
        if (fillerSlots.isEmpty()) return;

        ItemStack filler = new ItemStack(config.getFillerMaterial());
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int slot : fillerSlots) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private String formatTimeLeft(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " " + plugin.getLanguageManager().getMessage("time-days");
        } else if (hours > 0) {
            return hours + " " + plugin.getLanguageManager().getMessage("time-hours");
        } else if (minutes > 0) {
            return minutes + " " + plugin.getLanguageManager().getMessage("time-minutes");
        } else {
            return seconds + " " + plugin.getLanguageManager().getMessage("time-seconds");
        }
    }

    public Player getPlayer() {
        return player;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public AuctionListing getAuctionListingAt(int slot) {
        List<Integer> itemSlots = config.getItemSlots();
        int index = itemSlots.indexOf(slot);
        if (index == -1) return null;

        int actualIndex = currentPage * itemSlots.size() + index;
        if (actualIndex >= listings.size()) return null;

        return listings.get(actualIndex);
    }
}