package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.NightShopGUI;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class NightShopListener implements Listener {

    private final NDUltimateShop plugin;

    public NightShopListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof NightShopGUI)) return;
        event.setCancelled(true);

        NightShopGUI gui = (NightShopGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        Map<String, Integer> slots = gui.getConfig().getSlotMapping();
        ItemStack clickedItem = event.getCurrentItem();

        if (slot == gui.getSortSlot()) {
            gui.getConfig().playSound(player, "click");
            gui.handleSortClick(event.getClick());
            gui.open();
            return;
        }

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

        ShopItem shopItem = gui.getShopItemAt(slot);
        if (shopItem != null) {
            purchaseItem(player, shopItem, gui);
        }
    }

    private void purchaseItem(Player player, ShopItem shopItem, NightShopGUI gui) {
        if (!shopItem.hasStock()) {
            gui.getConfig().playSound(player, "error");
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-not-enough-stock"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            gui.getConfig().playSound(player, "error");
            player.sendMessage(plugin.getLanguageManager().getPrefix() + " §cTúi đồ đầy!");
            return;
        }

        double price = shopItem.getPrice();
        if (plugin.getEconomy().getBalance(player) < price) {
            gui.getConfig().playSound(player, "error");
            Map<String, String> ph = new HashMap<>();
            ph.put("amount", String.format("%,.2f", price));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", ph));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, price);

        for (ItemStack item : player.getInventory().addItem(shopItem.getItemStack()).values()) {
            player.getWorld().dropItem(player.getLocation(), item);
        }

        plugin.getBlackShopManager().purchaseItem(shopItem.getId(), 1);
        gui.getConfig().playSound(player, "success");

        Map<String, String> ph = new HashMap<>();
        ph.put("amount", "1");

        ItemStack item = shopItem.getItemStack();
        String itemName = item.getType().name();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            itemName = item.getItemMeta().getDisplayName();
        }
        ph.put("item", itemName);
        ph.put("price", String.format("%,.2f", price));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("nightshop-item-bought", ph));

        gui.open();
    }
}