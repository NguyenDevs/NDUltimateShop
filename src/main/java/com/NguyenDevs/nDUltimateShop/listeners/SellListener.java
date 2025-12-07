package com.NguyenDevs.nDUltimateShop.listeners;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.gui.SellGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.List;
import java.util.Map;

public class SellListener implements Listener {

    private final NDUltimateShop plugin;

    public SellListener(NDUltimateShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SellGUI)) return;

        SellGUI gui = (SellGUI) event.getInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        Map<String, Integer> slots = gui.getConfig().getSlotMapping();
        List<Integer> itemSlots = gui.getConfig().getItemSlots();

        if (slots.containsKey("confirm") && slot == slots.get("confirm")) {
            event.setCancelled(true);
            gui.getConfig().playSound(player, "success");
            player.closeInventory();
            return;
        }

        if (slots.containsKey("cancel") && slot == slots.get("cancel")) {
            event.setCancelled(true);
            gui.cancelSell();
            gui.getConfig().playSound(player, "click");
            player.closeInventory();
            return;
        }

        if (itemSlots.contains(slot)) {
            gui.getConfig().playSound(player, "click");
            plugin.getServer().getScheduler().runTask(plugin, gui::updateGUI);
            return;
        }

        if (slot < event.getInventory().getSize()) {
            event.setCancelled(true);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, gui::updateGUI);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SellGUI) {
            SellGUI gui = (SellGUI) event.getInventory().getHolder();
            if (event.getRawSlots().stream().anyMatch(s -> s < event.getInventory().getSize() && !gui.getConfig().getItemSlots().contains(s))) {
                event.setCancelled(true);
            } else {
                plugin.getServer().getScheduler().runTask(plugin, gui::updateGUI);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SellGUI) {
            ((SellGUI) event.getInventory().getHolder()).confirmSell();
        }
    }
}