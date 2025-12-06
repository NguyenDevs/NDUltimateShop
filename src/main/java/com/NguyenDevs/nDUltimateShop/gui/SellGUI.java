package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class SellGUI extends BaseGUI {

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
        // ... (giữ nguyên logic xử lý lore như cũ) ...
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
        double total = calculateTotal();
        boolean hasItems = total > 0;

        if (!hasItems) {
            player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-no-items"));
            return false;
        }

        // Xóa item trước khi cộng tiền để tránh lỗi logic
        for (int slot : config.getItemSlots()) {
            inventory.setItem(slot, null);
        }

        plugin.getEconomy().depositPlayer(player, total);
        Map<String, String> ph = new HashMap<>();
        ph.put("amount", String.format("%,.2f", total));
        player.sendMessage(plugin.getLanguageManager().getPrefixedMessage("sell-success", ph));
        return true;
    }

    public void cancelSell() {
        for (int slot : config.getItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                // Trả lại item vào kho người chơi
                for (ItemStack drop : player.getInventory().addItem(item).values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
                // QUAN TRỌNG: Xóa item khỏi GUI ngay lập tức để tránh trả lại lần 2
                inventory.setItem(slot, null);
            }
        }
    }
}