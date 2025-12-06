package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.ShopGUI;
import com.NguyenDevs.nDUltimateShop.managers.LanguageManager;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
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
        if (!(event.getWhoClicked() instanceof Player)) return;

        String title = event.getView().getTitle();
        String shopTitle = plugin.getConfig().getString("shop.gui-title", "&6&lCỬA HÀNG");
        shopTitle = com.NguyenDevs.nDUltimateShop.managers.LanguageManager.colorize(shopTitle);

        if (!title.equals(shopTitle)) return;

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

        // Handle item purchase
        for (ShopItem shopItem : plugin.getShopManager().getAllShopItems()) {
            if (isSameItem(clickedItem, shopItem.getItemStack())) {
                purchaseItem(player, shopItem);
                return;
            }
        }
    }

    private void purchaseItem(Player player, ShopItem shopItem) {
        if (!shopItem.hasStock()) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-not-enough-stock"));
            return;
        }

        double originalPrice = shopItem.getPrice();
        double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), originalPrice);

        if (plugin.getEconomy().getBalance(player) < finalPrice) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.format("%.2f", finalPrice));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", placeholders));
            return;
        }

        // Process purchase
        plugin.getEconomy().withdrawPlayer(player, finalPrice);
        player.getInventory().addItem(shopItem.getItemStack());
        plugin.getShopManager().purchaseItem(shopItem.getId(), 1);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", "1");
        placeholders.put("item", shopItem.getItemStack().getType().name());
        placeholders.put("price", String.format("%.2f", finalPrice));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-bought", placeholders));

        // Reopen GUI to update
        new ShopGUI(plugin, player).open();
    }

    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1.getType() != item2.getType()) return false;
        if (!item1.hasItemMeta() && !item2.hasItemMeta()) return true;
        if (!item1.hasItemMeta() || !item2.hasItemMeta()) return false;

        return item1.getItemMeta().getDisplayName().equals(item2.getItemMeta().getDisplayName());
    }
}