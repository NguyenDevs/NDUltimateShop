package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.ShopGUI;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
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
    private final Map<Player, ShopGUI> activeGUIs = new HashMap<>();

    public ShopListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIConfigManager.GUIConfig config = plugin.getConfigManager().getGUIConfig("shop");
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle());
        title = plugin.getLanguageManager().colorize(title);

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (clickedItem == null || slot >= event.getInventory().getSize()) return;

        ShopGUI gui = activeGUIs.get(player);
        if (gui == null) return;

        Map<String, Integer> slots = config.getSlotMapping();

        if (slots.containsKey("close") && slot == slots.get("close")) {
            config.playSound(player, "click"); // ADDED: Sound click
            player.closeInventory();
            activeGUIs.remove(player);
            return;
        }

        if (slots.containsKey("previous") && slot == slots.get("previous")) {
            if (gui.getCurrentPage() > 0) {
                config.playSound(player, "click"); // ADDED: Sound click
                gui.setCurrentPage(gui.getCurrentPage() - 1);
                gui.open();
            } else {
                config.playSound(player, "error"); // ADDED: Sound lỗi khi hết trang
            }
            return;
        }

        if (slots.containsKey("next") && slot == slots.get("next")) {
            config.playSound(player, "click"); // ADDED: Sound click
            gui.setCurrentPage(gui.getCurrentPage() + 1);
            gui.open();
            return;
        }

        ShopItem shopItem = gui.getShopItemAt(slot);
        if (shopItem != null) {
            purchaseItem(player, shopItem, gui, config);
        }
    }

    private void purchaseItem(Player player, ShopItem shopItem, ShopGUI gui, GUIConfigManager.GUIConfig config) {
        if (!shopItem.hasStock()) {
            config.playSound(player, "error"); // ADDED: Sound lỗi
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-not-enough-stock"));
            return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            config.playSound(player, "error"); // ADDED: Sound lỗi
            player.sendMessage(plugin.getLanguageManager().getPrefix() + " §cTúi đồ của bạn đã đầy!");
            return;
        }
        double originalPrice = shopItem.getPrice();
        double finalPrice = plugin.getCouponManager().getDiscountedPrice(player.getUniqueId(), originalPrice);

        if (plugin.getEconomy().getBalance(player) < finalPrice) {
            config.playSound(player, "error"); // ADDED: Sound lỗi
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.format("%.2f", finalPrice));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", placeholders));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, finalPrice);

        Map<Integer, ItemStack> remaining = player.getInventory().addItem(shopItem.getItemStack());
        if (!remaining.isEmpty()) {
            for (ItemStack item : remaining.values()) {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        plugin.getShopManager().purchaseItem(shopItem.getId(), 1);

        config.playSound(player, "success"); // ADDED: Sound mua thành công

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", "1");
        placeholders.put("item", shopItem.getItemStack().getType().name());
        placeholders.put("price", String.format("%.2f", finalPrice));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-item-bought", placeholders));

        gui.open();
    }

    public void registerGUI(Player player, ShopGUI gui) {
        activeGUIs.put(player, gui);
    }

    public void unregisterGUI(Player player) {
        activeGUIs.remove(player);
    }
}