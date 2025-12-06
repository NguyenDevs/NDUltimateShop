package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.BlackShopGUI;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BlackShopListener implements Listener {

    private final NDUltimateShop plugin;

    public BlackShopListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlackShopGUI)) return;
        event.setCancelled(true);

        BlackShopGUI gui = (BlackShopGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        Map<String, Integer> slots = gui.getConfig().getSlotMapping();

        if (slots.containsKey("close") && slot == slots.get("close")) {
            gui.getConfig().playSound(player, "click");
            player.closeInventory();
            return;
        }

        if (slots.containsKey("previous") && slot == slots.get("previous")) {
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

    private void purchaseItem(Player player, ShopItem shopItem, BlackShopGUI gui) {
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
        ph.put("item", shopItem.getItemStack().getType().name());
        ph.put("price", String.format("%,.2f", price));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("blackshop-item-bought", ph));

        gui.open();
    }
}