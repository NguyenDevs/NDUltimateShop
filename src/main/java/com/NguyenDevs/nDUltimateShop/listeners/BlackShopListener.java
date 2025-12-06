package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.BlackShopGUI;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BlackShopListener implements Listener {

    private final NDUltimateShop plugin;
    private final Map<Player, BlackShopGUI> activeGUIs = new HashMap<>();

    public BlackShopListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIConfigManager.GUIConfig config = plugin.getConfigManager().getGUIConfig("blackshop");

        String configTitleRaw = config.getTitle();
        // Lấy phần title chính trước dấu [ để so sánh
        String mainTitlePart = configTitleRaw.contains("[") ? configTitleRaw.split("\\[")[0].trim() : configTitleRaw;
        mainTitlePart = plugin.getLanguageManager().colorize(mainTitlePart);

        if (!event.getView().getTitle().startsWith(mainTitlePart)) return;

        event.setCancelled(true);

        // FIX: Check session
        BlackShopGUI gui = activeGUIs.get(player);
        if (gui == null) {
            player.closeInventory();
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (clickedItem == null || slot >= event.getInventory().getSize()) return;

        Map<String, Integer> slots = config.getSlotMapping();

        if (slots.containsKey("close") && slot == slots.get("close")) {
            config.playSound(player, "click");
            player.closeInventory();
            activeGUIs.remove(player);
            return;
        }

        if (slots.containsKey("previous") && slot == slots.get("previous")) {
            if (gui.getCurrentPage() > 0) {
                config.playSound(player, "click");
                gui.setCurrentPage(gui.getCurrentPage() - 1);
                gui.open();
            } else {
                config.playSound(player, "error");
            }
            return;
        }

        if (slots.containsKey("next") && slot == slots.get("next")) {
            config.playSound(player, "click");
            gui.setCurrentPage(gui.getCurrentPage() + 1);
            gui.open();
            return;
        }

        ShopItem shopItem = gui.getShopItemAt(slot);
        if (shopItem != null) {
            purchaseItem(player, shopItem, gui, config);
        }
    }

    private void purchaseItem(Player player, ShopItem shopItem, BlackShopGUI gui, GUIConfigManager.GUIConfig config) {
        if (!shopItem.hasStock()) {
            config.playSound(player, "error");
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-not-enough-stock"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            config.playSound(player, "error");
            player.sendMessage(plugin.getLanguageManager().getPrefix() + " §cTúi đồ của bạn đã đầy!");
            return;
        }

        double price = shopItem.getPrice();

        if (plugin.getEconomy().getBalance(player) < price) {
            config.playSound(player, "error");
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.format("%.2f", price));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("not-enough-money", placeholders));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, price);

        Map<Integer, ItemStack> remaining = player.getInventory().addItem(shopItem.getItemStack());
        if (!remaining.isEmpty()) {
            for (ItemStack item : remaining.values()) {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        plugin.getBlackShopManager().purchaseItem(shopItem.getId(), 1);

        config.playSound(player, "success");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", "1");
        placeholders.put("item", shopItem.getItemStack().getType().name());
        placeholders.put("price", String.format("%.2f", price));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("blackshop-item-bought", placeholders));

        gui.open();
    }

    public void registerGUI(Player player, BlackShopGUI gui) {
        activeGUIs.put(player, gui);
    }

    public void unregisterGUI(Player player) {
        activeGUIs.remove(player);
    }
}