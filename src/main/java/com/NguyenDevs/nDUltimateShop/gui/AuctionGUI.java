package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import com.NguyenDevs.nDUltimateShop.models.Coupon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionGUI extends BaseGUI {

    private static final int ITEMS_PER_PAGE = 45;

    public AuctionGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(null, 54,
                com.NguyenDevs.nDUltimateShop.managers.LanguageManager.colorize("&e&lNHÀ ĐẤU GIÁ"));

        loadItems();
        player.openInventory(inventory);
    }

    private void loadItems() {
        List<AuctionListing> listings = new ArrayList<>(plugin.getAuctionManager().getAllListings());

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, listings.size());

        for (int i = startIndex; i < endIndex; i++) {
            AuctionListing listing = listings.get(i);
            ItemStack displayItem = createAuctionDisplay(listing);
            inventory.setItem(i - startIndex, displayItem);
        }

        // Add navigation
        if (endIndex < listings.size()) {
            ItemStack nextButton = createItem(
                    Material.ARROW,
                    plugin.getLanguageManager().getMessage("gui-next-page")
            );
            inventory.setItem(inventory.getSize() - 1, nextButton);
        }

        addNavigationButtons(inventory.getSize());
    }

    private ItemStack createAuctionDisplay(AuctionListing listing) {
        ItemStack display = listing.getItemStack();
        ItemMeta meta = display.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            // Add seller info
            Map<String, String> sellerPlaceholder = new HashMap<>();
            sellerPlaceholder.put("seller", listing.getSellerName());
            lore.add(plugin.getLanguageManager().getMessage("lore-seller", sellerPlaceholder));

            // Get price with discount
            double originalPrice = listing.getPrice();
            double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), originalPrice);

            // Add discount indicator
            if (finalPrice < originalPrice) {
                String activeCoupon = plugin.getCouponManager().getActiveCoupon(player.getUniqueId());
                Coupon coupon = plugin.getCouponManager().getCoupon(activeCoupon);
                if (coupon != null) {
                    Map<String, String> discountPlaceholder = new HashMap<>();
                    discountPlaceholder.put("discount", String.format("%.0f", coupon.getDiscount()));
                    lore.add(plugin.getLanguageManager().getMessage("lore-discount", discountPlaceholder));

                    Map<String, String> originalPlaceholder = new HashMap<>();
                    originalPlaceholder.put("price", String.format("%.2f", originalPrice));
                    lore.add(plugin.getLanguageManager().getMessage("lore-original-price", originalPlaceholder));
                }
            }

            Map<String, String> pricePlaceholder = new HashMap<>();
            pricePlaceholder.put("price", String.format("%.2f", finalPrice));
            lore.add(plugin.getLanguageManager().getMessage("lore-price", pricePlaceholder));

            // Add time left
            Map<String, String> timePlaceholder = new HashMap<>();
            timePlaceholder.put("time", formatTimeLeft(listing.getTimeLeft()));
            lore.add(plugin.getLanguageManager().getMessage("lore-time-left", timePlaceholder));

            // Add action hints
            if (listing.getSellerUUID().equals(player.getUniqueId())) {
                lore.add(plugin.getLanguageManager().getMessage("lore-click-cancel"));
            } else {
                lore.add(plugin.getLanguageManager().getMessage("lore-click-buy"));
            }

            meta.setLore(lore);
            display.setItemMeta(meta);
        }

        return display;
    }

    private String formatTimeLeft(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " ngày";
        } else if (hours > 0) {
            return hours + " giờ";
        } else if (minutes > 0) {
            return minutes + " phút";
        } else {
            return seconds + " giây";
        }
    }
}