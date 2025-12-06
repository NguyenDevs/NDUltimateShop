package com.NguyenDevs.nDUltimateShop.gui;

import com.NguyenDevs.nDUltimateShop.NDUltimateShop;
import com.NguyenDevs.nDUltimateShop.managers.GUIConfigManager;
import com.NguyenDevs.nDUltimateShop.models.AuctionListing;
import com.NguyenDevs.nDUltimateShop.models.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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

    protected SubSortType subSortType = SubSortType.ENCHANT;

    public enum SortType {
        NAME_AZ,
        NAME_ZA,
        TYPE
    }

    public enum SubSortType {
        ENCHANT,
        FOOD,
        BLOCK,
        TOOL_WEAPON
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

    public void handleSortClick(ClickType clickType) {
        if (clickType.isLeftClick()) {
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
        } else if (clickType.isRightClick()) {
            if (sortType == SortType.TYPE) {
                switch (subSortType) {
                    case ENCHANT:
                        subSortType = SubSortType.FOOD;
                        break;
                    case FOOD:
                        subSortType = SubSortType.BLOCK;
                        break;
                    case BLOCK:
                        subSortType = SubSortType.TOOL_WEAPON;
                        break;
                    case TOOL_WEAPON:
                        subSortType = SubSortType.ENCHANT;
                        break;
                }
            }
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
                    String subName = getSubSortName(subSortType);
                    sortName = plugin.getLanguageManager().getMessage("gui-sort-type") + " (" + subName + "&f)";
                    break;
            }

            String nameFormat = plugin.getLanguageManager().getMessage("gui-sort-name");
            meta.setDisplayName(plugin.getLanguageManager().colorize(nameFormat.replace("%type%", sortName)));

            List<String> lore = new ArrayList<>();
            lore.add(plugin.getLanguageManager().getMessage("gui-action-left"));

            if (sortType == SortType.TYPE) {
                lore.add(plugin.getLanguageManager().getMessage("gui-action-right"));

                String priorityMsg = plugin.getLanguageManager().getMessage("gui-current-priority");
                lore.add(plugin.getLanguageManager().colorize(priorityMsg.replace("%type%", getSubSortName(subSortType))));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getSubSortName(SubSortType type) {
        switch (type) {
            case ENCHANT: return plugin.getLanguageManager().getMessage("gui-subtype-enchant");
            case FOOD: return plugin.getLanguageManager().getMessage("gui-subtype-food");
            case BLOCK: return plugin.getLanguageManager().getMessage("gui-subtype-block");
            case TOOL_WEAPON: return plugin.getLanguageManager().getMessage("gui-subtype-tool");
            default: return "";
        }
    }

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
                    boolean match1 = matchesSubType(i1);
                    boolean match2 = matchesSubType(i2);

                    if (match1 && !match2) return -1;
                    if (!match1 && match2) return 1;

                    return name1.compareToIgnoreCase(name2);
                case NAME_AZ:
                default:
                    return name1.compareToIgnoreCase(name2);
            }
        });
    }

    private boolean matchesSubType(ItemStack item) {
        if (item == null) return false;
        Material m = item.getType();

        switch (subSortType) {
            case ENCHANT:
                return item.getEnchantments().size() > 0 || m == Material.ENCHANTED_BOOK;
            case FOOD:
                return m.isEdible();
            case BLOCK:
                return m.isBlock();
            case TOOL_WEAPON:
                String name = m.name();
                return name.endsWith("_SWORD") || name.endsWith("_AXE") ||
                        name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") ||
                        name.endsWith("_HOE") || name.endsWith("BOW") ||
                        name.endsWith("TRIDENT") || name.endsWith("SHIELD");
            default:
                return false;
        }
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