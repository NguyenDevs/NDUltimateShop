package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.SellGUI;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellListener implements Listener {

    private final NDUltimateShop plugin;
    private final Map<Player, SellGUI> activeGUIs = new HashMap<>();

    public SellListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIConfigManager.GUIConfig config = plugin.getConfigManager().getGUIConfig("sell");
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle());
        title = plugin.getLanguageManager().colorize(title);

        if (!event.getView().getTitle().equals(title)) return;

        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlot();
        SellGUI gui = activeGUIs.get(player);
        if (gui == null) return;

        Map<String, Integer> slots = config.getSlotMapping();
        List<Integer> itemSlots = config.getItemSlots();

        if (slots.containsKey("confirm") && slot == slots.get("confirm")) {
            event.setCancelled(true);
            if (gui.confirmSell()) {
                player.closeInventory();
                activeGUIs.remove(player);
            }
            return;
        }

        if (slots.containsKey("cancel") && slot == slots.get("cancel")) {
            event.setCancelled(true);
            gui.cancelSell();
            player.closeInventory();
            activeGUIs.remove(player);
            return;
        }

        if (itemSlots.contains(slot)) {
            if (slot < event.getInventory().getSize()) {
                if (event.getClick() == ClickType.NUMBER_KEY) {
                    event.setCancelled(true);
                    return;
                }
                plugin.getServer().getScheduler().runTaskLater(plugin, gui::updateTotalDisplay, 1L);
                return;
            }
        }

        if (slot < event.getInventory().getSize()) {
            event.setCancelled(true);
        } else if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        GUIConfigManager.GUIConfig config = plugin.getConfigManager().getGUIConfig("sell");
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle());
        title = plugin.getLanguageManager().colorize(title);

        if (!event.getView().getTitle().equals(title)) return;

        SellGUI gui = activeGUIs.get(player);
        if (gui == null) return;

        List<Integer> itemSlots = config.getItemSlots();
        boolean affectsItemSlots = event.getRawSlots().stream().anyMatch(itemSlots::contains);
        boolean affectsOtherSlots = event.getRawSlots().stream().anyMatch(s -> !itemSlots.contains(s) && s < event.getInventory().getSize());

        if (affectsOtherSlots) {
            event.setCancelled(true);
            return;
        }

        if (affectsItemSlots) {
            plugin.getServer().getScheduler().runTaskLater(plugin, gui::updateTotalDisplay, 1L);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        GUIConfigManager.GUIConfig config = plugin.getConfigManager().getGUIConfig("sell");
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle());
        title = plugin.getLanguageManager().colorize(title);

        if (!event.getView().getTitle().equals(title)) return;

        SellGUI gui = activeGUIs.get(player);
        if (gui != null) {
            List<Integer> itemSlots = config.getItemSlots();
            for (int slot : itemSlots) {
                ItemStack item = event.getInventory().getItem(slot);
                if (item != null) {
                    Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                    if (!remaining.isEmpty()) {
                        for (ItemStack drop : remaining.values()) {
                            player.getWorld().dropItem(player.getLocation(), drop);
                        }
                    }
                }
            }
            activeGUIs.remove(player);
        }
    }

    public void registerGUI(Player player, SellGUI gui) {
        activeGUIs.put(player, gui);
    }

    public void unregisterGUI(Player player) {
        activeGUIs.remove(player);
    }
}