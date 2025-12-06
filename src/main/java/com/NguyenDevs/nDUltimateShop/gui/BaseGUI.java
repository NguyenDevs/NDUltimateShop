package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public abstract class BaseGUI implements InventoryHolder {

    protected final NDUltimateShop plugin;
    protected final Player player;
    protected final GUIConfigManager.GUIConfig config;
    protected Inventory inventory;
    protected int currentPage = 0;
    protected SortType sortType = SortType.NAME_AZ;

    public enum SortType {
        NAME_AZ,
        NAME_ZA,
        TYPE
    }

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

    public void rotateSort() {
        switch (sortType) {
            case NAME_AZ:
                sortType = SortType.NAME_ZA;
                break;
            case NAME_ZA:
                sortType = SortType.TYPE;
                break;
            case TYPE:
                sortType = SortType.NAME_AZ;
                break;
        }
    }

    public ItemStack getSortButton() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String sortName = "";
            switch (sortType) {
                case NAME_AZ:
                    sortName = plugin.getLanguageManager().getMessage("gui-sort-az");
                    break;
                case NAME_ZA:
                    sortName = plugin.getLanguageManager().getMessage("gui-sort-za");
                    break;
                case TYPE:
                    sortName = plugin.getLanguageManager().getMessage("gui-sort-type");
                    break;
            }

            String nameFormat = plugin.getLanguageManager().getMessage("gui-sort-name");
            meta.setDisplayName(plugin.getLanguageManager().colorize(nameFormat.replace("%type%", sortName)));

            List<String> lore = new ArrayList<>();
            String loreKey = "gui-sort-lore";
            if (plugin.getLanguageManager().getMessage(loreKey).startsWith("&cMissing")) {
                lore.add("§7Click để đổi cách sắp xếp.");
            } else {
                String rawLore = plugin.getLanguageManager().getMessage(loreKey);
                lore.add(plugin.getLanguageManager().colorize("&7Click để thay đổi."));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Helper sắp xếp chung cho ShopItem và AuctionListing
    protected <T> void sortItems(List<T> items) {
        items.sort((o1, o2) -> {
            ItemStack i1 = getItemStackFromObject(o1);
            ItemStack i2 = getItemStackFromObject(o2);
            if (i1 == null || i2 == null) return 0;

            String name1 = getDisplayName(i1);
            String name2 = getDisplayName(i2);

            switch (sortType) {
                case NAME_ZA:
                    return name2.compareToIgnoreCase(name1);
                case TYPE:
                    int typeScore1 = getMaterialScore(i1.getType());
                    int typeScore2 = getMaterialScore(i2.getType());
                    if (typeScore1 != typeScore2) {
                        return Integer.compare(typeScore1, typeScore2);
                    }
                    return name1.compareToIgnoreCase(name2);
                case NAME_AZ:
                default:
                    return name1.compareToIgnoreCase(name2);
            }
        });
    }

    private ItemStack getItemStackFromObject(Object obj) {
        if (obj instanceof ShopItem) return ((ShopItem) obj).getItemStack();
        if (obj instanceof AuctionListing) return ((AuctionListing) obj).getItemStack();
        return null;
    }

    private String getDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
        }
        return item.getType().name();
    }

    private int getMaterialScore(Material m) {
        String name = m.name();
        if (name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("BOW") || name.endsWith("TRIDENT")) return 1;
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) return 2;
        if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.endsWith("FISHING_ROD")) return 3;
        if (m.isEdible()) return 4;
        if (m.isBlock()) return 5;
        return 6;
    }

    public int getSortSlot() {
        Map<String, Integer> slots = config.getSlotMapping();
        return slots.getOrDefault("sort", 52);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = page; }
    public GUIConfigManager.GUIConfig getConfig() { return config; }
}