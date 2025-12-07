package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellGUI extends BaseGUI {

    private boolean processed = false;

    public SellGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player, "sell");
    }

    @Override
    public void open() {
        String title = plugin.getPlaceholderManager().replacePlaceholders(player, config.getTitle());
        inventory = Bukkit.createInventory(this, config.getRows() * 9, plugin.getLanguageManager().colorize(title));
        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        Map<String, Integer> slots = config.getSlotMapping();
        if (slots.containsKey("info")) inventory.setItem(slots.get("info"), config.getDecorativeItem("info"));
        if (slots.containsKey("cancel")) inventory.setItem(slots.get("cancel"), config.getDecorativeItem("cancel"));
        if (slots.containsKey("confirm")) updateConfirmButton();
        if (slots.containsKey("total-value")) updateTotalValue();
        fillDecorative();
    }

    public void updateGUI() {
        Map<String, Integer> slots = config.getSlotMapping();
        if (slots.containsKey("total-value")) updateTotalValue();
        if (slots.containsKey("confirm")) updateConfirmButton();
    }

    private void updateTotalValue() {
        int slot = config.getSlotMapping().get("total-value");
        ItemStack item = config.getDecorativeItem("total-value");
        processPlaceholders(item);
        inventory.setItem(slot, item);
    }

    private void updateConfirmButton() {
        int slot = config.getSlotMapping().get("confirm");
        ItemStack item = config.getDecorativeItem("confirm");
        processPlaceholders(item);
        inventory.setItem(slot, item);
    }

    private void processPlaceholders(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        Map<String, String> ph = new HashMap<>();
        ph.put("total", String.format("%,.2f", calculateTotal()));

        if (meta.hasDisplayName()) {
            meta.setDisplayName(plugin.getLanguageManager().colorize(
                    plugin.getPlaceholderManager().replacePlaceholders(player, meta.getDisplayName(), ph)
            ));
        }

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(plugin.getLanguageManager().colorize(
                        plugin.getPlaceholderManager().replacePlaceholders(player, line, ph)
                ));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    public double calculateTotal() {
        double total = 0.0;
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                total += plugin.getSellManager().calculateSellValue(item);
            }
        }
        return total;
    }

    public boolean confirmSell() {
        if (processed) return false;
        processed = true;

        double total = 0.0;
        List<ItemStack> unsoldItems = new ArrayList<>();
        boolean soldAnything = false;

        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                double value = plugin.getSellManager().calculateSellValue(item);
                if (value > 0) {
                    total += value;
                    soldAnything = true;
                } else {
                    unsoldItems.add(item);
                }
                inventory.setItem(slot, null);
            }
        }

        if (!soldAnything && unsoldItems.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-no-items"));
            return false;
        }

        if (total > 0) {
            plugin.getEconomy().depositPlayer(player, total);
            Map<String, String> ph = new HashMap<>();
            ph.put("amount", String.format("%,.2f", total));
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-success", ph));
        }

        if (!unsoldItems.isEmpty()) {
            for (ItemStack item : unsoldItems) {
                for (ItemStack drop : player.getInventory().addItem(item).values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
            }
            if (soldAnything) {
                player.sendMessage(plugin.getLanguageManager().getPrefix() + " §eMột số vật phẩm không thể bán đã được trả lại.");
            } else {
                player.sendMessage(plugin.getLanguageManager().getPrefix() + " §cVật phẩm này không thể bán!");
            }
        }

        return true;
    }

    public void cancelSell() {
        if (processed) return;
        processed = true;

        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                for (ItemStack drop : player.getInventory().addItem(item).values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
                inventory.setItem(slot, null);
            }
        }
    }
}