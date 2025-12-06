package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.AuctionGUI;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AuctionListener implements Listener {

    private final NDUltimateShop plugin;

    public AuctionListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        String title = event.getView().getTitle();
        if (!title.contains("NHÀ ĐẤU GIÁ")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        // Handle navigation buttons
        String displayName = clickedItem.getItemMeta().getDisplayName();
        if (displayName.equals(plugin.getLanguageManager().getMessage("gui-close"))) {
            player.closeInventory();
            return;
        }

        // Find matching listing
        for (AuctionListing listing : plugin.getAuctionManager().getAllListings()) {
            if (isSameItem(clickedItem, listing.getItemStack())) {
                if (event.getClick() == ClickType.SHIFT_LEFT &&
                        listing.getSellerUUID().equals(player.getUniqueId())) {
                    // Cancel listing
                    cancelListing(player, listing);
                } else if (!listing.getSellerUUID().equals(player.getUniqueId())) {
                    // Buy item
                    purchaseAuction(player, listing);
                }
                return;
            }
        }
    }

    private void purchaseAuction(Player buyer, AuctionListing listing) {
        double originalPrice = listing.getPrice();
        double finalPrice = plugin.getCouponManager().getDiscountedPrice(buyer.getUniqueId(), originalPrice);

        if (plugin.getEconomy().getBalance(buyer) < finalPrice) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.format("%.2f", finalPrice));
            buyer.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", placeholders));
            return;
        }

        // Process purchase
        plugin.getEconomy().withdrawPlayer(buyer, finalPrice);
        buyer.getInventory().addItem(listing.getItemStack());

        // Pay seller
        Player seller = Bukkit.getPlayer(listing.getSellerUUID());
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(listing.getSellerUUID()), listing.getPrice());

        if (seller != null && seller.isOnline()) {
            Map<String, String> sellerPlaceholders = new HashMap<>();
            sellerPlaceholders.put("amount", String.format("%.2f", listing.getPrice()));
            sellerPlaceholders.put("item", listing.getItemStack().getType().name());
            seller.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-seller-received", sellerPlaceholders));
        }

        // Remove listing
        plugin.getAuctionManager().removeListing(listing.getId());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", listing.getItemStack().getType().name());
        placeholders.put("seller", listing.getSellerName());
        placeholders.put("price", String.format("%.2f", finalPrice));
        buyer.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-bought", placeholders));

        // Reopen GUI
        new AuctionGUI(plugin, buyer).open();
    }

    private void cancelListing(Player player, AuctionListing listing) {
        plugin.getAuctionManager().removeListing(listing.getId());
        player.getInventory().addItem(listing.getItemStack());
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-cancelled"));

        new AuctionGUI(plugin, player).open();
    }

    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1.getType() != item2.getType()) return false;
        if (!item1.hasItemMeta() && !item2.hasItemMeta()) return true;
        if (!item1.hasItemMeta() || !item2.hasItemMeta()) return false;

        return item1.getItemMeta().getDisplayName().equals(item2.getItemMeta().getDisplayName());
    }
}