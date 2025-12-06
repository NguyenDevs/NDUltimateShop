package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.AuctionGUI;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
        if (!(event.getInventory().getHolder() instanceof AuctionGUI)) return;
        event.setCancelled(true);

        AuctionGUI gui = (AuctionGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        Map<String, Integer> slots = gui.getConfig().getSlotMapping();
        ItemStack clickedItem = event.getCurrentItem();

        if (slots.containsKey("close") && slot == slots.get("close")) {
            gui.getConfig().playSound(player, "click");
            player.closeInventory();
            return;
        }

        if (slots.containsKey("previous") && slot == slots.get("previous")) {
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            if (clickedItem.getType() == gui.getConfig().getFillerMaterial()) return;

            if (gui.getCurrentPage() > 0) {
                gui.getConfig().playSound(player, "click");
                gui.setCurrentPage(gui.getCurrentPage() - 1);
                gui.open();
            } else {
                gui.getConfig().playSound(player, "error");
            }
            return;
        }

        if (slots.containsKey("next") && slot == slots.get("next")) {
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            if (clickedItem.getType() == gui.getConfig().getFillerMaterial()) return;

            gui.getConfig().playSound(player, "click");
            gui.setCurrentPage(gui.getCurrentPage() + 1);
            gui.open();
            return;
        }

        AuctionListing listing = gui.getAuctionListingAt(slot);
        if (listing != null) {
            if (listing.getSellerUUID().equals(player.getUniqueId())) {
                if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    cancelListing(player, listing, gui);
                } else {
                    gui.getConfig().playSound(player, "error");
                    Map<String, String> ph = new HashMap<>();
                    ph.put("hint", "Shift + Click");
                    player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("help-admin-auction-cancel-hint", ph));
                }
            } else {
                purchaseAuction(player, listing, gui);
            }
        }
    }

    private void purchaseAuction(Player buyer, AuctionListing listing, AuctionGUI gui) {
        double originalPrice = listing.getPrice();
        double finalPrice = plugin.getCouponManager().getDiscountedPrice(buyer.getUniqueId(), originalPrice);

        if (plugin.getEconomy().getBalance(buyer) < finalPrice) {
            gui.getConfig().playSound(buyer, "error");
            Map<String, String> ph = new HashMap<>();
            ph.put("amount", String.format("%,.2f", finalPrice));
            buyer.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", ph));
            return;
        }

        plugin.getEconomy().withdrawPlayer(buyer, finalPrice);

        for (ItemStack item : buyer.getInventory().addItem(listing.getItemStack()).values()) {
            buyer.getWorld().dropItem(buyer.getLocation(), item);
        }

        Player seller = Bukkit.getPlayer(listing.getSellerUUID());
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(listing.getSellerUUID()), listing.getPrice());

        if (seller != null && seller.isOnline()) {
            Map<String, String> ph = new HashMap<>();
            ph.put("amount", String.format("%,.2f", listing.getPrice()));
            ItemStack itemStack = listing.getItemStack();
            String itemName = itemStack.getType().name();
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                itemName = itemStack.getItemMeta().getDisplayName();
            }
            ph.put("item", itemName);
            seller.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-seller-received", ph));
            seller.playSound(seller.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        plugin.getAuctionManager().removeListing(listing.getId());
        gui.getConfig().playSound(buyer, "success");

        Map<String, String> ph = new HashMap<>();
        ItemStack itemStack = listing.getItemStack();
        String itemName = itemStack.getType().name();
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            itemName = itemStack.getItemMeta().getDisplayName();
        }
        ph.put("item", itemName);
        ph.put("seller", listing.getSellerName());
        ph.put("price", String.format("%,.2f", finalPrice));
        buyer.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-bought", ph));

        gui.open();
    }

    private void cancelListing(Player player, AuctionListing listing, AuctionGUI gui) {
        plugin.getAuctionManager().removeListing(listing.getId());
        for (ItemStack item : player.getInventory().addItem(listing.getItemStack()).values()) {
            player.getWorld().dropItem(player.getLocation(), item);
        }
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-cancelled"));
        gui.getConfig().playSound(player, "click");
        gui.open();
    }
}