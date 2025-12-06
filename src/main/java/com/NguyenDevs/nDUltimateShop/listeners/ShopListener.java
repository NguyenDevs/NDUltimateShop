package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.ShopGUI;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShopListener implements Listener {

    private final NDUltimateShop plugin;

    public ShopListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI)) return;
        event.setCancelled(true);

        ShopGUI gui = (ShopGUI) event.getInventory().getHolder();
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

        ShopItem item = gui.getShopItemAt(slot);
        if (item != null) purchaseItem(player, item, gui);
    }

    private void purchaseItem(Player player, ShopItem shopItem, ShopGUI gui) {
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

        double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), shopItem.getPrice());

        if (plugin.getEconomy().getBalance(player) < finalPrice) {
            gui.getConfig().playSound(player, "error");
            Map<String, String> ph = new HashMap<>();
            ph.put("amount", String.format("%,.2f", finalPrice));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", ph));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, finalPrice);
        for (ItemStack drop : player.getInventory().addItem(shopItem.getItemStack()).values()) {
            player.getWorld().dropItem(player.getLocation(), drop);
        }

        plugin.getShopManager().purchaseItem(shopItem.getId(), 1);
        gui.getConfig().playSound(player, "success");

        Map<String, String> ph = new HashMap<>();
        ph.put("amount", "1");
        ItemStack item = shopItem.getItemStack();
        String itemName = item.getType().name();
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            itemName = item.getItemMeta().getDisplayName();
        }
        ph.put("item", itemName);
        ph.put("price", String.format("%,.2f", finalPrice));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-bought", ph));

        gui.open();
    }
}