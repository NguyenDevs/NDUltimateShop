package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionGUI extends BaseGUI {

    private List<AuctionListing> listings;

    public AuctionGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player, "auction");
        this.listings = new ArrayList<>();
    }

    @Override
    public void open() {
        this.listings = new ArrayList<>(plugin.getAuctionManager().getAllListings());
        // Sắp xếp items
        sortItems(listings);

        Map<String, String> ph = new HashMap<>();
        ph.put("page", String.valueOf(currentPage + 1));
        ph.put("total_listings", String.valueOf(listings.size()));
        ph.put("player_listings", String.valueOf(plugin.getAuctionManager().getPlayerListings(player.getUniqueId()).size()));
        ph.put("commission", String.valueOf((int) plugin.getAuctionManager().getCommissionFee()));

        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle(), ph);
        inventory = Bukkit.createInventory(this, config.getRows() * 9, plugin.getLanguageManager().colorize(title));

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
            inventory.setItem(slot, createAuctionDisplay(listing));
        }

        Map<String, Integer> slots = config.getSlotMapping();

        ItemStack filler = new ItemStack(config.getFillerMaterial());
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        if (slots.containsKey("previous")) {
            int prevSlot = slots.get("previous");
            if (currentPage > 0) {
                inventory.setItem(prevSlot, config.getDecorativeItem("previous-button"));
            } else {
                inventory.setItem(prevSlot, filler);
            }
        }

        if (slots.containsKey("next")) {
            int nextSlot = slots.get("next");
            int maxItemsPerPage = itemSlots.size();
            if ((currentPage + 1) * maxItemsPerPage < listings.size()) {
                inventory.setItem(nextSlot, config.getDecorativeItem("next-button"));
            } else {
                inventory.setItem(nextSlot, filler);
            }
        }

        if (slots.containsKey("close")) {
            inventory.setItem(slots.get("close"), config.getDecorativeItem("close-button"));
        }
        if (slots.containsKey("info")) {
            ItemStack info = config.getDecorativeItem("info");
            processDecorativePlaceholders(info);
            inventory.setItem(slots.get("info"), info);
        }
        if (slots.containsKey("my-listings")) {
            ItemStack myItems = config.getDecorativeItem("my-listings");
            processDecorativePlaceholders(myItems);
            inventory.setItem(slots.get("my-listings"), myItems);
        }

        // Add Sort Button
        inventory.setItem(getSortSlot(), getSortButton());

        fillDecorative();
    }

    private void processDecorativePlaceholders(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        Map<String, String> ph = new HashMap<>();
        ph.put("total_listings", String.valueOf(listings.size()));
        ph.put("player_listings", String.valueOf(plugin.getAuctionManager().getPlayerListings(player.getUniqueId()).size()));
        ph.put("commission", String.valueOf((int) plugin.getAuctionManager().getCommissionFee()));

        if (meta.hasDisplayName()) {
            meta.setDisplayName(plugin.getLanguageManager().colorize(
                    plugin.getPlaceholderManager().replacePlaceholders(player, meta.getDisplayName(), ph)));
        }
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(plugin.getLanguageManager().colorize(
                        plugin.getPlaceholderManager().replacePlaceholders(player, line, ph)));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    private ItemStack createAuctionDisplay(AuctionListing listing) {
        ItemStack display = listing.getItemStack().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        Map<String, String> ph = new HashMap<>();
        ph.put("seller", listing.getSellerName());

        double originalPrice = listing.getPrice();
        double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), originalPrice);

        ph.put("price", String.format("%,.2f", finalPrice));
        ph.put("original_price", String.format("%,.2f", originalPrice));
        ph.put("time", formatTimeLeft(listing.getTimeLeft()));

        if (finalPrice < originalPrice) {
            String activeCoupon = plugin.getCouponManager().getActiveCoupon(player.getUniqueId());
            Coupon coupon = plugin.getCouponManager().getCoupon(activeCoupon);
            ph.put("discount_percent", coupon != null ? String.valueOf((int) coupon.getDiscount()) : "0");
        } else {
            ph.put("discount_percent", "0");
        }

        boolean isOwner = listing.getSellerUUID().equals(player.getUniqueId());
        ph.put("action_text", isOwner ? config.getMessage("action-cancel") : config.getMessage("action-buy"));

        List<String> configLore = config.getLoreFormat();
        if (configLore != null && !configLore.isEmpty()) {
            List<String> finalLore = new ArrayList<>();
            for (String line : configLore) {
                if (line.contains("%lore%")) {
                    if (meta.hasLore()) finalLore.addAll(meta.getLore());
                } else {
                    finalLore.add(plugin.getLanguageManager().colorize(
                            plugin.getPlaceholderManager().replacePlaceholders(player, line, ph)));
                }
            }
            meta.setLore(finalLore);
        }
        display.setItemMeta(meta);
        return display;
    }

    private String formatTimeLeft(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + " " + plugin.getLanguageManager().getMessage("time-days");
        if (hours > 0) return hours + " " + plugin.getLanguageManager().getMessage("time-hours");
        if (minutes > 0) return minutes + " " + plugin.getLanguageManager().getMessage("time-minutes");
        return seconds + " " + plugin.getLanguageManager().getMessage("time-seconds");
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