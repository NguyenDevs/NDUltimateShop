package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseGUI {

    protected final NDUltimateShop plugin;
    protected final Player player;
    protected Inventory inventory;
    protected int currentPage = 0;

    public BaseGUI(NDUltimateShop plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public abstract void open();

    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(LanguageManager.colorize(name));
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(LanguageManager.colorize(line));
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(LanguageManager.colorize(name));
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(LanguageManager.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    protected void addNavigationButtons(int size) {
        // Previous page button
        if (currentPage > 0) {
            ItemStack prevButton = createItem(
                    Material.ARROW,
                    plugin.getLanguageManager().getMessage("gui-previous-page")
            );
            inventory.setItem(size - 9, prevButton);
        }

        // Next page button (will be set by subclasses if needed)
        // Close button
        ItemStack closeButton = createItem(
                Material.BARRIER,
                plugin.getLanguageManager().getMessage("gui-close")
        );
        inventory.setItem(size - 5, closeButton);
    }

    protected void fillEmptySlots() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }
}