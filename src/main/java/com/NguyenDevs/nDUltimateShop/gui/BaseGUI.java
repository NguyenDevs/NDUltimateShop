package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseGUI implements InventoryHolder {

    protected final NDUltimateShop plugin;
    protected final Player player;
    protected final GUIConfigManager.GUIConfig config;
    protected Inventory inventory;
    protected int currentPage = 0;

    public BaseGUI(NDUltimateShop plugin, Player player, String configName) {
        this.plugin = plugin;
        this.player = player;
        this.config = plugin.getConfigManager().getGUIConfig(configName);
    }

    public abstract void open();

    public void fillDecorative() {
        List<Integer> fillerSlots = config.getFillerSlots();
        if (fillerSlots.isEmpty()) return;
        ItemStack filler = new ItemStack(config.getFillerMaterial());
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int slot : fillerSlots) {
            if (inventory.getItem(slot) == null) inventory.setItem(slot, filler);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = page; }
    public GUIConfigManager.GUIConfig getConfig() { return config; }
}