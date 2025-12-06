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
        String mainTitlePart = configTitleRaw.split("\\[")[0].trim();
        mainTitlePart = plugin.getLanguageManager().colorize(mainTitlePart);

        if (!event.getView().getTitle().startsWith(mainTitlePart)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (clickedItem == null || slot >= event.getInventory().getSize()) return;

        BlackShopGUI gui = activeGUIs.get(player);
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

        ShopItem shopItem = gui.getShopItemAt(slot);
        if (shopItem != null) {
            purchaseItem(player, shopItem, gui);
        }
    }

    private void purchaseItem(Player player, ShopItem shopItem, BlackShopGUI gui) {
        if (!shopItem.hasStock()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("shop-not-enough-stock"));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendMessage(plugin.getLanguageManager().getPrefix() + " §cTúi đồ của bạn đã đầy!");
            return;
        }

        double price = shopItem.getPrice();

        if (plugin.getEconomy().getBalance(player) < price) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
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

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);

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