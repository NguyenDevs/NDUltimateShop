package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SellGUI {

    private final NDUltimateShop plugin;
    private final Player player;
    private Inventory inventory;
    private final GUIConfigManager.GUIConfig config;

    public SellGUI(NDUltimateShop plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.config = plugin.getConfigManager().getGUIConfig("sell");
    }

    public void open() {
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle());
        int rows = config.getRows();
        inventory = Bukkit.createInventory(null, rows * 9,
                plugin.getLanguageManager().colorize(title));

        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        Map<String, Integer> slots = config.getSlotMapping();

        if (slots.containsKey("info")) {
            ItemStack infoItem = config.getDecorativeItem("info");
            if (infoItem != null) {
                inventory.setItem(slots.get("info"), infoItem);
            }
        }

        if (slots.containsKey("confirm")) {
            ItemStack confirmButton = createConfirmButton();
            inventory.setItem(slots.get("confirm"), confirmButton);
        }

        if (slots.containsKey("cancel")) {
            ItemStack cancelButton = config.getDecorativeItem("cancel");
            if (cancelButton != null) {
                inventory.setItem(slots.get("cancel"), cancelButton);
            }
        }

        updateTotalDisplay();
        fillDecorative();
    }

    public void updateTotalDisplay() {
        Map<String, Integer> slots = config.getSlotMapping();
        if (!slots.containsKey("total-value")) return;

        double total = calculateTotal();

        ItemStack totalItem = config.getDecorativeItem("total-value");
        if (totalItem != null) {
            ItemMeta meta = totalItem.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("total", String.format("%.2f", total));

                for (String line : meta.getLore()) {
                    lore.add(plugin.getPlaceholderManager().replacePlaceholders(player, line, placeholders));
                }
                meta.setLore(lore);
                totalItem.setItemMeta(meta);
            }
            inventory.setItem(slots.get("total-value"), totalItem);
        }
    }

    private ItemStack createConfirmButton() {
        ItemStack button = config.getDecorativeItem("confirm");
        if (button != null) {
            ItemMeta meta = button.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("total", String.format("%.2f", calculateTotal()));

                for (String line : meta.getLore()) {
                    lore.add(plugin.getPlaceholderManager().replacePlaceholders(player, line, placeholders));
                }
                meta.setLore(lore);
                button.setItemMeta(meta);
            }
        }
        return button;
    }

    private double calculateTotal() {
        double total = 0.0;
        List<Integer> itemSlots = config.getItemSlots();

        for (int slot : itemSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                total += plugin.getSellManager().calculateSellValue(item);
            }
        }

        return total;
    }

    private void fillDecorative() {
        List<Integer> fillerSlots = config.getFillerSlots();
        if (fillerSlots.isEmpty()) return;

        ItemStack filler = new ItemStack(config.getFillerMaterial());
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int slot : fillerSlots) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    public boolean confirmSell() {
        double total = 0.0;
        boolean hasItems = false;
        List<Integer> itemSlots = config.getItemSlots();

        for (int slot : itemSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                hasItems = true;
                total += plugin.getSellManager().calculateSellValue(item);
                inventory.setItem(slot, null);
            }
        }

        if (!hasItems) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-no-items"));
            return false;
        }

        plugin.getEconomy().depositPlayer(player, total);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("amount", String.format("%.2f", total));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-success", placeholders));

        return true;
    }

    public void cancelSell() {
        List<Integer> itemSlots = config.getItemSlots();

        for (int slot : itemSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                if (!remaining.isEmpty()) {
                    for (ItemStack drop : remaining.values()) {
                        player.getWorld().dropItem(player.getLocation(), drop);
                    }
                }
                inventory.setItem(slot, null);
            }
        }

        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-cancelled"));
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public GUIConfigManager.GUIConfig getConfig() {
        return config;
    }
}