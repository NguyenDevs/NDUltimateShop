package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.SellGUI;
import com.NguyenDevs.nDUltimateShop.managers.LanguageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SellListener implements Listener {

    private final NDUltimateShop plugin;
    private final Map<Player, SellGUI> activeSellGUIs = new HashMap<>();

    public SellListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String sellTitle = plugin.getConfig().getString("sell.gui-title", "&e&lBÁN VẬT PHẨM");
        sellTitle = com.NguyenDevs.nDUltimateShop.managers.LanguageManager.colorize(sellTitle);

        if (!title.equals(sellTitle)) return;

        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        // Handle confirm button
        if (slot == 48 && clickedItem != null && clickedItem.getType() == Material.EMERALD) {
            event.setCancelled(true);
            confirmSell(player, event.getInventory());
            return;
        }

        // Handle cancel button
        if (slot == 50 && clickedItem != null && clickedItem.getType() == Material.REDSTONE) {
            event.setCancelled(true);
            cancelSell(player, event.getInventory());
            return;
        }

        // Prevent clicking on UI elements
        if (slot == 4 || slot == 49) {
            event.setCancelled(true);
            return;
        }

        // Allow placing items in sell slots (9-44)
        if (slot >= 9 && slot < 45) {
            // Allow drag and drop
            return;
        }

        // Prevent other clicks
        if (slot < 54) {
            event.setCancelled(true);
        }
    }

    private void confirmSell(Player player, Inventory inventory) {
        double total = 0.0;
        boolean hasItems = false;

        for (int i = 9; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                hasItems = true;
                total += plugin.getSellManager().calculateSellValue(item);
                inventory.setItem(i, null);
            }
        }

        if (!hasItems) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-no-items"));
            return;
        }

        plugin.getEconomy().depositPlayer(player, total);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.format("%.2f", total));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-success", placeholders));

        player.closeInventory();
    }

    private void cancelSell(Player player, Inventory inventory) {
        // Return items to player
        for (int i = 9; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
                inventory.setItem(i, null);
            }
        }

        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-cancelled"));
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        String sellTitle = plugin.getConfig().getString("sell.gui-title", "&e&lBÁN VẬT PHẨM");
        sellTitle = com.NguyenDevs.nDUltimateShop.managers.LanguageManager.colorize(sellTitle);

        if (title.equals(sellTitle)) {
            // Return unsold items
            Inventory inventory = event.getInventory();
            for (int i = 9; i < 45; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    player.getInventory().addItem(item);
                }
            }
        }
    }
}