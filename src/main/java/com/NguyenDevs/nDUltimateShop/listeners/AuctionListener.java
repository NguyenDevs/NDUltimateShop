package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.AuctionGUI;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import org.bukkit.Bukkit;
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
    private final Map<Player, AuctionGUI> activeGUIs = new HashMap<>();

    public AuctionListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIConfigManager.GUIConfig config = plugin.getConfigManager().getGUIConfig("auction");

        String configTitle = plugin.getLanguageManager().stripColor(
                plugin.getLanguageManager().colorize(config.getTitle())
        );
        String currentTitle = event.getView().getTitle();

        String baseTitle = config.getTitle().split("-")[0].trim();
        baseTitle = plugin.getLanguageManager().stripColor(plugin.getLanguageManager().colorize(baseTitle));

        if (!plugin.getLanguageManager().stripColor(currentTitle).contains(baseTitle)) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (clickedItem == null || slot >= event.getInventory().getSize()) return;

        AuctionGUI gui = activeGUIs.get(player);
        if (gui == null) return;

        Map<String, Integer> slots = config.getSlotMapping();

        if (slots.containsKey("close") && slot == slots.get("close")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.closeInventory();
            activeGUIs.remove(player);
            return;
        }

        if (slots.containsKey("previous") && slot == slots.get("previous")) {
            if (gui.getCurrentPage() > 0) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                gui.setCurrentPage(gui.getCurrentPage() - 1);
                gui.open();
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            return;
        }

        if (slots.containsKey("next") && slot == slots.get("next")) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            gui.setCurrentPage(gui.getCurrentPage() + 1);
            gui.open();
            return;
        }

        AuctionListing listing = gui.getAuctionListingAt(slot);
        if (listing != null) {
            if (event.getClick() == ClickType.SHIFT_LEFT && listing.getSellerUUID().equals(player.getUniqueId())) {
                cancelListing(player, listing, gui);
            } else if (!listing.getSellerUUID().equals(player.getUniqueId())) {
                purchaseAuction(player, listing, gui);
            }
        }
    }

    private void purchaseAuction(Player buyer, AuctionListing listing, AuctionGUI gui) {
        double originalPrice = listing.getPrice();
        double finalPrice = plugin.getCouponManager().getDiscountedPrice(buyer.getUniqueId(), originalPrice);

        if (plugin.getEconomy().getBalance(buyer) < finalPrice) {
            buyer.playSound(buyer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.format("%.2f", finalPrice));
            buyer.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", placeholders));
            return;
        }

        plugin.getEconomy().withdrawPlayer(buyer, finalPrice);

        Map<Integer, ItemStack> remaining = buyer.getInventory().addItem(listing.getItemStack());
        if (!remaining.isEmpty()) {
            for (ItemStack item : remaining.values()) {
                buyer.getWorld().dropItem(buyer.getLocation(), item);
            }
        }

        Player seller = Bukkit.getPlayer(listing.getSellerUUID());
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(listing.getSellerUUID()), listing.getPrice());

        if (seller != null && seller.isOnline()) {
            Map<String, String> sellerPlaceholders = new HashMap<>();
            sellerPlaceholders.put("amount", String.format("%.2f", listing.getPrice()));
            sellerPlaceholders.put("item", listing.getItemStack().getType().name());
            seller.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-seller-received", sellerPlaceholders));
            seller.playSound(seller.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        plugin.getAuctionManager().removeListing(listing.getId());
        buyer.playSound(buyer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", listing.getItemStack().getType().name());
        placeholders.put("seller", listing.getSellerName());
        placeholders.put("price", String.format("%.2f", finalPrice));
        buyer.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-bought", placeholders));

        gui.open();
    }

    private void cancelListing(Player player, AuctionListing listing, AuctionGUI gui) {
        plugin.getAuctionManager().removeListing(listing.getId());

        Map<Integer, ItemStack> remaining = player.getInventory().addItem(listing.getItemStack());
        if (!remaining.isEmpty()) {
            for (ItemStack item : remaining.values()) {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("auction-item-cancelled"));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        gui.open();
    }

    public void registerGUI(Player player, AuctionGUI gui) {
        activeGUIs.put(player, gui);
    }

    public void unregisterGUI(Player player) {
        activeGUIs.remove(player);
    }
}