package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellGUI extends BaseGUI {

    private final Inventory sellInventory;

    public SellGUI(NDUltimateShop plugin, Player player) {
        super(plugin, player);
        this.sellInventory = Bukkit.createInventory(null, 54);
    }

    @Override
    public void open() {
        String title = plugin.getConfig().getString("sell.gui-title", "&e&lBÁN VẬT PHẨM");
        int rows = plugin.getConfig().getInt("sell.gui-rows", 6);
        inventory = Bukkit.createInventory(null, rows * 9,
                com.NguyenDevs.nDUltimateShop.managers.LanguageManager.colorize(title));

        setupGUI();
        player.openInventory(inventory);
    }

    private void setupGUI() {
        // Info item
        ItemStack info = createItem(
                Material.BOOK,
                "&e&lHƯỚNG DẪN",
                "&7Kéo thả vật phẩm vào đây để bán",
                "&7Nhấn nút xác nhận để hoàn tất"
        );
        inventory.setItem(4, info);

        // Confirm button
        ItemStack confirm = createItem(
                Material.EMERALD,
                plugin.getLanguageManager().getMessage("gui-confirm"),
                "&7Nhấn để bán tất cả vật phẩm"
        );
        inventory.setItem(48, confirm);

        // Cancel button
        ItemStack cancel = createItem(
                Material.REDSTONE,
                plugin.getLanguageManager().getMessage("gui-cancel"),
                "&7Nhấn để hủy và lấy lại vật phẩm"
        );
        inventory.setItem(50, cancel);

        // Total value display
        updateTotalDisplay();
    }

    public void updateTotalDisplay() {
        double total = 0.0;
        for (int i = 9; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                total += plugin.getSellManager().calculateSellValue(item);
            }
        }

        ItemStack totalDisplay = createItem(
                Material.GOLD_INGOT,
                "&6Tổng giá trị: &e" + String.format("%.2f", total) + "$"
        );
        inventory.setItem(49, totalDisplay);
    }

    public Inventory getSellInventory() {
        return sellInventory;
    }
}